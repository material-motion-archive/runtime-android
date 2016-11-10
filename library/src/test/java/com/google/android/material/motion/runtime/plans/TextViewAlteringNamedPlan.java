/*
 * Copyright (C) 2016 - present The Material Motion Authors. All Rights Reserved.
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

package com.google.android.material.motion.runtime.plans;

import android.widget.TextView;

import com.google.android.material.motion.runtime.Performer;
import com.google.android.material.motion.runtime.PerformerFeatures.NamedPlanPerforming;
import com.google.android.material.motion.runtime.Plan;
import com.google.android.material.motion.runtime.PlanFeatures.BasePlan;
import com.google.android.material.motion.runtime.PlanFeatures.NamedPlan;

public class TextViewAlteringNamedPlan extends Plan implements NamedPlan {

  private final String text;

  public TextViewAlteringNamedPlan(String text) {
    this.text = text;
  }

  @Override
  public Class<? extends NamedPlanPerforming> getPerformerClass() {
    return TextViewAlteringPerformer.class;
  }

  public static class TextViewAlteringPerformer extends Performer implements NamedPlanPerforming {

    @Override
    public void addPlan(BasePlan plan) {
      Object target = getTarget();
      TextViewAlteringNamedPlan textViewAlteringNamedPlan = (TextViewAlteringNamedPlan) plan;
      if (target instanceof TextView) {
        TextView textView = getTarget();
        textView.setText(textView.getText() + " " + textViewAlteringNamedPlan.text);
      }
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
