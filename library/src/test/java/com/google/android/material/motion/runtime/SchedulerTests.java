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
  private Transaction transaction;

  @Before
  public void setUp() {
    Context context = Robolectric.setupActivity(Activity.class);
    scheduler = new Scheduler();
    textView = new TextView(context);
    transaction = new Transaction();
  }

  @Test
  public void testInitialSchedulerState() {
    assertThat(scheduler.getState()).isEqualTo(Scheduler.IDLE);
  }

  @Test
  public void testStandardPerformerSchedulerState() {
    transaction.addNamedPlan(new StandardPlan("standard"), "plan", textView);
    scheduler.commitTransaction(transaction);

    assertThat(scheduler.getState()).isEqualTo(Scheduler.IDLE);
  }

  @Test
  public void testManualPerformerSchedulerState() {
    transaction.addNamedPlan(new ManualPlan("manual"), "plan", textView);
    scheduler.commitTransaction(transaction);

    assertThat(scheduler.getState()).isEqualTo(Scheduler.ACTIVE);
  }

  @Test
  public void testAddingMultipleSchedulerListeners() {
    TestSchedulerListener firstListener = new TestSchedulerListener();
    TestSchedulerListener secondListener = new TestSchedulerListener();
    scheduler.addStateListener(firstListener);
    scheduler.addStateListener(secondListener);

    transaction.addNamedPlan(new ManualPlan("manual one"), "plan", textView);
    transaction.addPlan(new StandardPlan("standard one"), textView);

    scheduler.commitTransaction(transaction);

    assertThat(firstListener.getState()).isEqualTo(Scheduler.ACTIVE);
    assertThat(secondListener.getState()).isEqualTo(Scheduler.ACTIVE);
  }

  @Test
  public void testAddOrderedMultipleSchedulerListeners() {
    TestSchedulerListener firstListener = new TestSchedulerListener();
    TestSchedulerListener secondListener = new TestSchedulerListener();
    scheduler.addStateListener(firstListener);
    scheduler.addStateListener(secondListener);

    transaction.addPlan(new StandardPlan("standard one"), textView);
    transaction.addNamedPlan(new ManualPlan("manual one"), "plan", textView);

    scheduler.commitTransaction(transaction);

    assertThat(firstListener.getState()).isEqualTo(Scheduler.ACTIVE);
    assertThat(secondListener.getState()).isEqualTo(Scheduler.ACTIVE);
  }

  @Test
  public void testRemovingSchedulerListeners() {
    TestSchedulerListener firstListener = new TestSchedulerListener();
    TestSchedulerListener secondListener = new TestSchedulerListener();
    scheduler.addStateListener(firstListener);
    scheduler.addStateListener(secondListener);

    transaction.addNamedPlan(new ManualPlan("manual"), "plan", textView);

    scheduler.removeStateListener(secondListener);

    scheduler.commitTransaction(transaction);

    assertThat(firstListener.getState()).isEqualTo(Scheduler.ACTIVE);
    assertThat(secondListener.getState()).isEqualTo(Scheduler.IDLE);
  }

  @Test
  public void testNeverEndingDelegatePerformanceSchedulerState() {
    transaction.addNamedPlan(new NeverEndingContinuousPlan("continuous"), "plan", textView);
    scheduler.commitTransaction(transaction);

    assertThat(scheduler.getState()).isEqualTo(Scheduler.ACTIVE);
  }

  @Test
  public void testEndingContinuousPerformanceSchedulerState() {
    transaction.addNamedPlan(new EndingContinuousPlan("continuous"), "plan", textView);
    scheduler.commitTransaction(transaction);

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

  private static class StandardPlan extends Plan {

    private final String text;

    private StandardPlan(String text) {
      this.text = text;
    }

    @Override
    public Class<? extends Performer> getPerformerClass() {
      return StandardPerformer.class;
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

  private static class NeverEndingContinuousPlan extends Plan {

    private final String text;

    private NeverEndingContinuousPlan(String text) {
      this.text = text;
    }

    @Override
    public Class<? extends Performer> getPerformerClass() {
      return NeverEndingContinuousPerformer.class;
    }
  }

  private static class EndingContinuousPlan extends Plan {

    private final String text;

    private EndingContinuousPlan(String text) {
      this.text = text;
    }

    @Override
    public Class<? extends Performer> getPerformerClass() {
      return EndingContinuousPerformer.class;
    }
  }

  public static class StandardPerformer extends Performer {

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
    Performer.ContinuousPerformance {

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

  public static class EndingContinuousPerformer extends Performer implements
    Performer.ContinuousPerformance {

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
