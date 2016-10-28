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

import com.google.android.material.motion.runtime.PlanFeatures.BasePlan;

/**
 * A Plan is an object representing what you want something to do. A Plan uses a {@link Performer}
 * to fulfill itself.
 *
 * <p> Plans must be {@link Cloneable}, and by default {@link #clone()} makes a shallow copy. If
 * your Plan contains mutable Object references, override {@link #clone()} to make a deep copy.
 *
 * <p> The {@link PlanFeatures} interfaces define optional APIs.
 *
 * @see <a href="https://material-motion.gitbooks.io/material-motion-starmap/content/specifications/runtime/plan.html">The
 * Plan specificiation</a>
 * @see Object#clone()
 */
public abstract class Plan implements BasePlan {

  /**
   * By default this implementation makes a shallow copy. If your Plan contains mutable Object
   * references, override this method to make a deep copy.
   */
  @Override
  public Plan clone() {
    try {
      return (Plan) super.clone();
    } catch (CloneNotSupportedException e) {
      throw new AssertionError();
    }
  }
}
