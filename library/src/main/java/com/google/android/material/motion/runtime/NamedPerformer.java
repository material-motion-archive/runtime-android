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
 * A performer that supports the named plan API.
 */
public abstract class NamedPerformer extends Performer {

  /**
   * Provides a {@link NamedPlan} to this Performer. The Performer is expected to execute any plan
   * added in this manner.
   *
   * @param plan the plan which was added to this performer.
   * @param name the name by which this plan can be identified.
   */
  public abstract void addPlan(NamedPlan plan, String name);

  /**
   * Provides a {@link NamedPlan} to this Performer. The Performer is expected remove any plan
   * presented in this manner.
   *
   * @param name the name by which this plan was identified.
   */
  public abstract void removePlan(String name);
}
