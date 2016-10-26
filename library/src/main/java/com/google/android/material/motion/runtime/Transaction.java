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

import android.support.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A Transaction aggregates requests for {@link Plan Plans} to be assigned to targets. It can then
 * be committed to a {@link Scheduler}.
 *
 * <p>
 * Add Plans to this Transaction by calling {@link #addPlan(Plan, Object)}.
 * Add and remove named Plans by calling {@link #addNamedPlan(Plan, String, Object)} and
 * {@link #removeNamedPlan(String, Object)}. Named Plans overwrite previously added Plans with the
 * same name.
 *
 * <p>
 * Commit this Transaction to a Scheduler by passing this into
 * {@link Scheduler#commitTransaction(Transaction)}.
 *
 * @deprecated 3.0.0 Plans should be added directly to the Scheduler instead of using Transactions.
 * @see <a href="https://material-motion.gitbooks.io/material-motion-starmap/content/specifications/runtime/transaction.html">The Transaction specification</a>
 */
public final class Transaction {
  private final List<PlanInfo> orderedPlans = new ArrayList<>();
  private final Set<PlanInfo> namedPlans = new HashSet<>();

  /**
   * Adds a {@link Plan} to this Transaction, targeting the given object.
   * @deprecated 2.0.0. Plans should be added directly to the Scheduler instead of using Transactions. <br />
   *              This will be removed in the next version of the runtime <br />
   *              use {@link com.google.android.material.motion.runtime.Scheduler#addPlan(Plan, Object)} on the Scheduler instead
   */
  @Deprecated
  public void addPlan(Plan plan, Object target) {
    PlanInfo info = new PlanInfo();
    info.target = target;
    info.plan = plan.clone();

    orderedPlans.add(info);
  }

  /**
   * Adds a named {@link Plan} to this Transaction, targeting the given object. The Plan overwrites any
   * previously added Plans with the same name.
   * @deprecated 3.0.0. Named Plans should be added directly to the Scheduler instead of using Transactions. <br />
   *              use {@link com.google.android.material.motion.runtime.Scheduler#addNamedPlan(NamedPlan, String, Object)} on the Scheduler instead
   */
  @Deprecated
  public void addNamedPlan(Plan plan, String name, Object target) {
    PlanInfo info = removeNamedPlanInternal(name, target);
    info.plan = plan.clone();

    orderedPlans.add(info);
    namedPlans.add(info);
  }

  /**
   * Removes a named {@link Plan} with the given name from this Transaction.
   * @deprecated 3.0.0. Named Plans should be removed directly from the Scheduler instead of using Transactions. <br />
   *              use {@link com.google.android.material.motion.runtime.Scheduler#removeNamedPlan(String, Object)} on the Scheduler instead
   */
  @Deprecated
  public void removeNamedPlan(String name, Object target) {
    removeNamedPlanInternal(name, target);
  }

  private PlanInfo removeNamedPlanInternal(String name, Object target) {
    PlanInfo info = new PlanInfo();
    info.target = target;
    info.name = name;

    if (namedPlans.remove(info)) {
      orderedPlans.remove(info);
    }

    return info;
  }

  /**
   * Enumerates the {@link Plan} in this Transaction after all add and remove operations have been
   * processed.
   */
  List<PlanInfo> getPlans() {
    return orderedPlans;
  }

  /**
   * A holder for a {@link Plan} and metadata. {@link #hashCode()} and {@link #equals(Object)} has
   * been implemented such that two named Plans are equal iff they have the same target and name.
   * This allows {@link Transaction#namedPlans} to be a {@link Set} where two named Plans cannot
   * coexist if they have matching targets and names.
   */
  static class PlanInfo {

    Object target;
    @Nullable String name;
    Plan plan;

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }

      PlanInfo planInfo = (PlanInfo) o;

      if (!target.equals(planInfo.target)) {
        return false;
      }
      return name != null ? name.equals(planInfo.name) : planInfo.name == null;
    }

    @Override
    public int hashCode() {
      int result = target.hashCode();
      result = 31 * result + (name != null ? name.hashCode() : 0);
      return result;
    }
  }
}
