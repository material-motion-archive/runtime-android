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

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

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

  public static class NoOpPlan extends NamedPlan<Object> {
    @Override
    public Class<? extends NamedPerformer<Object>> getPerformerClass() {
      return NoOpPerformer.class;
    }
  }

  public static class NoOpPerformer extends NamedPerformer<Object> {
    @Override
    public void addPlan(Plan<Object> plan) {
      // No-op.
    }

    @Override
    public void addPlan(NamedPlan<Object> plan, String name) {
      // No-op.
    }

    @Override
    public void removePlan(String name) {
      // No-op.
    }
  }
}
