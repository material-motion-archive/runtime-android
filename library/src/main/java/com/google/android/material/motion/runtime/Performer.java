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

import com.google.android.material.motion.runtime.PerformerFeatures.BasePerformance;
import com.google.android.material.motion.runtime.PerformerFeatures.PlanPerformance;
import com.google.android.material.motion.runtime.PlanFeatures.BasePlan;

/**
 * A Performer is an object responsible for executing a {@link Plan}.
 *
 * <p> Plans define the {@link Class} of Performer that can fulfill it. Your Performer will be
 * instantiated via reflection, so take care that a {@link PerformerInstantiationException} will not
 * be thrown.
 *
 * <p> The {@link PerformerFeatures} interfaces define optional APIs.
 *
 * @see <a href="https://material-motion.gitbooks.io/material-motion-starmap/content/specifications/runtime/performer.html">The
 * Performer specification</a>
 */

public abstract class Performer implements BasePerformance {

  /**
   * Thrown when there is an instantiation failure. Make sure that your {@link Performer}'s class
   * name exists, is public, and has an empty constructor that is public.
   */
  public static class PerformerInstantiationException extends RuntimeException {

    public PerformerInstantiationException(
      Class<? extends BasePerformance> klass, Exception cause) {
      super(
        "Unable to instantiate Performer "
          + klass.getName()
          + ": make sure class name exists, is public, and has an empty constructor that is "
          + "public",
        cause);
    }
  }

  private Object target;

  @Override
  public final void initialize(Object target) {
    this.target = target;
    onInitialize(target);
  }

  /**
   * Note: Once {@link PlanPerformance} is removed, this empty implementation will be removed.
   */
  @Override
  public void addPlan(BasePlan plan) {
  }

  /**
   * Invoked immediately after this Performer has been initialized with a target.
   */
  protected void onInitialize(Object target) {
  }

  @Override
  public final <T> T getTarget() {
    //noinspection unchecked Cast expected to fail if target type is incorrect
    return (T) target;
  }
}
