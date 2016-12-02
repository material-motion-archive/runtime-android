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
 * A Plan is an object representing what you want something to do. A Plan uses a {@link Performer}
 * to fulfill itself.
 * <p>
 * Plans are {@link Cloneable}, and by default {@link #clone()} makes a shallow copy. If your Plan
 * contains mutable Object references, override {@link #clone()} to make a deep copy.
 * <p>
 * The {@link PlanFeatures} interfaces define optional APIs.
 *
 * @param <T> The type of target this plan can be applied to.
 * @see <a href="https://material-motion.gitbooks.io/material-motion-starmap/content/specifications/runtime/plan.html">The
 * Plan specificiation</a>
 * @see Object#clone()
 */
public abstract class Plan<T> implements Cloneable {

  /**
   * @return The {@link Class} of the {@link Performer} that can fulfill this plan.
   */
  protected abstract Class<? extends Performer<T>> getPerformerClass();

  /**
   * By default this implementation makes a shallow copy. If your Plan contains mutable Object
   * references, override this method to make a deep copy.
   */
  @SuppressWarnings("CloneDoesntCallSuperClone")
  @Override
  public Plan<T> clone() {
    try {
      //noinspection unchecked
      return (Plan<T>) superClone();
    } catch (CloneNotSupportedException e) {
      throw new AssertionError();
    }
  }

  @VisibleForTesting
  Object superClone() throws CloneNotSupportedException {
    return super.clone();
  }
}
