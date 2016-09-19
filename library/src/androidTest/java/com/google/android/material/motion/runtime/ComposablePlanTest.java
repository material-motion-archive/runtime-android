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

public class ComposablePlanTest extends AndroidTestCase {

  private static Scheduler scheduler;
  private static TextView textView;
  private Transaction transaction;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    scheduler = new Scheduler();
    textView = new TextView(getContext());
    transaction = new Transaction();
  }

  public void testComposablePlan() {
    // add the root plan and have it delegate to the leaf plan
    RootPlan rootPlan = new RootPlan("rootPlan");
    transaction.addNamedPlan(rootPlan, "rootPlan", textView);
    scheduler.commitTransaction(transaction);

    assertTrue(textView.getText().equals("leafPlan"));
  }

  private class RootPlan extends Plan {

    private String text;

    private RootPlan(String text) {
      this.text = text;
    }

    @Override
    public Class<? extends Performer> getPerformerClass() {
      return ComposablePerformer.class;
    }
  }

  private static class LeafPlan extends Plan {

    private String text;

    private LeafPlan(String text) {
      this.text = text;
    }

    @Override
    public Class<? extends Performer> getPerformerClass() {
      return LeafPlanPerformer.class;
    }
  }

  public static class LeafPlanPerformer extends Performer implements Performer.PlanPerformance {

    @Override
    public void addPlan(Plan plan) {
      LeafPlan leafPlan = (LeafPlan) plan;
      TextView target = getTarget();
      target.setText(leafPlan.text);
    }
  }

  public static class ComposablePerformer extends Performer implements Performer.ComposablePerformance, Performer.PlanPerformance {

    private TransactionEmitter transactionEmitter;

    @Override
    public void setTransactionEmitter(TransactionEmitter transactionEmitter) {
      this.transactionEmitter = transactionEmitter;
    }

    @Override
    public void setComposablePerformanceCallback(ComposablePerformanceCallback callback) {
    }

    @Override
    public void addPlan(Plan plan) {
      // immediately delegate the actual work of changing the text view to the leaf plan
      Transaction transaction = new Transaction();
      LeafPlan leafPlan = new LeafPlan("leafPlan");
      transaction.addNamedPlan(leafPlan, "leafPlan", textView);

      transactionEmitter.emit(transaction);
    }
  }
}
