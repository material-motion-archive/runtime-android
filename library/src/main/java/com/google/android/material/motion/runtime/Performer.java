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

import com.google.android.material.motion.runtime.Scheduler.State;

/**
 * A Performer is an object responsible for executing a {@link Plan}.
 *
 * <p>
 * Plans define the {@link Class} of Performer that can fulfill it. Your Performer will be
 * instantiated via reflection, so take care that a {@link PerformerInstantiationException} will
 * not be thrown.
 *
 * <p>
 * The <code>*Performance</code> interfaces define optional APIs.
 *
 * @see <a href="https://material-motion.gitbooks.io/material-motion-starmap/content/specifications/runtime/performer.html">The Performer specification</a>
 */

public abstract class Performer {

  /**
   * A Performer implements this interface in order to support the Add Plans API.
   *
   * <p>
   * A Performer can have logic that is configured by the {@link Plan Plans} provided to it.
   */
  public interface PlanPerformance {

    /**
     * Provides a {@link Plan} to this Performer. The Performer is expected to execute any plan
     * added in this manner.
     */
    void addPlan(Plan plan);
  }

  /**
   * A Performer implements this interface in order to delegate its work to another API.
   * {@link android.animation.Animator} and {@link android.view.ViewPropertyAnimator} are examples
   * of APIs that can be delegated to.
   *
   * <p>
   * The only requirement is that the Performer must be able to notify the
   * {@link DelegatedPerformanceTokenCallback callback} when the delegated work
   * {@link DelegatedPerformanceTokenCallback#onDelegatedPerformanceStart(DelegatedPerformance) starts}
   * and
   * {@link DelegatedPerformanceTokenCallback#onDelegatedPerformanceEnd(DelegatedPerformance, DelegatedPerformanceToken) ends}.
   */
  public interface DelegatedPerformance {

    /**
     * Called by the {@link Scheduler} to supply the {@link Performer} with a
     * {@link DelegatedPerformanceCallback}.
     */
    @Deprecated
    void setDelegatedPerformanceCallback(DelegatedPerformanceCallback callback);

    /**
     * Called by the {@link Scheduler} to supply the {@link Performer} with a
     * {@link DelegatedPerformanceTokenCallback}.
     */
    void setDelegatedPerformanceCallback(DelegatedPerformanceTokenCallback callback);

    /**
     * A callback to be provided to a {@link DelegatedPerformance} Performer.
     */
    @Deprecated
    interface DelegatedPerformanceCallback {

      /**
       * Notifies that the delegated performance has started.
       *
       * @param performer The Performer whose delegated performance has started.
       * @param name The identifier of the delegated performance. Must have a matching start and end.
       */
      void onDelegatedPerformanceStart(DelegatedPerformance performer, String name);

      /**
       * Notifies that the delegated performance has ended.
       *
       * @param performer The Performer whose delegated performance has ended.
       * @param name The identifier of the delegated performance. Must have a matching start and end.
       */
      void onDelegatedPerformanceEnd(DelegatedPerformance performer, String name);
    }

    /**
     * A token representing a single unit of delegated performance.
     */
    final class DelegatedPerformanceToken {}

    /**
     * A callback to be provided to a {@link DelegatedPerformance} Performer.
     */
    interface DelegatedPerformanceTokenCallback {

      /**
       * Notifies that the delegated performance has started.
       *
       * @param performer The Performer whose delegated performance has started.
       *
       * @return The token of the delegated performance. Must be provided to
       *     {@link #onDelegatedPerformanceEnd(DelegatedPerformance, DelegatedPerformanceToken)}.
       */
      DelegatedPerformanceToken onDelegatedPerformanceStart(DelegatedPerformance performer);

      /**
       * Notifies that the delegated performance has ended.
       *
       * @param performer The Performer whose delegated performance has ended.
       * @param token The token of the delegated performance returned by
       *     {@link #onDelegatedPerformanceStart(DelegatedPerformance)}.
       */
      void onDelegatedPerformanceEnd(
          DelegatedPerformance performer, DelegatedPerformanceToken token);
    }
  }

  /**
   * A Performer implements this interface in order to do manual calculations in
   * {@link #update(float)}.
   *
   * <p>
   * The Performer is expected to calculate and set its target's next state on each update.
   */
  public interface ManualPerformance {

    /**
     * Called by the {@link Scheduler} to notify the {@link Performer} of a new frame.
     *
     * @param deltaTimeMs The elapsed time in milliseconds since the last update.
     * @return The {@link State} of this Performer after this update. {@link Scheduler#IDLE} means
     *     this Performer does not wish to get any more frame updates.
     */
    @State
    int update(float deltaTimeMs);
  }

  /**
   * A Performer implements this interface in order to commit new {@link Plan Plans}.
   *
   * <p>
   * The Performer should call {@link ComposablePerformanceCallback#transact(Work)} to receive a
   * {@link Transaction} to add plans to.
   */
  public interface ComposablePerformance {

    /**
     * Called by the {@link Scheduler} to supply the {@link Performer} with a
     * {@link ComposablePerformanceCallback}.
     */
    void setComposablePerformanceCallback(ComposablePerformanceCallback callback);

    /**
     * A callback to be provided to a {@link ComposablePerformance} Performer.
     */
    interface ComposablePerformanceCallback {

      /**
       * The {@link Performer} calls this when it wants to commit new {@link Plan Plans} to the
       * {@link Scheduler}.
       *
       * @param work A {@link Work} that adds new Plans to a {@link Transaction} provided by the
       *     Scheduler.
       */
      void transact(Work work);
    }

    /**
     * A function object that adds {@link Plan Plans} to a {@link Transaction}.
     */
    abstract class Work {

      /**
       * Adds {@link Plan Plans} to a {@link Transaction}.
       */
      abstract void work(Transaction transaction);
    }
  }

  /**
   * Thrown when there is an instantiation failure. Make sure that your
   * {@link Performer}'s class name exists, is public, and has an empty constructor that is public.
   */
  public static class PerformerInstantiationException extends RuntimeException {
    public PerformerInstantiationException(Class<? extends Performer> klass, Exception cause) {
      super(
          "Unable to instantiate Performer "
              + klass.getName()
              + ": make sure class name exists, is public, and has an empty constructor that is "
              + "public",
          cause);
    }
  }

  private Object target;

  /**
   * Performers are initialized with a target.
   */
  public final void initialize(Object target) {
    this.target = target;
    onInitialize(target);
  }

  /**
   * Invoked immediately after this Performer has been initialized with a target.
   */
  protected void onInitialize(Object target) {}

  /**
   * Returns the target that this Performer is associated with.
   *
   * @param <T> Convenience to avoid casting, for when the caller knows the type of the target.
   * @return The target.
   */
  @SuppressWarnings("unchecked") // Cast expected to fail if target type is incorrect.
  protected final <T> T getTarget() {
    return (T) target;
  }
}
