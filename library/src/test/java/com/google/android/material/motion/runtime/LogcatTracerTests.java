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

package com.google.android.material.motion.runtime;

import android.app.Activity;
import android.widget.TextView;

import com.google.android.material.motion.runtime.plans.NoOpPlan;
import com.google.android.material.motion.runtime.targets.IncrementerTarget;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.google.common.truth.Truth.assertThat;

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21)
public class LogcatTracerTests {

  private MotionRuntime runtime;

  @Before
  public void setUp() {
    runtime = new MotionRuntime();
    runtime.addTracer(new LogcatTracer());
  }

  @Test
  public void testAddingAndRemovingPlansToAndFromDifferentTargets() {
    Plan regularPlan = new NoOpPlan();
    NamedPlan namedPlan = new NoOpPlan();

    List<Object> targets = new ArrayList<>();
    targets.add(new TextView(Robolectric.setupActivity(Activity.class)));
    targets.add(new Object());
    targets.add("");
    targets.add(" ");
    targets.add("\uD83D\uDC36");
    targets.add(new ArrayList<>());
    targets.add(new HashMap<>());
    targets.add(-1);
    targets.add(0);
    targets.add(1);
    targets.add(Long.MAX_VALUE);
    targets.add(Long.MIN_VALUE);
    targets.add(null);
    targets.add(new IncrementerTarget());

    boolean tracersThrew = false;
    try {
      for (Object target : targets) {
        runtime.addPlan(regularPlan, target);
        runtime.addNamedPlan(namedPlan, "text view altering named plan", target);
        runtime.removeNamedPlan("text view altering named plan", target);
      }
    } catch (Throwable t) {
      tracersThrew = true;
    }

    assertThat(tracersThrew).isFalse();
  }
}
