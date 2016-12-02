# Material Motion Android Runtime

[![Build Status](https://travis-ci.org/material-motion/runtime-android.svg?branch=develop)](https://travis-ci.org/material-motion/runtime-android)
[![codecov](https://codecov.io/gh/material-motion/runtime-android/branch/develop/graph/badge.svg)](https://codecov.io/gh/material-motion/runtime-android)
[![Release](https://img.shields.io/github/release/material-motion/runtime-android.svg)](https://github.com/material-motion/runtime-android/releases/latest)
[![Docs](https://img.shields.io/badge/jitpack-docs-green.svg)](https://jitpack.io/com/github/material-motion/runtime-android/stable-SNAPSHOT/javadoc/)

The Material Motion Runtime is a tool for describing motion declaratively.

## Declarative motion: motion as data

This library does not do much on its own. What it does do, however, is enable the expression of
motion as discrete units of data that can be introspected, composed, and sent over a wire.

This library encourages you to describe motion as data, or what we call *plans*. Plans are committed
to the *runtime*. The runtime coordinates the creation of *performers*, objects responsible for
translating plans into concrete execution.

To use the runtime, simply instantiate a `MotionRuntime` object and add a plan.

```
Plan plan;
View target;

MotionRuntime runtime = new MotionRuntime();
runtime.addPlan(plan, target);
```

Learn more about the APIs defined in the library by reading our
[technical documentation](https://jitpack.io/com/github/material-motion/runtime-android/6.0.1/javadoc/) and our
[Starmap](https://material-motion.github.io/material-motion/starmap/).

## Installation

### Installation with Jitpack

Add the Jitpack repository to your project's `build.gradle`:

```gradle
allprojects {
    repositories {
        maven { url "https://jitpack.io" }
    }
}
```

Depend on the [latest version](https://github.com/material-motion/runtime-android/releases) of the library.
Take care to occasionally [check for updates](https://github.com/ben-manes/gradle-versions-plugin).

```gradle
dependencies {
    compile 'com.github.material-motion:runtime-android:6.0.1'
}
```

For more information regarding versioning, see:

- [Material Motion Versioning Policies](https://material-motion.github.io/material-motion/team/essentials/core_team_contributors/release_process#versioning)

### Using the files from a folder local to the machine

You can have a copy of this library with local changes and test it in tandem
with its client project. To add a local dependency on this library, add this
library's identifier to your project's `local.dependencies`:

```
com.github.material-motion:runtime-android
```

> Because `local.dependencies` is never to be checked into Version Control
Systems, you must also ensure that any local dependencies are also defined in
`build.gradle` as explained in the previous section.

**Important**

For each local dependency listed, you *must* run `gradle install` from its
project root every time you make a change to it. That command will publish your
latest changes to the local maven repository. If your local dependencies have
local dependencies of their own, you must `gradle install` them as well.

You must `gradle clean` your project every time you add or remove a local
dependency.

### Usage

How to use the library in your project.

#### Editing the library in Android Studio

Open Android Studio,
choose `File > New > Import`,
choose the root `build.gradle` file.

## Example apps/unit tests

To build the sample application, run the following commands:

    git clone https://github.com/material-motion/runtime-android.git
    cd runtime-android
    gradle installDebug

To run all unit tests, run the following commands:

    git clone https://github.com/material-motion/runtime-android.git
    cd runtime-android
    gradle test

# Guides

1. [Architecture](#architecture)
1. [How to define a new plan and performer type](#how-to-create-a-new-plan-and-performer-type)
1. [How to commit a plan to the runtime](#how-to-commit-a-plan-to-the-runtime)
1. [How to commit a named plan to the runtime](#how-to-commit-a-named-plan-to-the-runtime)
1. [How to configure performers with plans](#how-to-configure-performers-with-plans)
1. [How to configure performers with named plans](#how-to-configure-performers-with-named-plans)
1. [How to use composition to fulfill plans](#how-to-use-composition-to-fulfill-plans)
1. [How to indicate continuous performance](#how-to-indicate-continuous-performance)
1. [How to trace internal runtime events](#how-to-trace-internal-runtime-events)
1. [How to log runtime events to the console](#how-to-log-runtime-events-to-the-console)

## Architecture

The Material Motion Runtime consists of two groups of APIs: a runtime object and a
constellation of protocols loosely consisting of plan and performing types.

### MotionRuntime

The [MotionRuntime](https://jitpack.io/com/github/material-motion/runtime-android/6.0.1/javadoc/index.html?com/google/android/material/motion/runtime/MotionRuntime.html)
object is a coordinating entity whose primary responsibility is to fulfill plans by creating
performers. You can create many runtimes throughout the lifetime of your application. A good rule
of thumb is to have one runtime per interaction or transition.

### Plan + Performing types

The [Plan](https://jitpack.io/com/github/material-motion/runtime-android/6.0.1/javadoc/index.html?com/google/android/material/motion/runtime/Plan.html)
and [Performer](https://jitpack.io/com/github/material-motion/runtime-android/6.0.1/javadoc/index.html?com/google/android/material/motion/runtime/Performer.html)
classes each define the minimal characteristics required for an object to be considered either a
plan or a performer, respectively, by the Material Motion Runtime.

Plans and performers have a symbiotic relationship. A plan is executed by the performer it defines.
Performer behavior is configured by the provided plan instances.

Learn more about the Material Motion Runtime by reading the
[Starmap](https://material-motion.gitbooks.io/material-motion-starmap/content/specifications/runtime/).

## How to create a new plan and performer type

The following steps provide copy-pastable snippets of code.

### Step 1: Define the plan type

Questions to ask yourself when creating a new plan type:

- What do I want my plan/performer to accomplish?
- Will my performer need many plans to achieve the desired outcome?
- How can I name my plan such that it clearly communicates either a **behavior** or a
  **change in state**?

As general rules:

1. Plans with an *-able* suffix alter the **behavior** of the target, often indefinitely. Examples:
   Draggable, Pinchable, Tossable.
2. Plans that are *verbs* describe some **change in state**, often over a period of time. Examples:
   FadeIn, Tween, SpringTo.

```java
public class MyPlan {
}
```

### Step 2: Define the performer type

Performers are responsible for fulfilling plans. Fulfillment is possible in a variety of ways:

- [NamedPerformer](https://jitpack.io/com/github/material-motion/runtime-android/6.0.1/javadoc/index.html?com/google/android/material/motion/runtime/NamedPerformer.html): [How to configure performers with named plans](#how-to-configure-performers-with-named-plans)
- [ContinuousPerforming](https://jitpack.io/com/github/material-motion/runtime-android/6.0.1/javadoc/index.html?com/google/android/material/motion/runtime/PerformerFeatures.ContinuousPerforming.html): [How to indicate continuous performance](#how-to-indicate-continuous-performance)
- [ComposablePerforming](https://jitpack.io/com/github/material-motion/runtime-android/6.0.1/javadoc/index.html?com/google/android/material/motion/runtime/PerformerFeatures.ComposablePerforming.html): [How to use composition to fulfill plans](#how-to-use-composition-to-fulfill-plans)

See the associated links for more details on each performing type.

> Note: only one instance of a type of performer **per target** is ever created. This allows you to
> register multiple plans to the same target in order to configure a performer. See
> [How to configure performers with plans](#how-to-configure-performers-with-plans) for more details.

```java
public class MyPerformer {
}
```

### Step 3: Make the plan type a formal Plan

Conforming to Plan requires:

1. that you choose the type of target your plan applies to,
1. that you define the type of performer your plan requires, and
1. that your plan be Cloneable.

```java
public class MyPlan extends Plan<View> {
  @Override
  public Class<? extends Performer<View>> getPerformerClass() {
    return MyPerformer.class;
  }

  @Override
  public Plan clone() {
    // Only override this method if you need to deep clone reference-typed fields.
    return super.clone();
  }
}
```

### Step 4: Make the performer type a formal Performer

Conforming to Performer requires:

1. that the type of target your performer can act on agrees with the plan, and
1. that you fulfill all plans passed to `addPlan()`.

```java
public class MyPerformer extends Performer<View> {
  @Override
  public void addPlan(Plan<View> plan) {
    View target = getTarget();
  }
}
```

## How to commit a plan to the runtime

### Step 1: Create and store a reference to a runtime instance

```java
public class MyActivity extends Activity {
  private final MotionRuntime runtime = new MotionRuntime();
}
```

### Step 2: Associate plans with targets

```java
Plan<View> plan;
View target;

runtime.addPlan(plan, target);
```

## How to commit a named plan to the runtime

### Step 1: Create and store a reference to a runtime instance

```java
public class MyActivity extends Activity {
  private final MotionRuntime runtime = new MotionRuntime();
}
```

### Step 2: Associate named plans with targets

```java
NamedPlan<View> plan;
String name;
View target;

runtime.addNamedPlan(plan, name, target);
```

## How to configure performers with plans

The `addPlan()` method will be invoked with plans that require use of this performer.

```java
public class MyPerformer extends Performer<View> {
  @Override
  public void addPlan(Plan<View> plan) {
    MyPlan myPlan = (MyPlan) plan;

    // Do something with myPlan.
  }
}
```

***Handling multiple plan types***

```java
public class MyPerformer extends Performer<View> {
  @Override
  public void addPlan(Plan<View> plan) {
    if (plan instanceof Plan1) {
      addPlan1((Plan1) plan);
    } else if (plan instanceof Plan2) {
      addPlan2((Plan2) plan);
    } else {
      throw new IllegalArgumentException("Plan type not supported for " + plan);
    }
  }
}
```

## How to configure performers with named plans

```java
public class MyPerformer extends NamedPerformer<View> {
  @Override
  public void addPlan(NamedPlan<View> plan, String name) {
    MyPlan myPlan = (MyPlan) plan;

    // Do something with myPlan.
  }

  @Override
  public void removePlan(String name) {
    // Remove any configuration associated with the given name.
  }
}
```

## How to use composition to fulfill plans

A composition performer is able to emit new plans using a plan emitter. This feature enables the
reuse of plans and the creation of higher-order abstractions.

### Step 1: Conform to ComposablePerforming and store the plan emitter

```java
public class MyPerformer extends Performer<View> implements ComposablePerforming<View> {
  // Store the emitter in your class' definition.
  private PlanEmitter<View> emitter;

  @Override
  public void setPlanEmitter(PlanEmitter<View> planEmitter) {
    this.emitter = planEmitter;
  }
}
```

### Step 2: Emit plans

Performers are only able to emit plans for their associated target.

```java
PlanEmitter<View> emitter;
Plan<View> plan;

emitter.emit(plan);
```

## How to indicate continuous performance

Performers will often perform their actions over a period of time or while an interaction is
active. These types of performers are called continuous performers.

A continuous performer is able to affect the active state of the runtime by generating is-active
tokens. The runtime is considered active so long as an is-active token exists and has not been
terminated. Continuous performers are expected to terminate a token when its corresponding work has
completed.

For example, a performer that registers a platform animation might generate a token when the
animation starts. When the animation completes the token would be terminated.

### Step 1: Conform to ContinuousPerforming and store the token generator

```java
public class MyPerformer extends Performer<View> implements ContinuousPerforming {
  // Store the emitter in your class' definition.
  private IsActiveTokenGenerator tokenGenerator;

  @Override
  public void setIsActiveTokenGenerator(IsActiveTokenGenerator isActiveTokenGenerator) {
    tokenGenerator = isActiveTokenGenerator;
  }
}
```

### Step 2: Generate a token when some continuous work has started

You will likely need to store the token in order to be able to reference it at a later point.

```java
Animator animator;
animator.addListener(new AnimatorListenerAdapter() {

  private IsActiveToken token;

  @Override
  public void onAnimationStart(Animator animation) {
    token = tokenGenerator.generate();
  }
});
animator.start();
```

### Step 3: Terminate the token when work has completed

```java
@Override
public void onAnimationEnd(Animator animation) {
  token.terminate();
}
```

## How to trace internal runtime events

Tracing allows you to observe internal events occurring within a runtime. This information may be
used for the following purposes:

- Debug logging.
- Inspection tooling.

Use for other purposes is unsupported.

### Step 1: Create a tracer class

```java
public class CustomTracer implements Tracing {
}
```

### Step 2: Implement methods

The documentation for the Tracing interface enumerates the available methods.

```java
public class CustomTracer implements Tracing {
  @Override
  public <T> void onAddPlan(Plan<T> plan, T target) {

  }
}
```

## How to log runtime events to the console

```java
runtime.addTracer(new LogcatTracer());
```

## Contributing

We welcome contributions!

Check out our [upcoming milestones](https://github.com/material-motion/runtime-android/milestones).

Learn more about [our team](https://material-motion.github.io/material-motion/team/),
[our community](https://material-motion.github.io/material-motion/team/community/), and
our [contributor essentials](https://material-motion.github.io/material-motion/team/essentials/).

## License

Licensed under the Apache 2.0 license. See LICENSE for details.
