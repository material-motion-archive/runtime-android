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

import android.support.annotation.VisibleForTesting;

/**
 * A Performer is an object responsible for executing a {@link Plan}.
 * <p>
 * Plans define the {@link Class} of Performer that can fulfill it. Your Performer will be
 * instantiated via reflection, so take care that a {@link PerformerInstantiationException} will not
 * be thrown.
 * <p>
 * The {@link PerformerFeatures} interfaces define optional APIs.
 *
 * @param <T> The type of target this performer can act on.
 * @see <a href="https://material-motion.gitbooks.io/material-motion-starmap/content/specifications/runtime/performer.html">The
 * Performer specification</a>
 */

public abstract class Performer<T> {

  /**
   * Thrown when there is an instantiation failure. Make sure that your {@link Performer}'s class
   * name exists, is public, and has an empty constructor that is public.
   */
  public static class PerformerInstantiationException extends RuntimeException {

    public PerformerInstantiationException(
      Class<? extends Performer> klass, Exception cause) {
      super(
        "Unable to instantiate Performer "
          + klass.getName()
          + ": make sure class name exists, is public, and has an empty constructor that is "
          + "public",
        cause);
    }
  }

  private T target;

  /**
   * Performers are initialized with a target.
   */
  @VisibleForTesting
  public final void initialize(T target) {
    //noinspection unchecked
    this.target = (T) target;
    onInitialize(this.target);
  }

  /**
   * Invoked immediately after this Performer has been initialized with a target.
   */
  protected void onInitialize(T target) {
  }

  /**
   * Provides a {@link Plan} to this Performer. The Performer is expected to execute this plan.
   */
  protected abstract void addPlan(Plan<T> plan);

  /**
   * ​Returns the target that this Performer is associated with. ​
   *
   * @param <Type> Convenience to avoid casting, for when the caller knows the type of the
   * target.
   * @return The target. ​
   */
  public final <Type extends T> Type getTarget() {
    //noinspection unchecked
    return (Type) target;
  }
}
