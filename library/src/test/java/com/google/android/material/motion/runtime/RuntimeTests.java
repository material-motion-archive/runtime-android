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
import com.google.android.material.motion.runtime.Runtime.State;
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
public class RuntimeTests {

  private Runtime runtime;
  private TextView textView;

  @Before
  public void setUp() {
    Context context = Robolectric.setupActivity(Activity.class);
    runtime = new Runtime();
    textView = new TextView(context);
  }

  @Test
  public void testInitialRuntimeState() {
    assertThat(runtime.getState()).isEqualTo(Runtime.IDLE);
  }

  @Test
  public void testStandardPerformerRuntimeState() {
    runtime.addNamedPlan(new StandardPlan("standard"), "plan", textView);

    assertThat(runtime.getState()).isEqualTo(Runtime.IDLE);
  }

  @Test
  public void testManualPerformerRuntimeState() {
    runtime.addNamedPlan(new ManualPlan("manual"), "plan", textView);

    assertThat(runtime.getState()).isEqualTo(Runtime.ACTIVE);
  }

  @Test
  public void testAddingMultipleRuntimeListeners() {
    TestRuntimeListener firstListener = new TestRuntimeListener();
    TestRuntimeListener secondListener = new TestRuntimeListener();
    runtime.addStateListener(firstListener);
    runtime.addStateListener(secondListener);

    runtime.addNamedPlan(new ManualPlan("manual one"), "plan", textView);
    runtime.addPlan(new StandardPlan("standard one"), textView);

    assertThat(firstListener.getState()).isEqualTo(Runtime.ACTIVE);
    assertThat(secondListener.getState()).isEqualTo(Runtime.ACTIVE);
  }

  @Test
  public void testAddOrderedMultipleRuntimeListeners() {
    TestRuntimeListener firstListener = new TestRuntimeListener();
    TestRuntimeListener secondListener = new TestRuntimeListener();
    runtime.addStateListener(firstListener);
    runtime.addStateListener(secondListener);

    runtime.addPlan(new StandardPlan("standard one"), textView);
    runtime.addNamedPlan(new ManualPlan("manual one"), "plan", textView);

    assertThat(firstListener.getState()).isEqualTo(Runtime.ACTIVE);
    assertThat(secondListener.getState()).isEqualTo(Runtime.ACTIVE);
  }

  @Test
  public void testRemovingRuntimeListeners() {
    TestRuntimeListener firstListener = new TestRuntimeListener();
    TestRuntimeListener secondListener = new TestRuntimeListener();
    runtime.addStateListener(firstListener);

    runtime.addStateListener(secondListener);
    runtime.removeStateListener(secondListener);

    runtime.addNamedPlan(new ManualPlan("manual"), "plan", textView);

    assertThat(firstListener.getState()).isEqualTo(Runtime.ACTIVE);
    assertThat(secondListener.getState()).isEqualTo(Runtime.IDLE);
  }

  @Test
  public void testNeverEndingDelegatePerformingRuntimeState() {
    runtime.addNamedPlan(new NeverEndingContinuousPlan("continuous"), "plan", textView);

    assertThat(runtime.getState()).isEqualTo(Runtime.ACTIVE);
  }

  @Test
  public void testEndingContinuousPerformingRuntimeState() {
    runtime.addNamedPlan(new EndingContinuousPlan("continuous"), "plan", textView);

    assertThat(runtime.getState()).isEqualTo(Runtime.IDLE);
  }

  @Test
  public void testAddingPlanDirectlyToRuntime() {
    runtime.addPlan(new NeverEndingContinuousPlan("continuous"), textView);

    assertThat(runtime.getState()).isEqualTo(Runtime.ACTIVE);
  }

  @Test
  public void testAddingStandardPlanDirectlyToRuntime() {
    runtime.addPlan(new StandardPlan("standard"), textView);

    assertThat(textView.getText()).isEqualTo(" standard");
  }

  @Test
  public void testAddingNamedPlan() {
    runtime.addNamedPlan(new NamedTargetAlteringPlan(), "common_name", textView);

    assertThat(textView.getText()).isEqualTo(" addPlanInvoked");
  }

  @Test
  public void testAddAndRemoveTheSameNamedPlan() {
    runtime.addNamedPlan(new NamedTargetAlteringPlan(), "name_one", textView);
    runtime.removeNamedPlan("name_one", textView);

    assertThat(textView.getText()).isEqualTo(" addPlanInvoked removePlanInvoked");
  }

  @Test
  public void testRemoveNamedPlanThatWasNeverAdded() {
    runtime.addNamedPlan(new NamedTargetAlteringPlan(), "common_name", textView);
    runtime.removeNamedPlan("this_was_never_added", textView);

    assertThat(textView.getText()).isEqualTo(" addPlanInvoked");
  }

  @Test
  public void testNamedPlansMakeMultipleAddCalls() {
    runtime.addNamedPlan(new NamedTargetAlteringPlan(), "one", textView);
    runtime.addNamedPlan(new NamedTargetAlteringPlan(), "two", textView);

    assertThat(textView.getText()).isEqualTo(" addPlanInvoked addPlanInvoked");
  }

  @Test
  public void testAddAndRemoveCallbacksAreInvoked() {
    NamedTargetAlteringPlan plan1 = new NamedTargetAlteringPlan();
    NamedTargetAlteringPlan plan2 = new NamedTargetAlteringPlan();
    Runtime runtime = new Runtime();
    runtime.addNamedPlan(plan1, "common_name", textView);
    runtime.addNamedPlan(plan2, "common_name", textView);

    assertThat(textView.getText()).isEqualTo(" addPlanInvoked removePlanInvoked addPlanInvoked");
  }

