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
   *
   * @deprecated 2.0.0. Override {@link Performer#addPlan(Plan)} instead.
   */
  @Deprecated
  public interface PlanPerformance {

    /**
     * Provides a {@link Plan} to this Performer. The Performer is expected to execute any plan
     * added in this manner.
     *
     * @deprecated 2.0.0. Override {@link Performer#addPlan(Plan)} instead.
     */
    @Deprecated
    void addPlan(Plan plan);
  }

  /**
   * A Performer can implement this interface in order to support the add and remove for {@link NamedPlan}s APIs.
   */
  public interface NamedPlanPerformance {

    /**
     * Provides a {@link NamedPlan} to this Performer. The Performer is expected to execute any plan
     * added in this manner.
     * @param plan the plan which was added to this performer.
     * @param name the name by which this plan can be identified.
     */
    void addPlan(NamedPlan plan, String name);

    /**
     * Provides a {@link NamedPlan} to this Performer. The Performer is expected remove any plan
     * presented in this manner.
     * @param name the name by which this plan was identified.
     */
    void removePlan(String name);
  }

  /**
   * A Performer implements this interface in order to request and release is-active tokens.
   * The scheduler uses these tokens to inform its active state. If any performer owns an is-active
   * token then the scheduler is active. Otherwise, the scheduler is idle.
   *
   * <p>
   * The only requirement is that the Performer must request a token from the
   * {@link IsActiveTokenGenerator token generator} when the continuous performance
   * {@link IsActiveTokenGenerator#generate() starts}
   * and release the token when the continuous performance {@link IsActiveToken#terminate() ends}.
   */
  public interface ContinuousPerformance {

    /**
     * Called by the {@link Scheduler} to supply the {@link Performer} with a
     * {@link IsActiveTokenGenerator}.
     */
    void setIsActiveTokenGenerator(IsActiveTokenGenerator isActiveTokenGenerator);

    /**
     * A generator for {@link IsActiveToken}s.
     */
    interface IsActiveTokenGenerator {

      /**
       * Generate and return a new is-active token. The receiver of this token is expected to
       * eventually {@link IsActiveToken#terminate()} the token.
       *
       * Usually called by a {@link ContinuousPerformance} when it starts.
       *
       */
      IsActiveToken generate();
    }

    /**
     * A token representing a single unit of continuous performance.
     */
    interface IsActiveToken {

      /**
       * Notifies that the continuous performance has ended. Subsequent invocations of this method
       * will result in an exception.
       */
      void terminate();
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
   * The Performer should call {@link TransactionEmitter#emit(Transaction)} to add new plans.
   */
  public interface ComposablePerformance {

    /**
     * Called by the {@link Scheduler} to supply the {@link Performer} with a
     * {@link TransactionEmitter}.
     */
    void setTransactionEmitter(TransactionEmitter transactionEmitter);

    /**
     * A transaction emitter to be provided to a {@link ComposablePerformance} Performer.
     */
    interface TransactionEmitter {

      /**
       * Adds the plans in the transaction to the {@link Scheduler}.
       */
      void emit(Transaction transaction);
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
   * Provides a {@link Plan} to this Performer. The Performer is expected to execute this plan.
   *
   * Note: Once {@link PlanPerformance} is removed, this will become an abstract method.
   */
  protected void addPlan(Plan plan) {}

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
