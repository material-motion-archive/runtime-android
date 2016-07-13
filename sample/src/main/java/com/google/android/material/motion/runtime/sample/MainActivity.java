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

package com.google.android.material.motion.runtime.sample;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;
import com.google.android.material.motion.runtime.Performer;
import com.google.android.material.motion.runtime.Performer.DelegatedPerformance;
import com.google.android.material.motion.runtime.Performer.PlanPerformance;
import com.google.android.material.motion.runtime.Plan;
import com.google.android.material.motion.runtime.Scheduler;
import com.google.android.material.motion.runtime.Transaction;

/**
 * Material Motion Android Runtime sample Activity.
 */
public class MainActivity extends AppCompatActivity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.main_activity);

    TextView text1 = (TextView) findViewById(R.id.text1);
    TextView text2 = (TextView) findViewById(R.id.text2);

    text1.setText("");
    text2.setAlpha(0f);

    Scheduler scheduler = new Scheduler();
    Transaction transaction = new Transaction();

    transaction.addNamedPlan(new DemoPlan1("trash"), "cd", text1);
    transaction.addPlan(new DemoPlan1("get"), text1);
    transaction.addNamedPlan(new DemoPlan1("real"), "cd", text1);

    transaction.addPlan(new DemoPlan2(.5f), text2);

    scheduler.commitTransaction(transaction);
  }

  private static class DemoPlan1 extends Plan {

    private final String text;

    private DemoPlan1(String text) {
      this.text = text;
    }

    @Override
    public Class<? extends Performer> getPerformerClass() {
      return DemoPerformer1.class;
    }
  }

  public static class DemoPerformer1 extends Performer implements PlanPerformance {

    @Override
    public void addPlan(Plan plan) {
      DemoPlan1 demoPlan = (DemoPlan1) plan;
      TextView target = getTarget();

      target.setText(target.getText() + " " + demoPlan.text);
    }
  }

  private static class DemoPlan2 extends Plan {
    private final float alpha;

    private DemoPlan2(float alpha) {
      this.alpha = alpha;
    }

    @Override
    public Class<? extends Performer> getPerformerClass() {
      return DemoPerformer2.class;
    }
  }

  public static class DemoPerformer2 extends Performer
      implements PlanPerformance, DelegatedPerformance {

    private DelegatedPerformanceCallback callback;

    @Override
    public void setDelegatedPerformanceCallback(DelegatedPerformanceCallback callback) {
      this.callback = callback;
    }

    @Override
    public void addPlan(Plan plan) {
      DemoPlan2 demoPlan = (DemoPlan2) plan;
      View target = getTarget();

      target
          .animate()
          .alpha(demoPlan.alpha)
          .setDuration(2000)
          .setListener(
              new AnimatorListenerAdapter() {
                @Override
                public void onAnimationStart(Animator animation) {
                  callback.onDelegatedPerformanceStart(DemoPerformer2.this, "alpha");
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                  callback.onDelegatedPerformanceEnd(DemoPerformer2.this, "alpha");
                }
              });
    }
  }
}
