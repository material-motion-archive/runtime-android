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

/**
 * A tracer object may implement a variety of hooks for the purposes of observing changes to the
 * internal workings of a runtime.
 *
 * @see <a href="https://material-motion.gitbooks.io/material-motion-starmap/content/specifications/runtime/runtime_tracing.html">The
 * Tracing specificiation</a>
 */
public interface Tracing {

  /**
   * Invoked after a plan has been added to the runtime.
   *
   * @param plan the plan which was added.
   * @param target the object on which the plan was targeted.
   */
  <T> void onAddPlan(Plan<T> plan, T target);

  /**
   * Invoked after a named plan has been added to the runtime.
   *
   * @param plan the plan which was added.
   * @param name the name by which the plan is identifiable.
   * @param target the object on which the plan was targeted.
   */
  <T> void onAddNamedPlan(NamedPlan<T> plan, String name, T target);

  /**
   * Invoked when a named plan is removed from the runtime.
   *
   * @param name the name by which the plan was identifiable.
   * @param target the object on which the plan was previously targeted.
   */
  <T> void onRemoveNamedPlan(String name, T target);

  /**
   * Invoked after a performer has been created by the runtime.
   *
   * @param performer the {@link Performer} which was just created.
   * @param target the object on which the performer is targeted.
   */
  <T> void onCreatePerformer(Performer<T> performer, T target);
}