  @Test
  public void testNamedPlansOverwriteOneAnother() {
    IncrementerTarget incrementerTarget = new IncrementerTarget();
    NamedCounterAlteringPlan planA = new NamedCounterAlteringPlan();
    NamedCounterAlteringPlan planB = new NamedCounterAlteringPlan();

    runtime.addNamedPlan(planA, "one", incrementerTarget);
    runtime.addNamedPlan(planB, "one", incrementerTarget);

    assertThat(incrementerTarget.addCounter).isEqualTo(2);
    assertThat(incrementerTarget.removeCounter).isEqualTo(1);
  }

  @Test
  public void testAddingTheSameNamedPlanToTheSameTarget() {
    IncrementerTarget incrementerTarget = new IncrementerTarget();
    runtime.addNamedPlan(new NamedCounterAlteringPlan(), "one", incrementerTarget);
    runtime.addNamedPlan(new NamedCounterAlteringPlan(), "one", incrementerTarget);

    assertThat(incrementerTarget.addCounter).isEqualTo(2);
    assertThat(incrementerTarget.removeCounter).isEqualTo(1);
  }

  @Test
  public void testAddingSimilarNamesToTheSameTarget() {
    IncrementerTarget incrementerTarget = new IncrementerTarget();
    runtime.addNamedPlan(new NamedCounterAlteringPlan(), "one", incrementerTarget);
    runtime.addNamedPlan(new NamedCounterAlteringPlan(), "One", incrementerTarget);
    runtime.addNamedPlan(new NamedCounterAlteringPlan(), "1", incrementerTarget);
    runtime.addNamedPlan(new NamedCounterAlteringPlan(), "ONE", incrementerTarget);

    assertThat(incrementerTarget.addCounter).isEqualTo(4);
    assertThat(incrementerTarget.removeCounter).isEqualTo(0);
  }

  @Test
  public void testAddingNamedPlansToDifferentTargets() {
    IncrementerTarget firstIncrementerTarget = new IncrementerTarget();
    IncrementerTarget secondIncrementerTarget = new IncrementerTarget();
    NamedCounterAlteringPlan plan = new NamedCounterAlteringPlan();

    runtime.addNamedPlan(plan, "one", firstIncrementerTarget);
    runtime.addNamedPlan(plan, "one", secondIncrementerTarget);

    assertThat(firstIncrementerTarget.addCounter).isEqualTo(1);
    assertThat(firstIncrementerTarget.removeCounter).isEqualTo(0);
    assertThat(secondIncrementerTarget.addCounter).isEqualTo(1);
    assertThat(secondIncrementerTarget.removeCounter).isEqualTo(0);
  }

  @Test
  public void testNamedPlanOnlyInvokesNamedPlanCallbacks() {
    runtime.addNamedPlan(new NamedTargetAlteringPlan(), "one", textView);

    assertThat(textView.getText().toString().contains("regularAddPlanInvoked"));
  }

  @Test
  public void testPlanOnlyInvokedPlanCallbacks() {
    runtime.addPlan(new RegularPlanTargetAlteringPlan(), textView);

    assertThat(!textView.getText().toString().contains("addPlanInvoked"));
    assertThat(textView.getText().toString().contains("regularAddPlanInvoked"));
  }

  @Test
  public void testPlanStorageExample() {
    StorageNamedPlan plan = new StorageNamedPlan();
    List<String> list = new ArrayList<>();
    runtime.addNamedPlan(plan, "one", list);

    assertThat(list.size()).isEqualTo(1);
    assertThat(list.get(0)).isEqualTo("one");
  }

  @Test
  public void testPlanStorageRemoveNamedPlanExample() {
    List<NamedPlan> list = new ArrayList<NamedPlan>();
    runtime.removeNamedPlan("never_added", list);

    assertThat(list.size() == 0);
  }

  @Test
  public void testExceptionThrownWhenAddingANamedPlanWithoutAName() {
    boolean errorThrown = false;
    try {
      runtime.addNamedPlan(new NamedTargetAlteringPlan(), null, textView);
    } catch (IllegalArgumentException e) {
      errorThrown = true;
    }
    assertThat(errorThrown);
  }

  @Test
  public void testExceptionThrownWhenAddingANamedPlanWithAnEmptyName() {
    boolean errorThrown = false;
    try {
      runtime.addNamedPlan(new NamedTargetAlteringPlan(), "", textView);
    } catch (IllegalArgumentException e) {
      errorThrown = true;
    }
    assertThat(errorThrown);
  }

  @Test
  public void testExceptionThrownWhenRemovingANamedPlanWithoutAName() {
    boolean errorThrown = false;
    try {
      runtime.removeNamedPlan(null, textView);
    } catch (IllegalArgumentException e) {
      errorThrown = true;
    }
    assertThat(errorThrown);
  }

  @Test
  public void testExceptionThrownWhenRemovingANamedPlanWithAnEmptyName() {
    boolean errorThrown = false;
    try {
      runtime.removeNamedPlan("", textView);
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
      return Runtime.ACTIVE;
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

  public static class TestRuntimeListener implements Runtime.StateListener {

    private int state;

    @Override
    public void onStateChange(Runtime runtime, @State int newState) {
      this.state = newState;
    }

    public int getState() {
      return state;
    }
  }
}
