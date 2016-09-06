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
import android.view.View;
import android.widget.TextView;

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

  public void testDelegatePlanPerformanceSchedulerState() {
    transaction.addNamedPlan(new DelegatedPlan("delegated"), "plan", textView);
    scheduler.commitTransaction(transaction);

    assertTrue(scheduler.getState() == Scheduler.IDLE);
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

  private static class DelegatedPlan extends Plan {

    private final String text;

    private DelegatedPlan(String text) {
      this.text = text;
    }

    @Override
    public Class<? extends Performer> getPerformerClass() {
      return DelegatedPerformer.class;
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

  public static class DelegatedPerformer extends Performer implements Performer.DelegatedPerformance, Performer.PlanPerformance {

    private DelegatedPerformanceTokenCallback tokenCallback;

    @Override
    public void addPlan(Plan plan) {
//      DelegatedPlan delegatedPlan = (DelegatedPlan) plan;
//      View target = getTarget();
      DelegatedPerformanceToken token = tokenCallback.onDelegatedPerformanceStart(this);
//      tokenCallback.onDelegatedPerformanceEnd(this, token);
    }

    @Override
    public void setDelegatedPerformanceCallback(DelegatedPerformanceCallback callback) {

    }

    @Override
    public void setDelegatedPerformanceCallback(DelegatedPerformanceTokenCallback callback) {
      this.tokenCallback = callback;
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
