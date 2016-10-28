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

import static com.google.android.material.motion.runtime.Scheduler.CONTINUOUS_DETAILED_STATE_FLAG;
import static com.google.android.material.motion.runtime.Scheduler.MANUAL_DETAILED_STATE_FLAG;

import android.support.v4.util.SimpleArrayMap;
import com.google.android.material.motion.runtime.Performer.PerformerInstantiationException;
import com.google.android.material.motion.runtime.PerformerFeatures.BasePerformance;
import com.google.android.material.motion.runtime.PerformerFeatures.ComposablePerformance;
import com.google.android.material.motion.runtime.PerformerFeatures.ComposablePerformance.TransactionEmitter;
import com.google.android.material.motion.runtime.PerformerFeatures.ContinuousPerformance;
import com.google.android.material.motion.runtime.PerformerFeatures.ContinuousPerformance.IsActiveToken;
import com.google.android.material.motion.runtime.PerformerFeatures.ContinuousPerformance.IsActiveTokenGenerator;
import com.google.android.material.motion.runtime.PerformerFeatures.ManualPerformance;
import com.google.android.material.motion.runtime.PerformerFeatures.NamedPlanPerformance;
import com.google.android.material.motion.runtime.PlanFeatures.BasePlan;
import com.google.android.material.motion.runtime.PlanFeatures.NamedPlan;
import com.google.android.material.motion.runtime.Scheduler.State;
import com.google.android.material.motion.runtime.Transaction.PlanInfo;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * A helper class for {@link Scheduler} that scopes {@link Performer} instances by target.
 *
 * <p> Ensures only a single instance of Performer is created for each type of Performer required by
 * a target.
 */
class TargetScope {

  private final SimpleArrayMap<Class<? extends BasePerformance>, BasePerformance> cache =
    new SimpleArrayMap<>();
  private final SimpleArrayMap<String, NamedPlanPerformance> namedCache = new SimpleArrayMap<>();

  private final Set<ManualPerformance> activeManualPerformances = new HashSet<>();

  private final SimpleArrayMap<ContinuousPerformance, Set<IsActiveToken>>
    activeContinuousPerformances = new SimpleArrayMap<>();

  private final Scheduler scheduler;

  TargetScope(Scheduler scheduler) {
    this.scheduler = scheduler;
  }

  void commitPlan(PlanInfo plan) {
    BasePerformance performer = getPerformer(plan);

    if (performer instanceof ManualPerformance) {
      activeManualPerformances.add((ManualPerformance) performer);
      notifyTargetStateChanged();
    }

    if (performer instanceof ComposablePerformance) {
      ((ComposablePerformance) performer).setTransactionEmitter(transactionEmitter);
    }

    performer.addPlan(plan.plan);
  }

  void commitAddNamedPlan(NamedPlan plan, String name, Object target) {
    // remove first
    commitRemoveNamedPlan(name);

    // then add
    NamedPlanPerformance namedPlanPerformance = namedCache.get(name);
    if (namedPlanPerformance == null) {
      // TODO: refactor getPerformer() and use it here
      namedPlanPerformance = (NamedPlanPerformance) createPerformer(plan, target);
    }
    namedPlanPerformance.addPlan(plan, name);

    namedCache.put(name, namedPlanPerformance);
  }

  void commitRemoveNamedPlan(String name) {
    NamedPlanPerformance performer = namedCache.get(name);
    if (performer != null) {
      performer.removePlan(name);
    }
    namedCache.remove(name);
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
    if (!activeContinuousPerformances.isEmpty()) {
      state |= CONTINUOUS_DETAILED_STATE_FLAG;
    }
    return state;
  }

  private BasePerformance getPerformer(PlanInfo plan) {
    Class<? extends BasePerformance> performerClass = plan.plan.getPerformerClass();
    BasePerformance performer = cache.get(performerClass);

    if (performer == null) {
      performer = createPerformer(plan.plan, plan.target);
      cache.put(performerClass, performer);
    }

    return performer;
  }

  private BasePerformance createPerformer(BasePlan plan, Object target) {
    Class<? extends BasePerformance> performerClass = plan.getPerformerClass();

    try {
      BasePerformance performer = performerClass.newInstance();
      performer.initialize(target);

      if (performer instanceof ContinuousPerformance) {
        ContinuousPerformance continuousPerformance = (ContinuousPerformance) performer;
        continuousPerformance
          .setIsActiveTokenGenerator(createIsActiveTokenGenerator(continuousPerformance));
      }

      if (performer.getClass() != performerClass) {
        throw new IllegalStateException(
          "#createPerformer returned wrong type. Expected " + performerClass.getName());
      }

      return performer;
    } catch (InstantiationException e) {
      throw new PerformerInstantiationException(performerClass, e);
    } catch (IllegalAccessException e) {
      throw new PerformerInstantiationException(performerClass, e);
    }
  }

  /**
   * Creates a {@link IsActiveTokenGenerator} to be assigned to the given {@link
   * ContinuousPerformance}.
   */
  private IsActiveTokenGenerator createIsActiveTokenGenerator(
    final ContinuousPerformance performer) {
    return new IsActiveTokenGenerator() {
      @Override
      public IsActiveToken generate() {
        final Set<IsActiveToken> tokens;

        if (activeContinuousPerformances.containsKey(performer)) {
          tokens = activeContinuousPerformances.get(performer);
        } else {
          tokens = new HashSet<>();
          activeContinuousPerformances.put(performer, tokens);
        }

        IsActiveToken token = new IsActiveToken() {
          @Override
          public void terminate() {
            boolean modified = tokens.remove(this);
            if (!modified) {
              throw new IllegalStateException("IsActiveToken already terminated.");
            }

            if (tokens.isEmpty()) {
              activeContinuousPerformances.remove(performer);
            }
            notifyTargetStateChanged();
          }
        };
        tokens.add(token);

        notifyTargetStateChanged();
        return token;
      }
    };
  }

  private final TransactionEmitter transactionEmitter = new TransactionEmitter() {
    @Override
    public void emit(Transaction transaction) {
      scheduler.commitTransaction(transaction);
    }
  };
}
