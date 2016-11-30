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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.widget.TextView;

import com.google.android.material.motion.runtime.PerformerFeatures.BasePerforming;
import com.google.android.material.motion.runtime.PerformerFeatures.ContinuousPerforming;
import com.google.android.material.motion.runtime.PerformerFeatures.ManualPerforming;
import com.google.android.material.motion.runtime.PerformerFeatures.NamedPlanPerforming;
import com.google.android.material.motion.runtime.PlanFeatures.BasePlan;
import com.google.android.material.motion.runtime.PlanFeatures.NamedPlan;
import com.google.android.material.motion.runtime.MotionRuntime.State;
import com.google.android.material.motion.runtime.MotionRuntime.StateListener;
import com.google.android.material.motion.runtime.plans.TextViewAlteringNamedPlan;
import com.google.android.material.motion.runtime.targets.IncrementerTarget;
import com.google.android.material.motion.runtime.testing.StepChoreographer;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.List;

import static com.google.common.truth.Truth.assertThat;

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21)
public class RuntimeTests {

  private static final float EPSILON = 0.0001f;

  private MotionRuntime runtime;
  private StepChoreographer choreographer;
  private TextView textView;

  @Before
  public void setUp() {
    Context context = Robolectric.setupActivity(Activity.class);
    runtime = new MotionRuntime();
    choreographer = new StepChoreographer();
    runtime.choreographer = choreographer;
    textView = new TextView(context);
  }

  @Test
  public void testInitialRuntimeState() {
    assertThat(runtime.getState()).isEqualTo(MotionRuntime.IDLE);
  }

  @Test
  public void testStandardPerformerRuntimeState() {
    runtime.addNamedPlan(new TextViewAlteringNamedPlan("standard"), "plan", textView);

    assertThat(runtime.getState()).isEqualTo(MotionRuntime.IDLE);
  }

  @Test
  public void testManualPerformerRuntimeState() {
    runtime.addNamedPlan(new ManualPlan("manual"), "plan", textView);

    assertThat(runtime.getState()).isEqualTo(MotionRuntime.ACTIVE);
  }

  @Test
  public void testTwoActivePerformersStillActive() {
    runtime.addNamedPlan(new ManualPlan("manual"), "plan", textView);

    assertThat(runtime.getState()).isEqualTo(MotionRuntime.ACTIVE);

    runtime.addPlan(new NeverEndingContinuousPlan("continuous"), textView);

    // Still active.
    assertThat(runtime.getState()).isEqualTo(MotionRuntime.ACTIVE);
  }

  @Test
  public void testManualPerformerUpdatesWithCorrectDelta() {
    runtime.addPlan(new ManualPlan("manual"), textView);

    // First frame updates with delta == 0.
    choreographer.advance(StepChoreographer.FRAME_MS);
    assertThat((float) textView.getTag()).isWithin(0f).of(0f);

    // Next frame updates with correct delta.
    choreographer.advance(StepChoreographer.FRAME_MS);
    assertThat((float) textView.getTag()).isWithin(EPSILON).of(StepChoreographer.FRAME_MS);
  }

  @Test
  public void testAddingMultipleRuntimeListeners() {
    TestRuntimeListener firstListener = new TestRuntimeListener();
    TestRuntimeListener secondListener = new TestRuntimeListener();
    runtime.addStateListener(firstListener);
    runtime.addStateListener(secondListener);

    runtime.addNamedPlan(new ManualPlan("manual one"), "plan", textView);
    runtime.addPlan(new TextViewAlteringNamedPlan("standard one"), textView);

    assertThat(firstListener.getState()).isEqualTo(MotionRuntime.ACTIVE);
    assertThat(secondListener.getState()).isEqualTo(MotionRuntime.ACTIVE);
  }

  @Test
  public void testAddOrderedMultipleRuntimeListeners() {
    TestRuntimeListener firstListener = new TestRuntimeListener();
    TestRuntimeListener secondListener = new TestRuntimeListener();
    runtime.addStateListener(firstListener);
    runtime.addStateListener(secondListener);

    runtime.addPlan(new TextViewAlteringNamedPlan("standard one"), textView);
    runtime.addNamedPlan(new ManualPlan("manual one"), "plan", textView);

    assertThat(firstListener.getState()).isEqualTo(MotionRuntime.ACTIVE);
    assertThat(secondListener.getState()).isEqualTo(MotionRuntime.ACTIVE);
  }

