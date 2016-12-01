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

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.widget.TextView;

import com.google.android.material.motion.runtime.MotionRuntime.State;
import com.google.android.material.motion.runtime.MotionRuntime.StateListener;
import com.google.android.material.motion.runtime.PerformerFeatures.ContinuousPerforming;
import com.google.android.material.motion.runtime.PerformerFeatures.ManualPerforming;
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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21)
public class RuntimeTests {

  @Test
  public void canCreateDeprecatedRuntime() {
    Plan<Object> plan = new NoOpPlan();
    Object target = new Object();

    Runtime runtime = new Runtime();
    runtime.addPlan(plan, target);
  }

  private static class NoOpPlan extends Plan<Object> {
    @Override
    public Class<? extends Performer<Object>> getPerformerClass() {
      return NoOpPerformer.class;
    }
  }

  public static class NoOpPerformer extends Performer<Object> {
    @Override
    public void addPlan(Plan<Object> plan) {
      // No-op.
    }
  }
}
