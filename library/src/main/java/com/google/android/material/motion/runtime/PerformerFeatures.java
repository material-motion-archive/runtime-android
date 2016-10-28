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

import com.google.android.material.motion.runtime.PlanFeatures.BasePlan;
import com.google.android.material.motion.runtime.PlanFeatures.NamedPlan;
import com.google.android.material.motion.runtime.Scheduler.State;

/**
 * Defines the APIs that a {@link Performer} can implement.
 */
public final class PerformerFeatures {

  private PerformerFeatures() {
  }

  /**
   * Defines the base functionality for {@link Performer}s. You should not have to implement this
   * interface yourself.
   */
  public interface BasePerformance {

    /**
     * Performers are initialized with a target.
     */
    void initialize(Object target);

    /**
     * Provides a {@link Plan} to this Performer. The Performer is expected to execute this plan.
     */
    void addPlan(BasePlan plan);

    /**
     * Returns the target that this Performer is associated with.
     *
     * @param <T> Convenience to avoid casting, for when the caller knows the type of the target.
     * @return The target.
     */
    <T> T getTarget();
  }

  /**
   * A Performer implements this interface in order to support the Add Plans API.
   *
   * <p> A Performer can have logic that is configured by the {@link Plan Plans} provided to it.
   *
   * @deprecated 2.0.0. Override {@link Performer#addPlan(BasePlan)} instead.
   */
  @Deprecated
  public interface PlanPerformance extends BasePerformance {

    /**
     * Provides a {@link Plan} to this Performer. The Performer is expected to execute any plan
     * added in this manner.
     *
     * @deprecated 2.0.0. Override {@link Performer#addPlan(BasePlan)} instead.
     */
    @Deprecated
    void addPlan(BasePlan plan);
  }

  /**
   * A Performer can implement this interface in order to support the add and remove for {@link
   * NamedPlan}s APIs.
   */
  public interface NamedPlanPerformance extends BasePerformance {

    /**
     * Provides a {@link NamedPlan} to this Performer. The Performer is expected to execute any plan
     * added in this manner.
     *
     * @param plan the plan which was added to this performer.
     * @param name the name by which this plan can be identified.
     */
    void addPlan(NamedPlan plan, String name);

    /**
     * Provides a {@link NamedPlan} to this Performer. The Performer is expected remove any plan
     * presented in this manner.
     *
     * @param name the name by which this plan was identified.
     */
    void removePlan(String name);
  }

  /**
   * A Performer implements this interface in order to request and release is-active tokens. The
   * scheduler uses these tokens to inform its active state. If any performer owns an is-active
   * token then the scheduler is active. Otherwise, the scheduler is idle.
   *
   * <p> The only requirement is that the Performer must request a token from the {@link
   * IsActiveTokenGenerator token generator} when the continuous performance {@link
   * IsActiveTokenGenerator#generate() starts} and release the token when the continuous performance
   * {@link IsActiveToken#terminate() ends}.
   */
  public interface ContinuousPerformance extends BasePerformance {

    /**
     * Called by the {@link Scheduler} to supply the {@link Performer} with a {@link
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
       *
       * Usually called by a {@link ContinuousPerformance} when it starts.
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
   * A Performer implements this interface in order to do manual calculations in {@link
   * #update(float)}.
   *
   * <p> The Performer is expected to calculate and set its target's next state on each update.
   */
  public interface ManualPerformance extends BasePerformance {

    /**
     * Called by the {@link Scheduler} to notify the {@link Performer} of a new frame.
     *
     * @param deltaTimeMs The elapsed time in milliseconds since the last update.
     * @return The {@link State} of this Performer after this update. {@link Scheduler#IDLE} means
     * this Performer does not wish to get any more frame updates.
     */
    @State
    int update(float deltaTimeMs);
  }

  /**
   * A Performer implements this interface in order to commit new {@link Plan Plans}.
   *
   * <p> The Performer should call {@link PlanEmitter#emit(Plan)} to add new plans.
   */
  public interface ComposablePerformance extends BasePerformance {

    /**
     * Called by the {@link Scheduler} to supply the {@link Performer} with a {@link PlanEmitter}.
     */
    void setPlanEmitter(PlanEmitter planEmitter);

    /**
     * A plan emitter allows an object to emit new plans to a backing scheduler for the target to
     * which the performer is associated.
     */
    interface PlanEmitter {

      /**
       * Emit a new plan. The plan will immediately be added to the backing scheduler.
       */
      void emit(Plan plan);
    }
  }
}