  @Test
  public void testRemovingRuntimeListeners() {
    TestRuntimeListener firstListener = new TestRuntimeListener();
    TestRuntimeListener secondListener = new TestRuntimeListener();
    runtime.addStateListener(firstListener);

    runtime.addStateListener(secondListener);
    runtime.removeStateListener(secondListener);

    runtime.addNamedPlan(new ManualPlan("manual"), "plan", textView);

    assertThat(firstListener.getState()).isEqualTo(MotionRuntime.ACTIVE);
    assertThat(secondListener.getState()).isEqualTo(MotionRuntime.IDLE);
  }

  @Test
  public void testAddingSameRuntimeListenerTwice() {
    StateListener listener = mock(StateListener.class);

    runtime.addStateListener(listener);
    runtime.addStateListener(listener);

    runtime.addPlan(new ManualPlan("manual"), textView);

    // Listener invoked only once.
    verify(listener, times(1)).onStateChange(runtime, MotionRuntime.ACTIVE);
  }

  @Test
  public void testNeverEndingDelegatePerformingRuntimeState() {
    runtime.addNamedPlan(new NeverEndingContinuousPlan("continuous"), "plan", textView);

    assertThat(runtime.getState()).isEqualTo(MotionRuntime.ACTIVE);
  }

  @Test
  public void testEndingContinuousPerformingRuntimeState() {
    runtime.addNamedPlan(new EndingContinuousPlan("continuous"), "plan", textView);

    assertThat(runtime.getState()).isEqualTo(MotionRuntime.IDLE);
  }

  @Test
  public void testAddingPlanDirectlyToRuntime() {
    runtime.addPlan(new NeverEndingContinuousPlan("continuous"), textView);

    assertThat(runtime.getState()).isEqualTo(MotionRuntime.ACTIVE);
  }

  @Test
  public void testAddingTextViewAlteringNamedPlanDirectlyToRuntime() {
    runtime.addPlan(new TextViewAlteringNamedPlan("standard"), textView);

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
    MotionRuntime runtime = new MotionRuntime();
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
    assertThat(errorThrown).isTrue();
  }

  @Test
  public void testExceptionThrownWhenAddingANamedPlanWithAnEmptyName() {
    boolean errorThrown = false;
    try {
      runtime.addNamedPlan(new NamedTargetAlteringPlan(), "", textView);
    } catch (IllegalArgumentException e) {
      errorThrown = true;
    }
    assertThat(errorThrown).isTrue();
  }

  @Test
  public void testExceptionThrownWhenRemovingANamedPlanWithoutAName() {
    boolean errorThrown = false;
    try {
      runtime.removeNamedPlan(null, textView);
    } catch (IllegalArgumentException e) {
      errorThrown = true;
    }
    assertThat(errorThrown).isTrue();
  }

  @Test
  public void testExceptionThrownWhenRemovingANamedPlanWithAnEmptyName() {
    boolean errorThrown = false;
    try {
      runtime.removeNamedPlan("", textView);
    } catch (IllegalArgumentException e) {
      errorThrown = true;
    }
    assertThat(errorThrown).isTrue();
  }

  @Test
  public void testTracersCanBeAddedToARuntime() {
    StorageTracing firstTracer = new StorageTracing();
    StorageTracing secondTracer = new StorageTracing();
    runtime.addTracer(firstTracer);
    runtime.addTracer(secondTracer);

    assertThat(runtime.getTracers().size()).isEqualTo(2);
  }

  @Test
  public void testTracersAreOnlyAddedOnceToARuntime() {
    StorageTracing firstTracer = new StorageTracing();
    runtime.addTracer(firstTracer);
    runtime.addTracer(firstTracer);

    assertThat(runtime.getTracers().size()).isEqualTo(1);
  }

  @Test
  public void testTracersCanBeRemovedFromARuntime() {
    StorageTracing firstTracer = new StorageTracing();
    StorageTracing secondTracer = new StorageTracing();
    runtime.addTracer(firstTracer);
    runtime.addTracer(secondTracer);
    runtime.removeTracer(firstTracer);

    assertThat(runtime.getTracers().size()).isEqualTo(1);
    assertThat(runtime.getTracers().contains(secondTracer)).isTrue();
  }

  @Test
  public void testRegularPlansAreCommunicatedViaTracers() {
    StorageTracing storageTracer = new StorageTracing();
    Plan plan = new RegularPlanTargetAlteringPlan();

    runtime.addTracer(storageTracer);
    runtime.addPlan(plan, textView);

    assertThat(storageTracer.addedRegularPlans.get(0) instanceof RegularPlanTargetAlteringPlan).isTrue();
    assertThat(textView.getText()).isEqualTo(" regularAddPlanInvoked");
  }

