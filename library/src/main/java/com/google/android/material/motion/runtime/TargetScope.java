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

import android.support.annotation.VisibleForTesting;
import android.support.v4.util.SimpleArrayMap;

import com.google.android.material.motion.runtime.MotionRuntime.State;
import com.google.android.material.motion.runtime.Performer.PerformerInstantiationException;
import com.google.android.material.motion.runtime.PerformerFeatures.ComposablePerforming;
import com.google.android.material.motion.runtime.PerformerFeatures.ComposablePerforming.PlanEmitter;
import com.google.android.material.motion.runtime.PerformerFeatures.ContinuousPerforming;
import com.google.android.material.motion.runtime.PerformerFeatures.ContinuousPerforming.IsActiveToken;
import com.google.android.material.motion.runtime.PerformerFeatures.ContinuousPerforming.IsActiveTokenGenerator;
import com.google.android.material.motion.runtime.PerformerFeatures.ManualPerforming;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import static com.google.android.material.motion.runtime.MotionRuntime.CONTINUOUS_DETAILED_STATE_FLAG;
import static com.google.android.material.motion.runtime.MotionRuntime.MANUAL_DETAILED_STATE_FLAG;

/**
 * A helper class for {@link MotionRuntime} that scopes {@link Performer} instances by target.
 * <p>
 * Ensures only a single instance of Performer is created for each type of Performer required by a
 * target.
 */
class TargetScope<T> {

  private final SimpleArrayMap<Class<? extends Performer<T>>, Performer<T>> cache = new SimpleArrayMap<>();
  private final SimpleArrayMap<String, NamedPerformer<T>> namedCache = new SimpleArrayMap<>();

  private final Set<ManualPerforming> activeManualPerformers = new HashSet<>();

  private final SimpleArrayMap<ContinuousPerforming, Set<IsActiveToken>>
    activeContinuousPerformers = new SimpleArrayMap<>();

  private final MotionRuntime runtime;

  TargetScope(MotionRuntime runtime) {
    this.runtime = runtime;
  }

  void commitPlan(Plan<T> plan, T target) {
    Performer<T> performer = commitPlanInternal(plan, target);
    performer.addPlan(plan);

    // notify tracers
    for (Tracing tracer : runtime.getTracers()) {
      tracer.onAddPlan(plan, target);
    }
  }

  void commitAddNamedPlan(NamedPlan<T> plan, String name, T target) {
    // remove first
    commitRemoveNamedPlan(name, target);

    // then add
    NamedPerformer<T> performer = commitPlanInternal(plan, target);
    performer.addPlan(plan, name);
    namedCache.put(name, performer);

    // notify tracers
    for (Tracing tracer : runtime.getTracers()) {
      tracer.onAddNamedPlan(plan, name, target);
    }
  }

  private <P extends Performer<T>> P commitPlanInternal(Plan<T> plan, T target) {
    Performer<T> performer = getPerformer(plan, target);

    if (performer instanceof ManualPerforming) {
      activeManualPerformers.add((ManualPerforming) performer);
      notifyTargetStateChanged();
    }

    //noinspection unchecked
    return (P) performer;
  }

  void commitRemoveNamedPlan(String name, T target) {
    NamedPerformer<T> performer = namedCache.get(name);
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

  private Performer<T> getPerformer(Plan<T> plan, T target) {
    Class<? extends Performer<T>> performerClass = plan.getPerformerClass();
    Performer<T> performer = cache.get(performerClass);

    if (performer == null) {
      performer = createPerformer(plan, target);
      cache.put(performerClass, performer);
    }

    return performer;
  }

  private Performer<T> createPerformer(Plan<T> plan, T target) {
    Class<? extends Performer<T>> performerClass = plan.getPerformerClass();

    //noinspection TryWithIdenticalCatches
    try {
      Performer<T> performer = performerClass.newInstance();
      performer.initialize(target);

      if (performer instanceof ContinuousPerforming) {
        ContinuousPerforming continuousPerformer = (ContinuousPerforming) performer;
        continuousPerformer
          .setIsActiveTokenGenerator(createIsActiveTokenGenerator(continuousPerformer));
      }

      if (performer instanceof ComposablePerforming) {
        //noinspection unchecked
        ComposablePerforming<T> composablePerformer = (ComposablePerforming<T>) performer;
        composablePerformer.setPlanEmitter(createPlanEmitter(performer));
      }

      for (Tracing tracing : runtime.getTracers()) {
        tracing.onCreatePerformer(performer, target);
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
   * Creates a {@link PlanEmitter} to be assigned to the given performer.
   */
  private PlanEmitter<T> createPlanEmitter(final Performer<T> performer) {
    return new PlanEmitter<T>() {
      @Override
      public void emit(Plan<T> plan) {
        runtime.addPlan(plan, performer.getTarget());
      }
    };
  }
}
