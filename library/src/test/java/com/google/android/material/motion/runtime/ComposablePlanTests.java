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
import com.google.android.material.motion.runtime.PerformerFeatures.ComposablePerformance;
import com.google.android.material.motion.runtime.PerformerFeatures.NamedPlanPerformance;
import com.google.android.material.motion.runtime.PlanFeatures.BasePlan;
import com.google.android.material.motion.runtime.PlanFeatures.NamedPlan;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21)
public class ComposablePlanTests {

  private Scheduler scheduler;
  private TextView textView;

  @Before
  public void setUp() {
    Context context = Robolectric.setupActivity(Activity.class);
    scheduler = new Scheduler();
    textView = new TextView(context);
  }

  @Test
  public void testComposablePlan() {
    // add the root plan and have it delegate to the leaf plan
    RootPlan rootPlan = new RootPlan("rootPlan");
    scheduler.addNamedPlan(rootPlan, "rootPlan", textView);

    assertThat(textView.getText()).isEqualTo("leafPlan");
  }

  private class RootPlan extends Plan implements NamedPlan {

    private String text;

    private RootPlan(String text) {
      this.text = text;
    }

    @Override
    public Class<? extends NamedPlanPerformance> getPerformerClass() {
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
      return LeafPerformer.class;
    }
  }

  public static class LeafPerformer extends Performer {

    @Override
    public void addPlan(BasePlan plan) {
      LeafPlan leafPlan = (LeafPlan) plan;
      TextView target = getTarget();
      target.setText(leafPlan.text);
    }
  }

  public static class ComposablePerformer extends Performer implements
    ComposablePerformance, NamedPlanPerformance {

    private PlanEmitter planEmitter;

    public void setPlanEmitter(PlanEmitter planEmitter) {
      this.planEmitter = planEmitter;
    }

    @Override
    public void addPlan(BasePlan plan) {
      // immediately delegate the actual work of changing the text view to the leaf plan
      planEmitter.emit(new LeafPlan("leafPlan"));
    }

    @Override
    public void addPlan(NamedPlan plan, String name) {
      addPlan(plan);
    }

    @Override
    public void removePlan(String name) {

    }
  }
}
