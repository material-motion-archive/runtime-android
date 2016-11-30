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
import android.util.JsonReader;
import android.util.JsonWriter;

import com.google.android.material.motion.runtime.PerformerFeatures.BasePerforming;
import com.google.android.material.motion.runtime.PerformerFeatures.NamedPlanPerforming;

/**
 * Defines the APIs that a {@link Plan} can implement.
 */
public final class PlanFeatures {

  @VisibleForTesting
  PlanFeatures() {
    throw new UnsupportedOperationException();
  }

  /**
   * Defines the base functionality for {@link Plan}s. You should not have to implement this
   * interface yourself.
   */
  public interface BasePlan extends Cloneable {

    /**
     * @return The {@link Class} of the {@link Performer} that can fulfill this plan.
     */
    Class<? extends BasePerforming> getPerformerClass();

    BasePlan clone();
  }

  /**
   * Plans should implement this interface if it wants to support the named plan API.
   * <p>
   * A named plan is a {@link Plan} whose performer supports adding and remove the plan by
   * name. Register a named plan by calling {@link MotionRuntime#addNamedPlan(NamedPlan, String,
   * Object)}, and remove it by calling {@link MotionRuntime#removeNamedPlan(String, Object)}.
   * <p>
   * A named plan or family of named plans enables fine configuration of a performer's
   * behavior.
   *
   * @see <a href="https://material-motion.gitbooks.io/material-motion-starmap/content/specifications/runtime/named-plans.html">The
   * Named Plan specificiation</a> for more details.
   */
  public interface NamedPlan extends BasePlan {

    /**
     * @return The {@link Class} of the {@link NamedPlanPerforming} that can fulfill this plan.
     */
    @Override
    Class<? extends NamedPlanPerforming> getPerformerClass();
  }

  /**
   * Plans should implement this interface if it wants to support the serialize API.
   * <p>
   * Serializable Plans can be sent over a wire or recorded to disk.
   */
  public interface SerializablePlan extends BasePlan {

    /**
     * Serializes the Plan into JSON.
     *
     * @param writer Writer to record serialized JSON to.
     */
    void toJson(JsonWriter writer);

    /**
     * Deserializes the Plan from JSON and populates this Plan's fields.
     *
     * @param reader Reader to consume serialized JSON from.
     */
    void fromJson(JsonReader reader);
  }
}
