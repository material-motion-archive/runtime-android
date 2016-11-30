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
package com.google.android.material.motion.runtime.testing;

import android.support.v4.util.SimpleArrayMap;

import com.google.android.material.motion.runtime.ChoreographerCompat;

/**
 * A {@link ChoreographerCompat} implementation for tests. Allows a test to {@link #advance(long)}
 * the choreographer in a controlled manner.
 */
public class StepChoreographer extends ChoreographerCompat {

  /**
   * Represents one frame. 16ms.
   */
  public final static long FRAME_MS = 16L;

  private final SimpleArrayMap<FrameCallback, Long> callbacks = new SimpleArrayMap<>();
  private long frameTimeMs = 0L;

  /**
   * Advance the choreographer for the given milliseconds. Any callbacks scheduled within this
   * period will be invoked and removed.
   */
  public void advance(long millis) {
    frameTimeMs += millis;

    for (int i = 0, count = callbacks.size(); i < count; i++) {
      FrameCallback callback = callbacks.keyAt(i);
      long delay = callbacks.valueAt(i);

      put(callback, delay - millis);
    }
  }

  private void put(FrameCallback callback, long delay) {
    if (delay <= 0) {
      callbacks.remove(callback);
      callback.doFrame(frameTimeMs * 1000);
    } else {
      callbacks.put(callback, delay);
    }
  }

  @Override
  public void postFrameCallback(FrameCallback callback) {
    callbacks.put(callback, FRAME_MS);
  }

  @Override
  public void postFrameCallbackDelayed(FrameCallback callback, long delayMillis) {
    callbacks.put(callback, delayMillis);
  }

  @Override
  public void removeFrameCallback(FrameCallback callback) {
    callbacks.remove(callback);
  }
}
