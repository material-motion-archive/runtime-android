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

import android.util.Log;

import com.google.android.material.motion.runtime.PlanFeatures.NamedPlan;

/**
 * An implementation of {@link Tracing} which logs directly to logcat.
 */
public class LogcatTracer implements Tracing {

  private static final String TAG = "LogcatTracer";

  @Override
  public void onAddPlan(Plan plan, Object target) {
    Log.v(TAG, String.format("didAddPlan: %s to: %s", plan, target));
  }

  @Override
  public void onAddNamedPlan(NamedPlan plan, String name, Object target) {
    Log.v(TAG, String.format("didAddNamedPlan: %s named: %s to: %s", plan, name, target));
  }

  @Override
  public void onRemoveNamedPlan(String name, Object target) {
    Log.v(TAG, String.format("didRemoveNamedPlan: %s from: %s", name, target));
  }

  @Override
  public void onCreatePerformer(Performer performer, Object target) {
    Log.v(TAG, String.format("didCreatePerformer: %s for: %s", performer, target));
  }
}
