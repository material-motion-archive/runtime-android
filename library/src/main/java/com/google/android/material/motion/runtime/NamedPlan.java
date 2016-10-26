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

/**
 * A named plan is a {@link Plan} whose performer supports adding and remove the plan by name.
 * Register a named plan by calling {@link Scheduler#addNamedPlan}, and remove it by calling {@link Scheduler#removeNamedPlan}.
 * NamedPlans should ensure that they override the {@link Plan#getPerformerClass()} method and
 * return a class which implements {@link com.google.android.material.motion.runtime.Performer.NamedPlanPerformance}
 *
 * A named plan or family of named plans enables fine configuration of a performer's behavior.
 *
 * @see <a href="https://material-motion.gitbooks.io/material-motion-starmap/content/specifications/runtime/named-plans.html">The Named Plan specificiation</a> for more details.
 */
public abstract class NamedPlan extends Plan implements Cloneable {

  @Override
  public NamedPlan clone() {
    return (NamedPlan) super.clone();
  }
}