  @Test
  public void testNamedPlansAreCommunicatedViaTracers() {
    StorageTracing storageTracer = new StorageTracing();
    runtime.addTracer(storageTracer);

    runtime.addNamedPlan(new TextViewAlteringNamedPlan("standard"), "plan", textView);
    runtime.removeNamedPlan("plan", textView);

    assertThat(storageTracer.addedNamePlans.size()).isEqualTo(1);
    assertThat(storageTracer.removedNamePlans.size()).isEqualTo(1);
    assertThat("plan").isEqualTo(storageTracer.addedNamePlans.get(0));
    assertThat("plan").isEqualTo(storageTracer.removedNamePlans.get(0));
  }

  @Test
  public void testPlansReusePerformers() {
    StorageTracing storageTracer = new StorageTracing();
    runtime.addTracer(storageTracer);

    runtime.addPlan(new ManualPlan("manual one"), textView);
    runtime.addNamedPlan(new TextViewAlteringNamedPlan("text view altering one"), "plan two", textView);
    runtime.addNamedPlan(new TextViewAlteringNamedPlan("text view altering two"), "plan three", textView);
    runtime.removeNamedPlan("plan two", textView);

    assertThat(storageTracer.performers.size()).isEqualTo(2);
  }

  @Test
  public void testPerformerCallbacksAreInvokedBeforeTracers() {
    TrackingTracing trackingTracer = new TrackingTracing();
    TrackingPlan trackingPlan = new TrackingPlan();

    runtime.addTracer(trackingTracer);
    runtime.addNamedPlan(trackingPlan, "tracking_plan_name", trackingTracer);
    runtime.removeNamedPlan("tracking_plan_name", trackingTracer);

    List<String> expectedEvents = new ArrayList<>();
    expectedEvents.add("performerAddPlan");
    expectedEvents.add("onAddNamedPlan");
    expectedEvents.add("performerRemovePlan");
    expectedEvents.add("onRemoveNamedPlan");

    assertThat(trackingTracer.getEvents()).isEqualTo(expectedEvents);
  }

  private static class TrackingTracing implements Tracing {

    List<String> events = new ArrayList<>();

    @Override
    public void onAddPlan(Plan plan, Object target) {

    }

    @Override
    public void onAddNamedPlan(NamedPlan plan, String name, Object target) {
      events.add("onAddNamedPlan");
    }

    @Override
    public void onRemoveNamedPlan(String name, Object target) {
      events.add("onRemoveNamedPlan");
    }

    @Override
    public void onCreatePerformer(Performer performer, Object target) {

    }

    List<String> getEvents() {
      return events;
    }
  }

  private static class TrackingPlan extends Plan implements NamedPlan {

    @Override
    public Class<? extends NamedPlanPerforming> getPerformerClass() {
      return TrackingPlanPerformer.class;
    }
  }

  public static class StorageTracing implements Tracing {

    List<BasePerforming> performers = new ArrayList<BasePerforming>();
    List<BasePlan> addedRegularPlans = new ArrayList<>();
    List<String> addedNamePlans = new ArrayList<>();
    List<String> removedNamePlans = new ArrayList<>();

    @Override
    public void onAddPlan(Plan plan, Object target) {
      addedRegularPlans.add(plan);
    }

    @Override
    public void onAddNamedPlan(NamedPlan plan, String name, Object target) {
      addedNamePlans.add(name);
    }

    @Override
    public void onRemoveNamedPlan(String name, Object target) {
      removedNamePlans.add(name);
    }

    @Override
    public void onCreatePerformer(Performer performer, Object target) {
      performers.add(performer);
    }
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

  public static class TrackingPlanPerformer extends StoragePlanPerformer {

    @Override
    public void addPlan(NamedPlan plan, String name) {
      TrackingTracing tracer = getTarget();
      tracer.events.add("performerAddPlan");
    }

    @Override
    public void removePlan(String name) {
      TrackingTracing tracer = getTarget();
      tracer.events.add("performerRemovePlan");
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

  public static class ManualPerformer extends Performer implements ManualPerforming,
    NamedPlanPerforming {

    @Override
    public int update(float deltaTimeMs) {
      // Incredibly ugly hack. Tests need to inspect that a certain deltaTimeMs was passed into
      // this function. Save it to the target. Perhaps tracing support will make this easier to
      // test.
      View target = getTarget();
      target.setTag(deltaTimeMs);
      return MotionRuntime.ACTIVE;
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

  public static class TestRuntimeListener implements MotionRuntime.StateListener {

    private int state;

    @Override
    public void onStateChange(MotionRuntime runtime, @State int newState) {
      this.state = newState;
    }

    public int getState() {
      return state;
    }
  }
}
