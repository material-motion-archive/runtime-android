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

/**
 * Defines the APIs that a {@link Plan} can implement.
 */
public final class PlanFeatures {

  @VisibleForTesting
  PlanFeatures() {
    throw new UnsupportedOperationException();
  }

  /**
   * Plans should implement this interface if it wants to support the serialize API.
   * <p>
   * Serializable Plans can be sent over a wire or recorded to disk.
   */
  public interface SerializablePlan {

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
