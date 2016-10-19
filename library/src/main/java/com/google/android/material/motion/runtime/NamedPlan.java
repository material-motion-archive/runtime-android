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

/**
 * Similar to {@link Plan} in that a named plan is an object representing what you want something to do.
 * {@link NamedPlan} is a simple marker interface which extends from {@link Plan}. It does not include any specifics for how named plans are represented in the system.
 * Like {@link Plan}s, named plans use {@link Performer}s to fulfill themselves.
 * See {@link Performer.NamedPlanPerformance} for the named plan specific callbacks.
 *
 * @see <a href="https://material-motion.gitbooks.io/material-motion-starmap/content/specifications/runtime/named-plans.html">The Named Plan specificiation</a> for more details.
 */
public abstract class NamedPlan extends Plan {
  
}
