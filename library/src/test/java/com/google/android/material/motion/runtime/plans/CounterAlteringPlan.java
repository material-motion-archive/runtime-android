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

import com.google.android.material.motion.runtime.Performer;
import com.google.android.material.motion.runtime.PerformerFeatures;
import com.google.android.material.motion.runtime.Plan;
import com.google.android.material.motion.runtime.PlanFeatures.BasePlan;
import com.google.android.material.motion.runtime.targets.IncrementerTarget;

public class CounterAlteringPlan extends Plan {

  @Override
  public Class<? extends PerformerFeatures.BasePerforming> getPerformerClass() {
    return CounterAlteringPerformer.class;
  }

  public static class CounterAlteringPerformer extends Performer {

    @Override
    public void addPlan(BasePlan plan) {
      Object target = getTarget();
      if (target instanceof IncrementerTarget) {
        ((IncrementerTarget)target).addCounter += 1;
      }
    }
  }
}
