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

import static com.google.android.material.motion.runtime.MotionRuntime.CONTINUOUS_DETAILED_STATE_FLAG;
import static com.google.android.material.motion.runtime.MotionRuntime.MANUAL_DETAILED_STATE_FLAG;

import android.support.annotation.VisibleForTesting;
import android.support.v4.util.SimpleArrayMap;
import com.google.android.material.motion.runtime.Performer.PerformerInstantiationException;
import com.google.android.material.motion.runtime.PerformerFeatures.BasePerforming;
import com.google.android.material.motion.runtime.PerformerFeatures.ComposablePerforming;
import com.google.android.material.motion.runtime.PerformerFeatures.ComposablePerforming.PlanEmitter;
import com.google.android.material.motion.runtime.PerformerFeatures.ContinuousPerforming;
import com.google.android.material.motion.runtime.PerformerFeatures.ContinuousPerforming.IsActiveToken;
import com.google.android.material.motion.runtime.PerformerFeatures.ContinuousPerforming.IsActiveTokenGenerator;
import com.google.android.material.motion.runtime.PerformerFeatures.ManualPerforming;
import com.google.android.material.motion.runtime.PerformerFeatures.NamedPlanPerforming;
import com.google.android.material.motion.runtime.PlanFeatures.BasePlan;
import com.google.android.material.motion.runtime.PlanFeatures.NamedPlan;
import com.google.android.material.motion.runtime.MotionRuntime.State;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * A helper class for {@link MotionRuntime} that scopes {@link Performer} instances by target.
 *
 * <p> Ensures only a single instance of Performer is created for each type of Performer required by
 * a target.
 */
class TargetScope {

  private final SimpleArrayMap<Class<? extends BasePerforming>, BasePerforming> cache =
    new SimpleArrayMap<>();
  private final SimpleArrayMap<String, NamedPlanPerforming> namedCache = new SimpleArrayMap<>();

  private final Set<ManualPerforming> activeManualPerformers = new HashSet<>();

  private final SimpleArrayMap<ContinuousPerforming, Set<IsActiveToken>>
    activeContinuousPerformers = new SimpleArrayMap<>();

  private final MotionRuntime runtime;

  TargetScope(MotionRuntime runtime) {
    this.runtime = runtime;
  }

  void commitPlan(BasePlan plan, Object target) {
    Performer performer = commitPlanInternal(plan, target);
    performer.addPlan(plan);

    // notify tracers
    for (Tracing tracer : runtime.getTracers()) {
      tracer.onAddPlan((Plan) plan, target);
    }
  }

  void commitAddNamedPlan(NamedPlan plan, String name, Object target) {
    // remove first
    commitRemoveNamedPlan(name, target);

    // then add
    NamedPlanPerforming performer = commitPlanInternal(plan, target);
    performer.addPlan(plan, name);
    namedCache.put(name, performer);

    // notify tracers
    for (Tracing tracer : runtime.getTracers()) {
      tracer.onAddNamedPlan(plan, name, target);
    }
  }

  private <T extends BasePerforming> T commitPlanInternal(BasePlan plan, Object target) {
    BasePerforming performer = getPerformer(plan, target);

    if (performer instanceof ManualPerforming) {
      activeManualPerformers.add((ManualPerforming) performer);
      notifyTargetStateChanged();
    }

    //noinspection unchecked
    return (T) performer;
  }

  void commitRemoveNamedPlan(String name, Object target) {
    NamedPlanPerforming performer = namedCache.get(name);
    if (performer != null) {
      performer.removePlan(name);

      // notify tracers
      for (Tracing tracer : runtime.getTracers()) {
        tracer.onRemoveNamedPlan(name, target);
      }
    }
    namedCache.remove(name);
  }

  void update(float deltaTimeMs) {
    Iterator<ManualPerforming> iterator = activeManualPerformers.iterator();

    boolean changed = false;
    while (iterator.hasNext()) {
      ManualPerforming performer = iterator.next();
      @State int state = performer.update(deltaTimeMs);
      if (state == MotionRuntime.IDLE) {
        iterator.remove();
        changed = true;
      }
    }

    if (changed) {
      notifyTargetStateChanged();
    }
  }

  private void notifyTargetStateChanged() {
    runtime.setTargetState(this, getDetailedState());
  }

  private int getDetailedState() {
    int state = 0;
    if (!activeManualPerformers.isEmpty()) {
      state |= MANUAL_DETAILED_STATE_FLAG;
    }
    if (!activeContinuousPerformers.isEmpty()) {
      state |= CONTINUOUS_DETAILED_STATE_FLAG;
    }
    return state;
  }

  private BasePerforming getPerformer(BasePlan plan, Object target) {
    Class<? extends BasePerforming> performerClass = plan.getPerformerClass();
    BasePerforming performer = cache.get(performerClass);

    if (performer == null) {
      performer = createPerformer(plan, target);
      cache.put(performerClass, performer);
    }

    return performer;
  }

  private BasePerforming createPerformer(BasePlan plan, Object target) {
    Class<? extends BasePerforming> performerClass = plan.getPerformerClass();

    try {
      BasePerforming performer = performerClass.newInstance();
      performer.initialize(target);

      if (performer instanceof ContinuousPerforming) {
        ContinuousPerforming continuousPerformer = (ContinuousPerforming) performer;
        continuousPerformer
          .setIsActiveTokenGenerator(createIsActiveTokenGenerator(continuousPerformer));
      }

      if (performer instanceof ComposablePerforming) {
        ComposablePerforming composablePerformer = (ComposablePerforming) performer;
        composablePerformer.setPlanEmitter(createPlanEmitter(composablePerformer));
      }

      for (Tracing tracing : runtime.getTracers()) {
        tracing.onCreatePerformer((Performer) performer, target);
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
   * ContinuousPerforming}.
   */
  @VisibleForTesting
  IsActiveTokenGenerator createIsActiveTokenGenerator(
    final ContinuousPerforming performer) {
    return new IsActiveTokenGenerator() {
      @Override
      public IsActiveToken generate() {
        final Set<IsActiveToken> tokens;

        if (activeContinuousPerformers.containsKey(performer)) {
          tokens = activeContinuousPerformers.get(performer);
        } else {
          tokens = new HashSet<>();
          activeContinuousPerformers.put(performer, tokens);
        }

        IsActiveToken token = new IsActiveToken() {
          @Override
          public void terminate() {
            boolean modified = tokens.remove(this);
            if (!modified) {
              throw new IllegalStateException("IsActiveToken already terminated.");
            }

            if (tokens.isEmpty()) {
              activeContinuousPerformers.remove(performer);
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

  /**
   * Creates a {@link PlanEmitter} to be assigned to the given {@link ComposablePerforming}.
   */
  private PlanEmitter createPlanEmitter(final ComposablePerforming performer) {
    return new PlanEmitter() {
      @Override
      public void emit(Plan plan) {
        runtime.addPlan(plan, performer.getTarget());
      }
    };
  }
}
