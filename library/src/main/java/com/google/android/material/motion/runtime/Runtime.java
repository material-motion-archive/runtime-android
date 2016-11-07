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

import android.support.annotation.IntDef;
import android.support.annotation.VisibleForTesting;
import android.support.v4.util.SimpleArrayMap;
import android.util.Log;

import com.google.android.material.motion.runtime.ChoreographerCompat.FrameCallback;
import com.google.android.material.motion.runtime.PerformerFeatures.ContinuousPerforming;
import com.google.android.material.motion.runtime.PerformerFeatures.ManualPerforming;
import com.google.android.material.motion.runtime.PlanFeatures.NamedPlan;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * The Material Motion runtime accepts {@link Plan Plans} and creates {@link Performer Performers}.
 * The runtime generates relevant events for Performers and {@link StateListener listeners} and
 * monitors {@link State}.
 *
 * <p> Commit Plans to the runtime by calling {@link #addPlan(Plan, Object)}. A runtime ensures that
 * only one {@link Performer} instance is created for each type of Performer required by a target.
 * This allows multiple {@link Plan Plans} to affect a single Performer instance. The Performers can
 * then maintain state across multiple Plans.
 *
 * <p> Query the State of the runtime by calling {@link #getState()}. The runtime is active if any
 * of its Performers are active. To listen for state changes, attach listeners via {@link
 * #addStateListener(StateListener)}.
 *
 * <p> The runtime correctly handles all the interfaces defined in {@link PlanFeatures} and {@link
 * PerformerFeatures}.
 *
 * @see <a href="https://material-motion.gitbooks.io/material-motion-starmap/content/specifications/runtime/runtime.html">The
 * runtime specification</a>
 */
public final class Runtime {

  /**
   * A listener that receives callbacks when the {@link Runtime}'s {@link State} changes.
   */
  public interface StateListener {

    /**
     * Notifies the {@link State} change of the {@link Runtime}.
     */
    void onStateChange(Runtime runtime, @State int newState);
  }

  /**
   * An idle {@link State}, signifying no active {@link Performer Performers}.
   */
  public static final int IDLE = 0;
  /**
   * An active {@link State}, signifying one or more active {@link Performer Performers}.
   */
  public static final int ACTIVE = 1;

  /**
   * The state of a {@link Runtime}.
   */
  @IntDef({IDLE, ACTIVE})
  @Retention(RetentionPolicy.SOURCE)
  public @interface State {

  }

  private static final String TAG = "Runtime";
  /**
   * Flag for detailed state bitmask specifying that the activity originates from a {@link
   * ManualPerforming}.
   */
  static final int MANUAL_DETAILED_STATE_FLAG = 1 << 0;
  /**
   * Flag for detailed state bitmask specifying that the activity originates from a {@link
   * ContinuousPerforming}.
   */
  static final int CONTINUOUS_DETAILED_STATE_FLAG = 1 << 1;

  @VisibleForTesting
  static ChoreographerCompat choreographer = ChoreographerCompat.getInstance();

  private final CopyOnWriteArraySet<StateListener> listeners = new CopyOnWriteArraySet<>();
  private final ManualPerformingFrameCallback manualPerformingFrameCallback =
    new ManualPerformingFrameCallback();

  private final SimpleArrayMap<Object, TargetScope> targets = new SimpleArrayMap<>();
  private final Set<TargetScope> activeManualPerformerTargets = new HashSet<>();
  private final Set<TargetScope> activeContinuousPerformerTargets = new HashSet<>();

  private final List<Tracing> tracings = new ArrayList<>();

  /**
   * @return The current {@link State} of the runtime.
   */
  @State
  public int getState() {
    return getDetailedState() == 0 ? IDLE : ACTIVE;
  }

  /**
   * Returns the detailed state of the runtime, which includes information on the type of {@link
   * Performer} that affects this state.
   *
   * @return A bitmask representing the detailed state of the runtime.
   */
  private int getDetailedState() {
    int state = 0;
    if (!activeManualPerformerTargets.isEmpty()) {
      state |= MANUAL_DETAILED_STATE_FLAG;
    }
    if (!activeContinuousPerformerTargets.isEmpty()) {
      state |= CONTINUOUS_DETAILED_STATE_FLAG;
    }
    return state;
  }

  /**
   * Adds a {@link StateListener} to be notified of the runtime's {@link State} changes.
   */
  public void addStateListener(StateListener listener) {
    if (!listeners.contains(listener)) {
      listeners.add(listener);
    }
  }

  /**
   * Removes a {@link StateListener} from the runtime's {@link State} changes.
   */
  public void removeStateListener(StateListener listener) {
    listeners.remove(listener);
  }

  /**
   * Adds a plan to the runtime.
   *
   * @param plan the {@link Plan} to add to the runtime.
   * @param target the target on which the plan will operate.
   */
  public void addPlan(Plan plan, Object target) {
    getTargetScope(target).commitPlan(plan.clone(), target);
  }

  /**
   * Adds a {@link NamedPlan} to the runtime. When this method is invoked, a {@link NamedPlan} with
   * the same name and target is removed from the runtime before the plan is eventually added.
   *
   * @param plan the {@link NamedPlan} to add to the runtime.
   * @param name the name by which this plan can be identified.
   * @param target the target on which the plan will operate.
   */
  public void addNamedPlan(NamedPlan plan, String name, Object target) {
    if (name == null || name.isEmpty()) {
      throw new IllegalArgumentException("A NamedPlan must have a non-empty name.");
    }
    getTargetScope(target).commitAddNamedPlan((NamedPlan) plan.clone(), name, target);
  }

  /**
   * Removes a {@link NamedPlan} from the runtime.
   *
   * @param name the name by which the named plan can be identified.
   * @param target the target on which the named plan was added.
   */
  public void removeNamedPlan(String name, Object target) {
    if (name == null || name.isEmpty()) {
      throw new IllegalArgumentException("A NamedPlan must have a non-empty name.");
    }
    getTargetScope(target).commitRemoveNamedPlan(name, target);
  }

  /**
   * Adds a {@link Tracing} instance to the runtime.
   *
   * @param tracer the tracer to add.
   */
  public void addTracer(Tracing tracer) {
    if (!tracings.contains(tracer)) {
      tracings.add(tracer);
    }
  }

  /**
   * Removes a {@link Tracing} instance from the runtime.
   *
   * @param tracer the tracer to remove.
   */
  public void removeTracer(Tracing tracer) {
    tracings.remove(tracer);
  }

  /**
   * Retrieves a collection of currently active tracings which have been added to the runtime.
   *
   * @return a {@link List} of {@link Tracing}s which are associated with the runtime.
   */
  List<Tracing> getTracings() {
    return tracings;
  }

  private TargetScope getTargetScope(Object target) {
    TargetScope targetScope = targets.get(target);

    if (targetScope == null) {
      targetScope = new TargetScope(this);
      targets.put(target, targetScope);
    }

    return targetScope;
  }

  /**
   * Notifies the runtime that a {@link TargetScope}'s detailed state may or may not have changed.
   */
  void setTargetState(TargetScope target, int targetDetailedState) {
    int oldDetailedState = getDetailedState();

    if (isSet(targetDetailedState, MANUAL_DETAILED_STATE_FLAG)) {
      activeManualPerformerTargets.add(target);
    } else {
      activeManualPerformerTargets.remove(target);
    }

    if (isSet(targetDetailedState, CONTINUOUS_DETAILED_STATE_FLAG)) {
      activeContinuousPerformerTargets.add(target);
    } else {
      activeContinuousPerformerTargets.remove(target);
    }

    int newDetailedState = getDetailedState();
    if (oldDetailedState != newDetailedState) {
      onDetailedStateChange(oldDetailedState, newDetailedState);
    }
  }

  private void onDetailedStateChange(int oldDetailedState, int newDetailedState) {
    if (changed(oldDetailedState, newDetailedState, MANUAL_DETAILED_STATE_FLAG)) {
      if (isSet(newDetailedState, MANUAL_DETAILED_STATE_FLAG)) {
        Log.d(TAG, "Manual performing TargetScopes now active.");
        manualPerformingFrameCallback.start();
      } else {
        Log.d(TAG, "Manual performing TargetScopes now idle.");
        manualPerformingFrameCallback.stop();
      }
    }
    if (changed(oldDetailedState, newDetailedState, CONTINUOUS_DETAILED_STATE_FLAG)) {
      if (isSet(newDetailedState, CONTINUOUS_DETAILED_STATE_FLAG)) {
        Log.d(TAG, "Continuous performing TargetScopes now active.");
      } else {
        Log.d(TAG, "Continuous performing TargetScopes now idle.");
      }
    }

    if ((oldDetailedState == 0) != (newDetailedState == 0)) {
      @State int state = newDetailedState == 0 ? IDLE : ACTIVE;
      Log.d(TAG, "Runtime state now: " + state);
      for (StateListener listener : listeners) {
        listener.onStateChange(this, state);
      }
    }
  }

  /**
   * Returns whether a flag bit on one bitmask differs from that on another bitmask.
   *
   * @param oldDetailedState The old bitmask.
   * @param newDetailedState The new bitmask.
   * @param flag The flag bit to check for a change.
   */
  private static boolean changed(int oldDetailedState, int newDetailedState, int flag) {
    return (oldDetailedState & flag) != (newDetailedState & flag);
  }

  /**
   * Returns whether a flag bit is set on a bitmask.
   *
   * @param detailedState The bitmask.
   * @param flag The flag bit to check if is set.
   */
  private static boolean isSet(int detailedState, int flag) {
    return (detailedState & flag) != 0;
  }

  /**
   * A {@link FrameCallback} that calls {@link ManualPerforming#update(float)} on each frame for
   * every active {@link ManualPerforming manual performer}.
   */
  private class ManualPerformingFrameCallback extends FrameCallback {

    private double lastTimeMs = 0.0;

    public void start() {
      lastTimeMs = 0.0;
      choreographer.postFrameCallback(this);
    }

    public void stop() {
      choreographer.removeFrameCallback(this);
    }

    @Override
    public void doFrame(long frameTimeNanos) {
      for (TargetScope activeTarget : activeManualPerformerTargets) {
        double frameTimeMs = frameTimeNanos / 1000;
        float deltaTimeMs = lastTimeMs == 0.0 ? 0f : (float) (frameTimeMs - lastTimeMs);

        activeTarget.update(deltaTimeMs);
      }
    }
  }
}
