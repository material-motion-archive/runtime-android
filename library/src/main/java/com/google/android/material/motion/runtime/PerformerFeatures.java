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

import android.support.annotation.VisibleForTesting;

import com.google.android.material.motion.runtime.MotionRuntime.State;

/**
 * Defines the APIs that a {@link Performer} can implement.
 */
public final class PerformerFeatures {

  @VisibleForTesting
  PerformerFeatures() {
    throw new UnsupportedOperationException();
  }

  /**
   * A Performer implements this interface in order to request and release is-active tokens. The
   * runtime uses these tokens to inform its active state. If any performer owns an is-active
   * token then the runtime is active. Otherwise, the runtime is idle.
   * <p>
   * The only requirement is that the Performer must request a token from the {@link
   * IsActiveTokenGenerator token generator} when the continuous performance {@link
   * IsActiveTokenGenerator#generate() starts} and release the token when the continuous
   * performance {@link IsActiveToken#terminate() ends}.
   */
  public interface ContinuousPerforming {

    /**
     * Called by the {@link MotionRuntime} to supply the {@link Performer} with a {@link
     * IsActiveTokenGenerator}.
     */
    void setIsActiveTokenGenerator(IsActiveTokenGenerator isActiveTokenGenerator);

    /**
     * A generator for {@link IsActiveToken}s.
     */
    interface IsActiveTokenGenerator {

      /**
       * Generate and return a new is-active token. The receiver of this token is expected to
       * eventually {@link IsActiveToken#terminate()} the token.
       * <p>
       * Usually called by a {@link ContinuousPerforming} when it starts.
       */
      IsActiveToken generate();
    }

    /**
     * A token representing a single unit of continuous performance.
     */
    interface IsActiveToken {

      /**
       * Notifies that the continuous performance has ended. Subsequent invocations of this
       * method will result in an exception.
       */
      void terminate();
    }
  }

  /**
   * A Performer implements this interface in order to do manual calculations in {@link
   * #update(float)}.
   * <p>
   * The Performer is expected to calculate and set its target's next state on each update.
   */
  public interface ManualPerforming {

    /**
     * Called by the {@link MotionRuntime} to notify the {@link Performer} of a new frame.
     *
     * @param deltaTimeMs The elapsed time in milliseconds since the last update.
     * @return The {@link State} of this Performer after this update. {@link MotionRuntime#IDLE}
     * means this Performer does not wish to get any more frame updates.
     */
    @State
    int update(float deltaTimeMs);
  }

  /**
   * A Performer implements this interface in order to commit new {@link Plan Plans}.
   * <p>
   * The Performer should call {@link PlanEmitter#emit(Plan)} to add new plans.
   */
  public interface ComposablePerforming {

    /**
     * Called by the {@link MotionRuntime} to supply the {@link Performer} with a {@link
     * PlanEmitter}.
     */
    void setPlanEmitter(PlanEmitter planEmitter);

    /**
     * A plan emitter allows an object to emit new plans to a backing runtime for the target to
     * which the performer is associated.
     *
     * @param <T> This emitter will only accept plans that can be applied to this type of
     * target.
     */
    interface PlanEmitter<T> {

      /**
       * Emit a new plan. The plan will immediately be added to the backing runtime.
       */
      void emit(Plan<T> plan);
    }
  }
}
