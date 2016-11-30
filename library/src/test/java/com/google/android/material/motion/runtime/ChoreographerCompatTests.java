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

import com.google.android.material.motion.runtime.ChoreographerCompat.FrameCallback;

import org.junit.Test;
import org.robolectric.Robolectric;

import static com.google.common.truth.Truth.assertThat;

public abstract class ChoreographerCompatTests {

  /**
   * One frame in ms.
   */
  private static final long FRAME = 17;

  @Test
  public void postFrameCallback() {
    ChoreographerCompat choreographer = ChoreographerCompat.getInstance();

    TestFrameCallback callback = new TestFrameCallback();
    choreographer.postFrameCallback(callback);

    assertThat(callback.didFrame).isTrue();
  }

  @Test
  public void postFrameCallbackDelayed() {
    ChoreographerCompat choreographer = ChoreographerCompat.getInstance();

    TestFrameCallback callback = new TestFrameCallback();
    choreographer.postFrameCallbackDelayed(callback, FRAME + 1);

    Robolectric.getForegroundThreadScheduler().advanceBy(FRAME);
    assertThat(callback.didFrame).isFalse();
    Robolectric.getForegroundThreadScheduler().advanceToLastPostedRunnable();
    assertThat(callback.didFrame).isTrue();
  }

  @Test
  public void removeFrameCallback() {
    ChoreographerCompat choreographer = ChoreographerCompat.getInstance();

    TestFrameCallback callback = new TestFrameCallback();
    choreographer.postFrameCallbackDelayed(callback, FRAME + 1);

    Robolectric.getForegroundThreadScheduler().advanceBy(FRAME);
    assertThat(callback.didFrame).isFalse();

    choreographer.removeFrameCallback(callback);

    Robolectric.getForegroundThreadScheduler().advanceBy(FRAME);
    assertThat(callback.didFrame).isFalse();
  }

  private static class TestFrameCallback extends FrameCallback {
    public boolean didFrame = false;

    @Override
    public void doFrame(long frameTimeNanos) {
      didFrame = true;
    }
  }
}
