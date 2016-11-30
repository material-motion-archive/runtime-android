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

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.mock;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import com.google.android.material.motion.runtime.Performer.PerformerInstantiationException;
import com.google.android.material.motion.runtime.PerformerFeatures.BasePerforming;
import com.google.android.material.motion.runtime.PerformerFeatures.ContinuousPerforming;
import com.google.android.material.motion.runtime.PerformerFeatures.ContinuousPerforming.IsActiveToken;
import com.google.android.material.motion.runtime.PerformerFeatures.ContinuousPerforming.IsActiveTokenGenerator;
import com.google.android.material.motion.runtime.PerformerFeatures.ManualPerforming;
import com.google.android.material.motion.runtime.PlanFeatures.BasePlan;
import com.google.android.material.motion.runtime.Runtime.State;
import com.google.android.material.motion.runtime.testing.StepChoreographer;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21)
public class TargetScopeTests {

  /**
   * One frame in ms.
   */
  private static final float FRAME = 16;
  private Runtime runtime;
  private View target;

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Before
  public void setUp() {
    runtime = new Runtime();
    runtime.choreographer = new StepChoreographer();
    Context context = Robolectric.setupActivity(Activity.class);
    target = new View(context);
  }

  @Test
  public void manualPerformerIdleChangesRuntimeState() {
    TargetScope targetScope = new TargetScope(runtime);

    // Runtime starts as idle.
    assertThat(runtime.getState()).isEqualTo(Runtime.IDLE);

    targetScope.commitPlan(new ManualPlan(Runtime.IDLE), target);

    // Runtime becomes active when a manual performer exists.
    assertThat(runtime.getState()).isEqualTo(Runtime.ACTIVE);

    targetScope.update(FRAME);

    // Runtime becomes idle when manual performer is idle.
    assertThat(runtime.getState()).isEqualTo(Runtime.IDLE);
  }

  @Test(expected = PerformerInstantiationException.class)
  public void privatePerformerThrowsException() {
    TargetScope targetScope = new TargetScope(runtime);

    targetScope.commitPlan(new PrivatePlan(), target);
  }

  @Test(expected = PerformerInstantiationException.class)
  public void oneArgConstructorPerformerThrowsException() {
    TargetScope targetScope = new TargetScope(runtime);

    targetScope.commitPlan(new OneArgConstructorPlan(), target);
  }

  @Test
  public void canRequestMultipleTokensForSamePerformer() {
    TargetScope targetScope = new TargetScope(runtime);
    ContinuousPerforming performer = mock(ContinuousPerforming.class);

    IsActiveTokenGenerator generator = targetScope.createIsActiveTokenGenerator(performer);

    IsActiveToken token1 = generator.generate();
    IsActiveToken token2 = generator.generate();// Should not crash.

    token1.terminate();
    token2.terminate();
  }

  @Test
  public void canNotTerminateTokenMultipleTimes() {
    TargetScope targetScope = new TargetScope(runtime);
    ContinuousPerforming performer = mock(ContinuousPerforming.class);

    IsActiveTokenGenerator generator = targetScope.createIsActiveTokenGenerator(performer);
    IsActiveToken token = generator.generate();

    token.terminate();

    thrown.expect(IllegalStateException.class);
    token.terminate();
  }

  private static class ManualPlan extends Plan {

    @State
    private int state;

    private ManualPlan(@State int state) {
      this.state = state;
    }

    @Override
    public Class<? extends BasePerforming> getPerformerClass() {
      return ManualPerformer.class;
    }
  }

  public static class ManualPerformer extends Performer implements ManualPerforming {

    @State
    int state;

    @Override
    public int update(float deltaTimeMs) {
      return state;
    }

    @Override
    public void addPlan(BasePlan plan) {
      state = ((ManualPlan) plan).state;
    }
  }

  private static class PrivatePlan extends Plan {

    @Override
    public Class<? extends BasePerforming> getPerformerClass() {
      return PrivatePerformer.class;
    }
  }

  private static class PrivatePerformer extends Performer {

    @Override
    public void addPlan(BasePlan plan) {
    }
  }

  private static class OneArgConstructorPlan extends Plan {

    @Override
    public Class<? extends BasePerforming> getPerformerClass() {
      return OneArgConstructorPerformer.class;
    }
  }

  public static class OneArgConstructorPerformer extends Performer {

    public OneArgConstructorPerformer(Object param) {
    }

    @Override
    public void addPlan(BasePlan plan) {
    }
  }
}
