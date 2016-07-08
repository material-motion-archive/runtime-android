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

/**
 * <h1>Material Motion Android Runtime package.</h1>
 *
 * <p>
 * API documentation for the <a href="https://github.com/material-motion/material-motion-runtime-android">Material Motion Android Runtime library</a>.
 *
 * <p>
 * This package defines two important abstract classes:
 * <ul>
 *   <li>{@link com.google.android.material.motion.runtime.Plan}</li>
 *   <li>{@link com.google.android.material.motion.runtime.Performer}</li>
 * </ul>
 * and two concrete classes:
 * <ul>
 *   <li>{@link com.google.android.material.motion.runtime.Transaction}</li>
 *   <li>{@link com.google.android.material.motion.runtime.Scheduler}</li>
 * </ul>
 *
 * Learn more about these APIs by reading our <a href="https://material-motion.gitbooks.io/material-motion-starmap/content/specifications/runtime/">Starmap</a>.
 *
 * <h2>Basic Usage</h2>
 *
 * Let’s say we want to describe a view as fading out.
 *
 * <h3>Step 1: Define the Plan</h3>
 *
 * <p>
 * First, we define a new Plan class.
 *
 * <p>
 * In FadeOutPlan.java;
 *
 * <pre>{@code
 * public class FadeOutPlan extends Plan {
 *   @Override
 *   public Class<? extends Performer> getPerformerClass() {
 *     return FadeOutPerformer.class;
 *   }
 * }}</pre>
 *
 * We map the Plan to the Performer by implementing {@link com.google.android.material.motion.runtime.Plan#getPerformerClass()}.
 *
 * <h3>Step 2: Define the Performer</h3>
 *
 * <p>
 * We now define the Performer class that will fulfill our FadeOutPlan.
 *
 * <p>
 * In FadeOutPerformer.java;
 *
 * <pre>{@code
 * public class FadeOutPerformer extends Performer implements PlanPerformance, DelegatedPerformance {
 *
 *   private DelegatedPerformanceCallback callback;
 *
 *   @Override
 *   public void setDelegatedPerformanceCallback(DelegatedPerformanceCallback callback) {
 *     this.callback = callback;
 *   }
 *
 *   @Override
 *   public void addPlan(Plan plan) {
 *     FadeOutPlan fadeOutPlan = (FadeOutPlan) plan;
 *     View target = getTarget();
 *
 *     // Delegate the animation to another API.
 *     target.animate().alpha(0f).setListener(
 *       new AnimatorListenerAdapter() {
 *         @Override
 *         public void onAnimationStart(Animator animation) {
 *           callback.onDelegatedPerformanceStart(FadeOutPerformer.this, "fadeOut");
 *         }
 *         @Override
 *         public void onAnimationEnd(Animator animation) {
 *           callback.onDelegatedPerformanceEnd(FadeOutPerformer.this, "fadeOut");
 *         }
 *       });
 *   }
 * }}</pre>
 *
 * <p>
 * Note: The implementation shown is only one of many ways such a performer could be implemented.
 *
 * <h3>Step 4: Associate Plans with views</h3>
 *
 * <p>
 * Create and keep a reference to a Scheduler:
 *
 * <pre>{@code
 * Scheduler scheduler = new Scheduler();}</pre>
 *
 * <p>
 * Create a Transaction. Plans are added to Transactions. Transactions are committed to Schedulers.
 *
 * <pre>{@code
 * Transaction transaction = new Transaction();
 * transaction.addPlan(new FadeOutPlan(), view);}</pre>
 *
 * <p>
 * Commit the Transaction to the Scheduler.
 *
 * <pre>{@code
 * scheduler.commitTransaction(transaction);}</pre>
 *
 * <h3>Step 5: Test it!</h3>
 *
 * <p>
 * The view should now fade out.
 *
 * <h2>Next steps</h2>
 *
 * <ul>
 *   <li>Create your own family of Plans/Performers.</li>
 * </ul>
 */
package com.google.android.material.motion.runtime;
