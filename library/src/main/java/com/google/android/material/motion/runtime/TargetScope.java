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

import static com.google.android.material.motion.runtime.Scheduler.DELEGATED_DETAILED_STATE_FLAG;
import static com.google.android.material.motion.runtime.Scheduler.MANUAL_DETAILED_STATE_FLAG;

import android.support.v4.util.SimpleArrayMap;
import com.google.android.material.motion.runtime.Performer.ComposablePerformance;
import com.google.android.material.motion.runtime.Performer.ComposablePerformance.ComposablePerformanceCallback;
import com.google.android.material.motion.runtime.Performer.ComposablePerformance.Work;
import com.google.android.material.motion.runtime.Performer.DelegatedPerformance;
import com.google.android.material.motion.runtime.Performer.DelegatedPerformance.DelegatedPerformanceCallback;
import com.google.android.material.motion.runtime.Performer.DelegatedPerformance.DelegatedPerformanceToken;
import com.google.android.material.motion.runtime.Performer.DelegatedPerformance.DelegatedPerformanceTokenCallback;
import com.google.android.material.motion.runtime.Performer.ManualPerformance;
import com.google.android.material.motion.runtime.Performer.PerformerInstantiationException;
import com.google.android.material.motion.runtime.Performer.PlanPerformance;
import com.google.android.material.motion.runtime.Scheduler.State;
import com.google.android.material.motion.runtime.Transaction.PlanInfo;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * A helper class for {@link Scheduler} that scopes {@link Performer} instances by target.
 *
 * <p>
 * Ensures only a single instance of Performer is created for each type of Performer required by a
 * target.
 */
class TargetScope {

  private final SimpleArrayMap<Class<? extends Performer>, Performer> cache =
      new SimpleArrayMap<>();

  private final Set<ManualPerformance> activeManualPerformances = new HashSet<>();

  @Deprecated
  private final SimpleArrayMap<DelegatedPerformance, Set<String>> activeDelegatedPerformances =
      new SimpleArrayMap<>();

  private final SimpleArrayMap<DelegatedPerformance, Set<DelegatedPerformanceToken>>
      activeTokenDelegatedPerformances = new SimpleArrayMap<>();

  private final Scheduler scheduler;

  TargetScope(Scheduler scheduler) {
    this.scheduler = scheduler;
  }

  void commitPlan(PlanInfo plan) {
    Performer performer = getPerformer(plan);

    if (performer instanceof ManualPerformance) {
      activeManualPerformances.add((ManualPerformance) performer);
      notifyTargetStateChanged();
    }

    if (performer instanceof DelegatedPerformance) {
      ((DelegatedPerformance) performer)
        .setDelegatedPerformanceCallback(delegatedPerformanceCallback);
      ((DelegatedPerformance) performer)
        .setDelegatedPerformanceCallback(delegatedPerformanceTokenCallback);
    }

    if (performer instanceof ComposablePerformance) {
      ((ComposablePerformance) performer)
        .setComposablePerformanceCallback(composablePerformanceCallback);
    }

    if (performer instanceof PlanPerformance) {
      ((PlanPerformance) performer).addPlan(plan.plan);
    }
  }

  void update(float deltaTimeMs) {
    Iterator<ManualPerformance> iterator = activeManualPerformances.iterator();

    boolean changed = false;
    while (iterator.hasNext()) {
      ManualPerformance performer = iterator.next();
      @State int state = performer.update(deltaTimeMs);
      if (state == Scheduler.IDLE) {
        iterator.remove();
        changed = true;
      }
    }

    if (changed) {
      notifyTargetStateChanged();
    }
  }

  private void notifyTargetStateChanged() {
    scheduler.setTargetState(this, getDetailedState());
  }

  private int getDetailedState() {
    int state = 0;
    if (!activeManualPerformances.isEmpty()) {
      state |= MANUAL_DETAILED_STATE_FLAG;
    }
    if (!activeDelegatedPerformances.isEmpty() || !activeTokenDelegatedPerformances.isEmpty()) {
      state |= DELEGATED_DETAILED_STATE_FLAG;
    }
    return state;
  }

