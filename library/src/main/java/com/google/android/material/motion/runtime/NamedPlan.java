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

/**
 * A plan that supports the named plan API.
 * <p>
 * A named plan is a {@link Plan} whose performer supports adding and remove the plan by name.
 * Register a named plan by calling {@link MotionRuntime#addNamedPlan(NamedPlan, String, Object)},
 * and remove it by calling {@link MotionRuntime#removeNamedPlan(String, Object)}.
 * <p>
 * A named plan or family of named plans enables fine configuration of a performer's behavior.
 *
 * @see <a href="https://material-motion.gitbooks.io/material-motion-starmap/content/specifications/runtime/named-plans.html">The
 * Named Plan specificiation</a> for more details.
 */
public abstract class NamedPlan<T> extends Plan<T> {

  @Override
  public abstract Class<? extends NamedPerformer<T>> getPerformerClass();

  @Override
  public NamedPlan<T> clone() {
    return (NamedPlan<T>) super.clone();
  }
}
