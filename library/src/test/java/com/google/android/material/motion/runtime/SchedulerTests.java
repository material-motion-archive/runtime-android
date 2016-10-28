/*
 * Copyright 2016-present The Material Motion Authors. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.android.material.motion.runtime;

import static com.google.common.truth.Truth.assertThat;

import android.app.Activity;
import android.content.Context;
import android.widget.TextView;
import com.google.android.material.motion.runtime.PerformerFeatures.ContinuousPerforming;
import com.google.android.material.motion.runtime.PerformerFeatures.ManualPerforming;
import com.google.android.material.motion.runtime.PerformerFeatures.NamedPlanPerforming;
import com.google.android.material.motion.runtime.PlanFeatures.BasePlan;
import com.google.android.material.motion.runtime.PlanFeatures.NamedPlan;
import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21)
public class SchedulerTests {

  private Scheduler scheduler;
  private TextView textView;

  @Before
  public void setUp() {
    Context context = Robolectric.setupActivity(Activity.class);
    scheduler = new Scheduler();
    textView = new TextView(context);
  }

  @Test
  public void testInitialSchedulerState() {
    assertThat(scheduler.getState()).isEqualTo(Scheduler.IDLE);
  }

  @Test
  public void testStandardPerformerSchedulerState() {
    scheduler.addNamedPlan(new StandardPlan("standard"), "plan", textView);

    assertThat(scheduler.getState()).isEqualTo(Scheduler.IDLE);
  }

  @Test
  public void testManualPerformerSchedulerState() {
    scheduler.addNamedPlan(new ManualPlan("manual"), "plan", textView);

    assertThat(scheduler.getState()).isEqualTo(Scheduler.ACTIVE);
  }

  @Test
  public void testAddingMultipleSchedulerListeners() {
    TestSchedulerListener firstListener = new TestSchedulerListener();
    TestSchedulerListener secondListener = new TestSchedulerListener();
    scheduler.addStateListener(firstListener);
    scheduler.addStateListener(secondListener);

    scheduler.addNamedPlan(new ManualPlan("manual one"), "plan", textView);
    scheduler.addPlan(new StandardPlan("standard one"), textView);

    assertThat(firstListener.getState()).isEqualTo(Scheduler.ACTIVE);
    assertThat(secondListener.getState()).isEqualTo(Scheduler.ACTIVE);
  }

  @Test
  public void testAddOrderedMultipleSchedulerListeners() {
    TestSchedulerListener firstListener = new TestSchedulerListener();
    TestSchedulerListener secondListener = new TestSchedulerListener();
    scheduler.addStateListener(firstListener);
    scheduler.addStateListener(secondListener);

    scheduler.addPlan(new StandardPlan("standard one"), textView);
    scheduler.addNamedPlan(new ManualPlan("manual one"), "plan", textView);

    assertThat(firstListener.getState()).isEqualTo(Scheduler.ACTIVE);
    assertThat(secondListener.getState()).isEqualTo(Scheduler.ACTIVE);
  }

  @Test
  public void testRemovingSchedulerListeners() {
    TestSchedulerListener firstListener = new TestSchedulerListener();
    TestSchedulerListener secondListener = new TestSchedulerListener();
    scheduler.addStateListener(firstListener);

    scheduler.addStateListener(secondListener);
    scheduler.removeStateListener(secondListener);

    scheduler.addNamedPlan(new ManualPlan("manual"), "plan", textView);

    assertThat(firstListener.getState()).isEqualTo(Scheduler.ACTIVE);
    assertThat(secondListener.getState()).isEqualTo(Scheduler.IDLE);
  }

  @Test
  public void testNeverEndingDelegatePerformingSchedulerState() {
    scheduler.addNamedPlan(new NeverEndingContinuousPlan("continuous"), "plan", textView);

    assertThat(scheduler.getState()).isEqualTo(Scheduler.ACTIVE);
  }

  @Test
  public void testEndingContinuousPerformingSchedulerState() {
    scheduler.addNamedPlan(new EndingContinuousPlan("continuous"), "plan", textView);

    assertThat(scheduler.getState()).isEqualTo(Scheduler.IDLE);
  }

  @Test
  public void testAddingPlanDirectlyToScheduler() {
    scheduler.addPlan(new NeverEndingContinuousPlan("continuous"), textView);

    assertThat(scheduler.getState()).isEqualTo(Scheduler.ACTIVE);
  }

  @Test
  public void testAddingStandardPlanDirectlyToScheduler() {
    scheduler.addPlan(new StandardPlan("standard"), textView);

    assertThat(textView.getText()).isEqualTo(" standard");
  }

  @Test
  public void testAddingNamedPlan() {
    scheduler.addNamedPlan(new NamedTargetAlteringPlan(), "common_name", textView);

    assertThat(textView.getText()).isEqualTo(" addPlanInvoked");
  }

  @Test
  public void testAddAndRemoveTheSameNamedPlan() {
    scheduler.addNamedPlan(new NamedTargetAlteringPlan(), "name_one", textView);
    scheduler.removeNamedPlan("name_one", textView);

    assertThat(textView.getText()).isEqualTo(" addPlanInvoked removePlanInvoked");
  }

  @Test
  public void testRemoveNamedPlanThatWasNeverAdded() {
    scheduler.addNamedPlan(new NamedTargetAlteringPlan(), "common_name", textView);
    scheduler.removeNamedPlan("this_was_never_added", textView);

    assertThat(textView.getText()).isEqualTo(" addPlanInvoked");
  }

  @Test
  public void testNamedPlansMakeMultipleAddCalls() {
    scheduler.addNamedPlan(new NamedTargetAlteringPlan(), "one", textView);
    scheduler.addNamedPlan(new NamedTargetAlteringPlan(), "two", textView);

    assertThat(textView.getText()).isEqualTo(" addPlanInvoked addPlanInvoked");
  }

  @Test
  public void testAddAndRemoveCallbacksAreInvoked() {
    NamedTargetAlteringPlan plan1 = new NamedTargetAlteringPlan();
    NamedTargetAlteringPlan plan2 = new NamedTargetAlteringPlan();
    Scheduler scheduler = new Scheduler();
    scheduler.addNamedPlan(plan1, "common_name", textView);
    scheduler.addNamedPlan(plan2, "common_name", textView);

    assertThat(textView.getText()).isEqualTo(" addPlanInvoked removePlanInvoked addPlanInvoked");
  }

  @Test
  public void testNamedPlansOverwriteOneAnother() {
    IncrementerTarget incrementerTarget = new IncrementerTarget();
    NamedCounterAlteringPlan planA = new NamedCounterAlteringPlan();
    NamedCounterAlteringPlan planB = new NamedCounterAlteringPlan();

    scheduler.addNamedPlan(planA, "one", incrementerTarget);
    scheduler.addNamedPlan(planB, "one", incrementerTarget);

    assertThat(incrementerTarget.addCounter).isEqualTo(2);
    assertThat(incrementerTarget.removeCounter).isEqualTo(1);
  }

  @Test
  public void testAddingTheSameNamedPlanToTheSameTarget() {
    IncrementerTarget incrementerTarget = new IncrementerTarget();
    scheduler.addNamedPlan(new NamedCounterAlteringPlan(), "one", incrementerTarget);
    scheduler.addNamedPlan(new NamedCounterAlteringPlan(), "one", incrementerTarget);

    assertThat(incrementerTarget.addCounter).isEqualTo(2);
    assertThat(incrementerTarget.removeCounter).isEqualTo(1);
  }

  @Test
  public void testAddingSimilarNamesToTheSameTarget() {
    IncrementerTarget incrementerTarget = new IncrementerTarget();
    scheduler.addNamedPlan(new NamedCounterAlteringPlan(), "one", incrementerTarget);
    scheduler.addNamedPlan(new NamedCounterAlteringPlan(), "One", incrementerTarget);
    scheduler.addNamedPlan(new NamedCounterAlteringPlan(), "1", incrementerTarget);
    scheduler.addNamedPlan(new NamedCounterAlteringPlan(), "ONE", incrementerTarget);

    assertThat(incrementerTarget.addCounter).isEqualTo(4);
    assertThat(incrementerTarget.removeCounter).isEqualTo(0);
  }

  @Test
  public void testAddingNamedPlansToDifferentTargets() {
    IncrementerTarget firstIncrementerTarget = new IncrementerTarget();
    IncrementerTarget secondIncrementerTarget = new IncrementerTarget();
    NamedCounterAlteringPlan plan = new NamedCounterAlteringPlan();

    scheduler.addNamedPlan(plan, "one", firstIncrementerTarget);
    scheduler.addNamedPlan(plan, "one", secondIncrementerTarget);

    assertThat(firstIncrementerTarget.addCounter).isEqualTo(1);
    assertThat(firstIncrementerTarget.removeCounter).isEqualTo(0);
    assertThat(secondIncrementerTarget.addCounter).isEqualTo(1);
    assertThat(secondIncrementerTarget.removeCounter).isEqualTo(0);
  }

  @Test
  public void testNamedPlanOnlyInvokesNamedPlanCallbacks() {
    scheduler.addNamedPlan(new NamedTargetAlteringPlan(), "one", textView);

    assertThat(textView.getText().toString().contains("regularAddPlanInvoked"));
  }

  @Test
  public void testPlanOnlyInvokedPlanCallbacks() {
    scheduler.addPlan(new RegularPlanTargetAlteringPlan(), textView);

    assertThat(!textView.getText().toString().contains("addPlanInvoked"));
    assertThat(textView.getText().toString().contains("regularAddPlanInvoked"));
  }

  @Test
  public void testPlanStorageExample() {
    StorageNamedPlan plan = new StorageNamedPlan();
    List<String> list = new ArrayList<>();
    scheduler.addNamedPlan(plan, "one", list);

    assertThat(list.size()).isEqualTo(1);
    assertThat(list.get(0)).isEqualTo("one");
  }

  @Test
  public void testPlanStorageRemoveNamedPlanExample() {
    List<NamedPlan> list = new ArrayList<NamedPlan>();
    scheduler.removeNamedPlan("never_added", list);

    assertThat(list.size() == 0);
  }

  @Test
  public void testExceptionThrownWhenAddingANamedPlanWithoutAName() {
    boolean errorThrown = false;
    try {
      scheduler.addNamedPlan(new NamedTargetAlteringPlan(), null, textView);
    } catch (IllegalArgumentException e) {
      errorThrown = true;
    }
    assertThat(errorThrown);
  }

  @Test
  public void testExceptionThrownWhenAddingANamedPlanWithAnEmptyName() {
    boolean errorThrown = false;
    try {
      scheduler.addNamedPlan(new NamedTargetAlteringPlan(), "", textView);
    } catch (IllegalArgumentException e) {
      errorThrown = true;
    }
    assertThat(errorThrown);
  }

  @Test
  public void testExceptionThrownWhenRemovingANamedPlanWithoutAName() {
    boolean errorThrown = false;
    try {
      scheduler.removeNamedPlan(null, textView);
    } catch (IllegalArgumentException e) {
      errorThrown = true;
    }
    assertThat(errorThrown);
  }

  @Test
  public void testExceptionThrownWhenRemovingANamedPlanWithAnEmptyName() {
    boolean errorThrown = false;
    try {
      scheduler.removeNamedPlan("", textView);
    } catch (IllegalArgumentException e) {
      errorThrown = true;
    }
    assertThat(errorThrown);
  }

  private static class StorageNamedPlan extends Plan implements NamedPlan {

    @Override
    public Class<? extends NamedPlanPerforming> getPerformerClass() {
      return StoragePlanPerformer.class;
    }
  }

  private static class RegularPlanTargetAlteringPlan extends Plan {

    @Override
    public Class<? extends NamedPlanPerforming> getPerformerClass() {
      return GenericPlanPerformer.class;
    }
  }

  private static class NamedCounterAlteringPlan extends Plan implements NamedPlan {

    @Override
    public Class<? extends NamedPlanPerforming> getPerformerClass() {
      return NamedCounterPlanPerformer.class;
    }
  }

  private static class NamedTargetAlteringPlan extends Plan implements NamedPlan {

    @Override
    public Class<? extends NamedPlanPerforming> getPerformerClass() {
      return GenericPlanPerformer.class;
    }
  }

  private static class StandardPlan extends Plan implements NamedPlan {

    private final String text;

    private StandardPlan(String text) {
      this.text = text;
    }

    @Override
    public Class<? extends NamedPlanPerforming> getPerformerClass() {
      return StandardPerformer.class;
    }
  }

  private static class ManualPlan extends Plan implements NamedPlan {

    private final String text;

    private ManualPlan(String text) {
      this.text = text;
    }

    @Override
    public Class<? extends NamedPlanPerforming> getPerformerClass() {
      return ManualPerformer.class;
    }
  }

  private static class NeverEndingContinuousPlan extends Plan implements NamedPlan {

    private final String text;

    private NeverEndingContinuousPlan(String text) {
      this.text = text;
    }

    @Override
    public Class<? extends NamedPlanPerforming> getPerformerClass() {
      return NeverEndingContinuousPerformer.class;
    }
  }

  private static class EndingContinuousPlan extends Plan implements NamedPlan {

    private final String text;

    private EndingContinuousPlan(String text) {
      this.text = text;
    }

    @Override
    public Class<? extends NamedPlanPerforming> getPerformerClass() {
      return EndingContinuousPerformer.class;
    }
  }

  public class IncrementerTarget {

    int addCounter = 0;
    int removeCounter = 0;
  }

  public static class NamedCounterPlanPerformer extends Performer implements NamedPlanPerforming {

    @Override
    public void addPlan(BasePlan plan) {
      throw new UnsupportedOperationException();
    }

    @Override
    public void addPlan(NamedPlan plan, String name) {
      IncrementerTarget target = getTarget();
      target.addCounter += 1;
    }

    @Override
    public void removePlan(String name) {
      IncrementerTarget target = getTarget();
      target.removeCounter += 1;
    }
  }

  public static class StoragePlanPerformer extends Performer implements NamedPlanPerforming {

    @Override
    public void addPlan(BasePlan plan) {
      throw new UnsupportedOperationException();
    }

    @Override
    public void addPlan(NamedPlan plan, String name) {
      List<String> target = getTarget();
      target.add(name);
    }

    @Override
    public void removePlan(String name) {
      List<String> target = getTarget();
      target.add(name);
    }
  }

  public static class GenericPlanPerformer extends Performer implements NamedPlanPerforming {

    @Override
    public void addPlan(BasePlan plan) {
      TextView target = getTarget();
      target.setText(target.getText() + " regularAddPlanInvoked");
    }

    @Override
    public void addPlan(NamedPlan plan, String name) {
      TextView target = getTarget();
      target.setText(target.getText() + " addPlanInvoked");
    }

    @Override
    public void removePlan(String name) {
      TextView target = getTarget();
      target.setText(target.getText() + " removePlanInvoked");
    }
  }

  public static class StandardPerformer extends Performer implements NamedPlanPerforming {

    @Override
    public void addPlan(BasePlan plan) {
      StandardPlan standardPlan = (StandardPlan) plan;
      TextView target = getTarget();
      target.setText(target.getText() + " " + standardPlan.text);
    }

    @Override
    public void addPlan(NamedPlan plan, String name) {
      addPlan(plan);
    }

    @Override
    public void removePlan(String name) {
    }
  }

  public static class ManualPerformer extends Performer implements ManualPerforming,
    NamedPlanPerforming {

    @Override
    public int update(float deltaTimeMs) {
      return Scheduler.ACTIVE;
    }

    @Override
    public void addPlan(BasePlan plan) {
    }

    @Override
    public void addPlan(NamedPlan plan, String name) {
      addPlan(plan);
    }

    @Override
    public void removePlan(String name) {
    }
  }

  public static class NeverEndingContinuousPerformer extends Performer implements
    ContinuousPerforming, NamedPlanPerforming {

    private IsActiveTokenGenerator isActiveTokenGenerator;

    @Override
    public void addPlan(BasePlan plan) {
      // start the plan, but never finish it
      IsActiveToken token = isActiveTokenGenerator.generate();
    }

    @Override
    public void setIsActiveTokenGenerator(IsActiveTokenGenerator isActiveTokenGenerator) {
      this.isActiveTokenGenerator = isActiveTokenGenerator;
    }

    @Override
    public void addPlan(NamedPlan plan, String name) {
      addPlan(plan);
    }

    @Override
    public void removePlan(String name) {
    }
  }

  public static class EndingContinuousPerformer extends Performer implements ContinuousPerforming,
    NamedPlanPerforming {

    private IsActiveTokenGenerator isActiveTokenGenerator;

    @Override
    public void addPlan(BasePlan plan) {
      throw new UnsupportedOperationException();
    }

    @Override
    public void setIsActiveTokenGenerator(IsActiveTokenGenerator isActiveTokenGenerator) {
      this.isActiveTokenGenerator = isActiveTokenGenerator;
    }

    @Override
    public void addPlan(NamedPlan plan, String name) {
      // start and end it immediately
      IsActiveToken token = isActiveTokenGenerator.generate();
      token.terminate();
    }

    @Override
    public void removePlan(String name) {
    }
  }

  public static class TestSchedulerListener implements Scheduler.StateListener {

    private int state;

    @Override
    public void onStateChange(Scheduler scheduler, @Scheduler.State int newState) {
      this.state = newState;
    }

    public int getState() {
      return state;
    }
  }
}
