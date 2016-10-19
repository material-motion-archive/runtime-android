/*
 * Copyright (C) 2016 The Material Motion Authors. All Rights Reserved.
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

import android.test.AndroidTestCase;
import android.widget.TextView;

import java.util.List;
import java.util.ArrayList;

public class SchedulerTest extends AndroidTestCase {

  private Scheduler scheduler;
  private TextView textView;
  private Transaction transaction;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    scheduler = new Scheduler();
    textView = new TextView(getContext());
    transaction = new Transaction();
  }

  public void testInitialSchedulerState() {
    assertTrue(scheduler.getState() == Scheduler.IDLE);
  }

  public void testStandardPlanPerformanceSchedulerState() {
    transaction.addNamedPlan(new StandardPlan("standard"), "plan", textView);
    scheduler.commitTransaction(transaction);

    assertTrue(scheduler.getState() == Scheduler.IDLE);
  }

  public void testManualPlanPerformanceSchedulerState() {
    transaction.addNamedPlan(new ManualPlan("manual"), "plan", textView);
    scheduler.commitTransaction(transaction);

    assertTrue(scheduler.getState() == Scheduler.ACTIVE);
  }

  public void testAddingMultipleSchedulerListeners() {
    TestSchedulerListener firstListener = new TestSchedulerListener();
    TestSchedulerListener secondListener = new TestSchedulerListener();
    scheduler.addStateListener(firstListener);
    scheduler.addStateListener(secondListener);

    transaction.addNamedPlan(new ManualPlan("manual one"), "plan", textView);
    transaction.addPlan(new StandardPlan("standard one"), textView);

    scheduler.commitTransaction(transaction);

    assertTrue(firstListener.getState() == Scheduler.ACTIVE);
    assertTrue(secondListener.getState() == Scheduler.ACTIVE);
  }

  public void testAddOrderedMultipleSchedulerListeners() {
    TestSchedulerListener firstListener = new TestSchedulerListener();
    TestSchedulerListener secondListener = new TestSchedulerListener();
    scheduler.addStateListener(firstListener);
    scheduler.addStateListener(secondListener);

    transaction.addPlan(new StandardPlan("standard one"), textView);
    transaction.addNamedPlan(new ManualPlan("manual one"), "plan", textView);

    scheduler.commitTransaction(transaction);

    assertTrue(firstListener.getState() == Scheduler.ACTIVE);
    assertTrue(secondListener.getState() == Scheduler.ACTIVE);
  }

  public void testRemovingSchedulerListeners() {
    TestSchedulerListener firstListener = new TestSchedulerListener();
    TestSchedulerListener secondListener = new TestSchedulerListener();
    scheduler.addStateListener(firstListener);
    scheduler.addStateListener(secondListener);

    transaction.addNamedPlan(new ManualPlan("manual"), "plan", textView);

    scheduler.removeStateListener(secondListener);

    scheduler.commitTransaction(transaction);

    assertTrue(firstListener.getState() == Scheduler.ACTIVE);
    assertTrue(secondListener.getState() == Scheduler.IDLE);
  }

  public void testNeverEndingDelegatePlanPerformanceSchedulerState() {
    transaction.addNamedPlan(new NeverEndingDelegatedPlan("delegated"), "plan", textView);
    scheduler.commitTransaction(transaction);

    assertTrue(scheduler.getState() == Scheduler.ACTIVE);
  }

  public void testEndingDelegatedPlanPerformanceSchedulerState() {
    transaction.addNamedPlan(new EndingDelegatedPlan("delegated"), "plan", textView);
    scheduler.commitTransaction(transaction);

    assertTrue(scheduler.getState() == Scheduler.IDLE);
  }

  public void testAddingPlanDirectlyToScheduler() {
    scheduler.addPlan(new NeverEndingDelegatedPlan("delegated"), textView);

    assertTrue(scheduler.getState() == Scheduler.ACTIVE);
  }

  public void testAddingStandardPlanDirectlyToScheduler() {
    scheduler.addPlan(new StandardPlan("standard"), textView);

    assertTrue(textView.getText().equals(" standard"));
  }

  public void testAddingNamedPlan() {
    scheduler.addNamedPlan(new NamedTargetAlteringPlan(), "common_name", textView);

    assertTrue(textView.getText().equals(" removePlanInvoked addPlanInvoked"));
  }

  public void testAddAndRemoveTheSameNamedPlan() {
    scheduler.addNamedPlan(new NamedTargetAlteringPlan(), "name_one", textView);
    scheduler.removeNamedPlan("name_one", textView);

    assertTrue(textView.getText().equals(" removePlanInvoked addPlanInvoked removePlanInvoked"));
  }

  public void testRemoveNamedPlanThatWasNeverAdded() {
    scheduler.addNamedPlan(new NamedTargetAlteringPlan(), "common_name", textView);
    scheduler.removeNamedPlan("this_was_never_added", textView);

    assertTrue(textView.getText().equals(" removePlanInvoked addPlanInvoked"));
  }

  public void testNamedPlansMakeMultipleAddAndRemoveCalls() {
    scheduler.addNamedPlan(new NamedTargetAlteringPlan(), "one", textView);
    scheduler.addNamedPlan(new NamedTargetAlteringPlan(), "two", textView);

    assertTrue(textView.getText().equals(" removePlanInvoked addPlanInvoked removePlanInvoked addPlanInvoked"));
  }

  public void testNamedPlansOverwriteOneAnother() {
    IncrementerTarget incrementerTarget = new IncrementerTarget();
    NamedCounterAlteringPlan planA = new NamedCounterAlteringPlan();
    NamedCounterAlteringPlan planB = new NamedCounterAlteringPlan();

    scheduler.addNamedPlan(planA, "one", incrementerTarget);
    scheduler.addNamedPlan(planB, "one", incrementerTarget);

    assertTrue(incrementerTarget.addCounter == 2);
    assertTrue(incrementerTarget.removeCounter == 2);
  }

  public void testAddingTheSameNamedPlanToTheSameTarget() {
    IncrementerTarget incrementerTarget = new IncrementerTarget();
    scheduler.addNamedPlan(new NamedCounterAlteringPlan(), "one", incrementerTarget);
    scheduler.addNamedPlan(new NamedCounterAlteringPlan(), "one", incrementerTarget);

    assertTrue(incrementerTarget.addCounter == 2);
    assertTrue(incrementerTarget.removeCounter == 2);
  }

  public void testAddingSimilarNamesToTheSameTarget() {
    IncrementerTarget incrementerTarget = new IncrementerTarget();
    scheduler.addNamedPlan(new NamedCounterAlteringPlan(), "one", incrementerTarget);
    scheduler.addNamedPlan(new NamedCounterAlteringPlan(), "One", incrementerTarget);
    scheduler.addNamedPlan(new NamedCounterAlteringPlan(), "1", incrementerTarget);
    scheduler.addNamedPlan(new NamedCounterAlteringPlan(), "ONE", incrementerTarget);

    assertTrue(incrementerTarget.addCounter == 4);
    assertTrue(incrementerTarget.removeCounter == 4);
  }

  public void testAddingNamedPlansToDifferentTargets() {
    IncrementerTarget firstIncrementerTarget = new IncrementerTarget();
    IncrementerTarget secondIncrementerTarget = new IncrementerTarget();
    NamedCounterAlteringPlan plan = new NamedCounterAlteringPlan();

    scheduler.addNamedPlan(plan, "one", firstIncrementerTarget);
    scheduler.addNamedPlan(plan, "one", secondIncrementerTarget);

    assertTrue(firstIncrementerTarget.addCounter == 1);
    assertTrue(firstIncrementerTarget.removeCounter == 1);
    assertTrue(secondIncrementerTarget.addCounter == 1);
    assertTrue(secondIncrementerTarget.removeCounter == 1);
  }

  public void testNamedPlanOnlyInvokesNamedPlanCallbacks() {
    scheduler.addNamedPlan(new NamedTargetAlteringPlan(), "one", textView);

    assertFalse(textView.getText().toString().contains("regularAddPlanInvoked"));
  }

  public void testPlanOnlyInvokedPlanCallbacks() {
    scheduler.addPlan(new RegularPlanTargetAlteringPlan(), textView);

    assertFalse(textView.getText().toString().contains("removePlanInvoked"));
    assertFalse(textView.getText().toString().contains("addPlanInvoked"));
    assertTrue(textView.getText().toString().contains("regularAddPlanInvoked"));
  }

  public void testPlanStorageExample() {
    StorageNamedPlan plan = new StorageNamedPlan();
    List<NamedPlan> list = new ArrayList<NamedPlan>();
    scheduler.addNamedPlan(plan, "one", list);

    assertTrue(list.size() == 2);
    assertTrue(list.get(0) instanceof NamedPlan);
    assertTrue(list.get(1) instanceof NamedPlan);
    // plans are always copied
    assertFalse(list.contains(plan));
  }

  public void testExceptionThrownWhenAddingANamedPlanWithoutAName() {
    boolean errorThrown = false;
    try {
      scheduler.addNamedPlan(new NamedTargetAlteringPlan(), null, textView);
    } catch (IllegalArgumentException e) {
      errorThrown = true;
    }
    assertTrue(errorThrown);
  }

  public void testExceptionThrownWhenAddingANamedPlanWithAnEmptyName() {
    boolean errorThrown = false;
    try {
      scheduler.addNamedPlan(new NamedTargetAlteringPlan(), "", textView);
    } catch (IllegalArgumentException e) {
      errorThrown = true;
    }
    assertTrue(errorThrown);
  }

  public void testExceptionThrownWhenRemovingANamedPlanWithoutAName() {
    boolean errorThrown = false;
    try {
      scheduler.removeNamedPlan(null, textView);
    } catch (IllegalArgumentException e) {
      errorThrown = true;
    }
    assertTrue(errorThrown);
  }

  public void testExceptionThrownWhenRemovingANamedPlanWithAnEmptyName() {
    boolean errorThrown = false;
    try {
      scheduler.removeNamedPlan("", textView);
    } catch (IllegalArgumentException e) {
      errorThrown = true;
    }
    assertTrue(errorThrown);
  }

  private static class StorageNamedPlan extends NamedPlan {
    @Override
    public Class<? extends Performer> getPerformerClass() { return StoragePlanPerformer.class; }
  }

  private static class RegularPlanTargetAlteringPlan extends Plan {
    @Override
    public Class<? extends Performer> getPerformerClass() { return GenericPlanPerformer.class; }
  }

  private static class NamedCounterAlteringPlan extends NamedPlan {
    @Override
    public Class<? extends Performer> getPerformerClass() { return NamedCounterPlanPerformer.class; }
  }

  private static class NamedTargetAlteringPlan extends NamedPlan {
    @Override
    public Class<? extends Performer> getPerformerClass() { return GenericPlanPerformer.class; }
  }

  private static class StandardPlan extends Plan {

    private final String text;

    private StandardPlan(String text) {
      this.text = text;
    }

    @Override
    public Class<? extends Performer> getPerformerClass() {
      return StandardPlanPerformer.class;
    }
  }

  private static class ManualPlan extends Plan {

    private final String text;

    private ManualPlan(String text) {
      this.text = text;
    }

    @Override
    public Class<? extends Performer> getPerformerClass() {
      return ManualPerformer.class;
    }
  }

  private static class NeverEndingDelegatedPlan extends Plan {

    private final String text;

    private NeverEndingDelegatedPlan(String text) {
      this.text = text;
    }

    @Override
    public Class<? extends Performer> getPerformerClass() {
      return NeverEndingContinuousPerformer.class;
    }
  }

  private static class EndingDelegatedPlan extends Plan {

    private final String text;

    private EndingDelegatedPlan(String text) {
      this.text = text;
    }

    @Override
    public Class<? extends Performer> getPerformerClass() {
      return EndingDelegatedPerformer.class;
    }
  }

  public class IncrementerTarget {
    int addCounter = 0;
    int removeCounter = 0;
  }

  public static class NamedCounterPlanPerformer extends Performer implements Performer.NamedPlanPerformance {

    @Override
    public void addPlan(NamedPlan plan, String name) {
      IncrementerTarget target = getTarget();
      target.addCounter += 1;
    }

    @Override
    public void removePlan(NamedPlan plan, String name) {
      IncrementerTarget target = getTarget();
      target.removeCounter += 1;
    }
  }

  public static class StoragePlanPerformer extends Performer implements Performer.NamedPlanPerformance {

    @Override
    public void addPlan(NamedPlan plan, String name) {
      List<NamedPlan> target = getTarget();
      target.add(plan);
    }

    @Override
    public void removePlan(NamedPlan plan, String name) {
      List<NamedPlan> target = getTarget();
      target.add(plan);
    }
  }

  public static class GenericPlanPerformer extends Performer implements Performer.NamedPlanPerformance, Performer.PlanPerformance {

    @Override
    public void addPlan(Plan plan) {
      TextView target = getTarget();
      target.setText(target.getText() + " regularAddPlanInvoked");
    }

    @Override
    public void addPlan(NamedPlan plan, String name) {
      TextView target = getTarget();
      target.setText(target.getText() + " addPlanInvoked");
    }

    @Override
    public void removePlan(NamedPlan plan, String name) {
      TextView target = getTarget();
      target.setText(target.getText() + " removePlanInvoked");
    }
  }

  public static class StandardPlanPerformer extends Performer implements Performer.PlanPerformance {

    @Override
    public void addPlan(Plan plan) {
      StandardPlan standardPlan = (StandardPlan) plan;
      TextView target = getTarget();
      target.setText(target.getText() + " " + standardPlan.text);
    }
  }

  public static class ManualPerformer extends Performer implements Performer.ManualPerformance {

    @Override
    public int update(float deltaTimeMs) {
      return Scheduler.ACTIVE;
    }
  }

  public static class NeverEndingContinuousPerformer extends Performer implements
    Performer.ContinuousPerformance, Performer.PlanPerformance {

    private IsActiveTokenGenerator isActiveTokenGenerator;

    @Override
    public void addPlan(Plan plan) {
      // start the plan, but never finish it
      IsActiveToken token = isActiveTokenGenerator.generate();
    }

    @Override
    public void setIsActiveTokenGenerator(IsActiveTokenGenerator isActiveTokenGenerator) {
      this.isActiveTokenGenerator = isActiveTokenGenerator;
    }
  }

  public static class EndingDelegatedPerformer extends Performer implements
    Performer.ContinuousPerformance, Performer.PlanPerformance {

    private IsActiveTokenGenerator isActiveTokenGenerator;

    @Override
    public void addPlan(Plan plan) {
      // start and end it immediately
      IsActiveToken token = isActiveTokenGenerator.generate();
      token.terminate();
    }

    @Override
    public void setIsActiveTokenGenerator(IsActiveTokenGenerator isActiveTokenGenerator) {
      this.isActiveTokenGenerator = isActiveTokenGenerator;
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