  private Performer getPerformer(PlanInfo plan) {
    Class<? extends Performer> performerClass = plan.plan.getPerformerClass();
    Performer performer = cache.get(performerClass);

    if (performer == null) {
      performer = createPerformer(plan);
      cache.put(performerClass, performer);
    }

    return performer;
  }

  private Performer createPerformer(PlanInfo plan) {
    Class<? extends Performer> PerformerClass = plan.plan.getPerformerClass();

    try {
      Performer performer = PerformerClass.newInstance();
      performer.initialize(plan.target);

      if (performer.getClass() != PerformerClass) {
        throw new IllegalStateException(
            "#createPerformer returned wrong type. Expected " + PerformerClass.getName());
      }

      return performer;
    } catch (InstantiationException e) {
      throw new PerformerInstantiationException(PerformerClass, e);
    } catch (IllegalAccessException e) {
      throw new PerformerInstantiationException(PerformerClass, e);
    }
  }

  /**
   * The {@link DelegatedPerformanceCallback} assigned to every {@link DelegatedPerformance} in
   * this TargetScope.
   */
  @Deprecated
  private final DelegatedPerformanceCallback delegatedPerformanceCallback =
      new DelegatedPerformanceCallback() {

        @Override
        public void onDelegatedPerformanceStart(DelegatedPerformance performer, String name) {
          Set<String> delegatedNames = activeDelegatedPerformances.get(performer);

          if (delegatedNames == null) {
            delegatedNames = new HashSet<>();
            activeDelegatedPerformances.put(performer, delegatedNames);
          }

          boolean modified = delegatedNames.add(name);
          if (!modified) {
            throw new IllegalArgumentException(
                "Existing delegated performance already active: " + name);
          }

          notifyTargetStateChanged();
        }

        @Override
        public void onDelegatedPerformanceEnd(DelegatedPerformance performer, String name) {
          Set<String> delegatedNames = activeDelegatedPerformances.get(performer);

          boolean modified = delegatedNames.remove(name);
          if (!modified) {
            throw new IllegalArgumentException(
                "Expected delegated performance to be active: " + name);
          }

          if (delegatedNames.isEmpty()) {
            activeDelegatedPerformances.remove(performer);
          }

          notifyTargetStateChanged();
        }
      };

  /**
   * The {@link DelegatedPerformanceTokenCallback} assigned to every {@link DelegatedPerformance} in
   * this TargetScope.
   */
  private final DelegatedPerformanceTokenCallback delegatedPerformanceTokenCallback =
      new DelegatedPerformanceTokenCallback() {
        @Override
        public DelegatedPerformanceToken onDelegatedPerformanceStart(
            DelegatedPerformance performer) {
          Set<DelegatedPerformanceToken> delegatedTokens =
              activeTokenDelegatedPerformances.get(performer);

          if (delegatedTokens == null) {
            delegatedTokens = new HashSet<>();
            activeTokenDelegatedPerformances.put(performer, delegatedTokens);
          }

          DelegatedPerformanceToken token = new DelegatedPerformanceToken();
          delegatedTokens.add(token);

          notifyTargetStateChanged();

          return token;
        }

        @Override
        public void onDelegatedPerformanceEnd(
            DelegatedPerformance performer, DelegatedPerformanceToken token) {
          Set<DelegatedPerformanceToken> delegatedTokens =
              activeTokenDelegatedPerformances.get(performer);

          boolean modified = delegatedTokens.remove(token);
          if (!modified) {
            throw new IllegalArgumentException(
                "Expected delegated performance to be active: " + token);
          }

          if (delegatedTokens.isEmpty()) {
            activeTokenDelegatedPerformances.remove(performer);
          }

          notifyTargetStateChanged();
        }
      };

  private final ComposablePerformanceCallback composablePerformanceCallback =
    new ComposablePerformanceCallback() {
      @Override
      public void transact(Work work) {
        Transaction transaction = new Transaction();
        work.work(transaction);
        scheduler.commitTransaction(transaction);
      }
    };
}
