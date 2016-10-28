# Material Motion Android Runtime

[![Build Status](https://travis-ci.org/material-motion/material-motion-runtime-android.svg?branch=develop)](https://travis-ci.org/material-motion/material-motion-runtime-android)
[![codecov](https://codecov.io/gh/material-motion/material-motion-runtime-android/branch/develop/graph/badge.svg)](https://codecov.io/gh/material-motion/material-motion-runtime-android)

The Material Motion Runtime is a tool for describing motion declaratively.

## Declarative motion: motion as data

This library does not do much on its own. What it does do, however, is enable the expression of
motion as discrete units of data that can be introspected, composed, and sent over a wire.

This library encourages you to describe motion as data, or what we call *plans*. Plans are committed
to a *scheduler*. A scheduler coordinates the creation of *performers*, objects responsible for
translating plans into concrete execution.

Learn more about the APIs defined in the library by reading our
[technical documentation](https://material-motion.github.io/material-motion-runtime-android/) and our
[Starmap](https://material-motion.gitbooks.io/material-motion-starmap/content/specifications/runtime/).

## Installation

### Installation with Jitpack

Use Jitpack to depend on any of our [public releases](https://github.com/material-motion/material-motion-runtime-android/releases).

Add the Jitpack repository to your project's `build.gradle`:

```gradle
allprojects {
    repositories {
        maven { url "https://jitpack.io" }
    }
}
```

When starting out with a project it is likely that you will want to use the
latest version of the library. Add the dependency to your module's
`build.gradle`:

```gradle
dependencies {
    compile 'com.github.material-motion:material-motion-runtime-android:+'
}
```

Later on in the project you may want to freeze to a specific version of the
library. This is **highly recommended** because it makes your builds predictable
and reproducible. Take care to occasionally [check for updates](https://github.com/ben-manes/gradle-versions-plugin).

```gradle
dependencies {
    compile 'com.github.material-motion:material-motion-runtime-android:3.0.0'
}
```

It is also possible to specify a *dynamic version* range. This is useful to stay
up to date on a major version, without the risk of new library releases
introducing breaking changes into your project.

```gradle
dependencies {
    compile 'com.github.material-motion:material-motion-runtime-android:3.+'
}
```

For more information regarding versioning, see:

- [Gradle Documentation on Dynamic Versions](https://docs.gradle.org/current/userguide/dependency_management.html#sub:dynamic_versions_and_changing_modules)
- [Material Motion Versioning Policies](https://material-motion.gitbooks.io/material-motion-team/content/essentials/core_team_contributors/release_process.html#versioning)

### Using the files from a folder local to the machine

You can have a copy of this library with local changes and test it in tandem
with its client project. To add a local dependency on this library, add this
library's identifier to your project's `local.dependencies`:

```
com.github.material-motion:material-motion-runtime-android
```

> Because `local.dependencies` is never to be checked into Version Control
Systems, you must also ensure that any local dependencies are also defined in
`build.gradle` as explained in the previous section.

**Important**

For each local dependency listed, you *must* run `gradle install` from its
project root every time you make a change to it. That command will publish your
latest changes to the local maven repository. If your local dependencies have
local dependencies of their own, you must `gradle install` them as well. See
[Issue #16](https://github.com/material-motion/material-motion-runtime-android/issues/16).

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

    git clone https://github.com/material-motion/material-motion-runtime-android.git
    cd material-motion-runtime-android
    gradle installDebug

To run all unit tests, run the following commands:

    git clone https://github.com/material-motion/material-motion-runtime-android.git
    cd material-motion-runtime-android
    gradle test

# Guides

1. [Architecture](#architecture)
1. [How to define a new plan and performer type](#how-to-create-a-new-plan-and-performer-type)
1. [How to commit a plan to a scheduler](#how-to-commit-a-plan-to-a-scheduler)
1. [How to commit a named plan to a scheduler](#how-to-commit-a-named-plan-to-a-scheduler)
1. [How to configure performers with plans](#how-to-configure-performers-with-plans)
1. [How to configure performers with named plans](#how-to-configure-performers-with-named-plans)
1. [How to use composition to fulfill plans](#how-to-use-composition-to-fulfill-plans)
1. [How to indicate continuous performance](#how-to-indicate-continuous-performance)

## Architecture

The Material Motion Runtime consists of two groups of APIs: a scheduler object and a
constellation of protocols loosely consisting of plan and performing types.

### Scheduler

The Scheduler](https://material-motion.github.io/material-motion-runtime-android/index.html?com/google/android/material/motion/runtime/Scheduler.html)
object is a coordinating entity whose primary responsibility is to fulfill plans by creating
performers. You can create many schedulers throughout the lifetime of your application. A good rule
of thumb is to have one scheduler per interaction or transition.

### Plan + Performance types

The [Plan](https://material-motion.github.io/material-motion-runtime-android/index.html?com/google/android/material/motion/runtime/Plan.html)
and [Performer](https://material-motion.github.io/material-motion-runtime-android/index.html?com/google/android/material/motion/runtime/Performer.html)
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

- [NamedPlanPerformance](https://material-motion.github.io/material-motion-runtime-android/index.html?com/google/android/material/motion/runtime/Performer.NamedPlanPerformance.html): [How to configure performers with named plans](#how-to-configure-performers-with-named-plans)
- [ContinuousPerformance](https://material-motion.github.io/material-motion-runtime-android/index.html?com/google/android/material/motion/runtime/Performer.ContinuousPerformance.html): [How to indicate continuous performance](#how-to-indicate-continuous-performance)
- [ComposablePerformance](https://material-motion.github.io/material-motion-runtime-android/index.html?com/google/android/material/motion/runtime/Performer.ComposablePerformance.html): [How to use composition to fulfill plans](#how-to-use-composition-to-fulfill-plans)

See the associated links for more details on each performing type.

> Note: only one instance of a type of performer **per target** is ever created. This allows you to
> register multiple plans to the same target in order to configure a performer. See
> [How to configure performers with plans](#how-to-configure-performers-with-plans) for more details.

```java
public class MyPerformer extends Performer {
  @Override
  public void addPlan(BasePlan plan) {
  }
}
```

### Step 3: Make the plan type a formal Plan

Conforming to Plan requires:

1. that you define the type of performer your plan requires, and
2. that your plan be Cloneable.

```java
public class MyPlan extends Plan {
  @Override
  public Class<? extends BasePerformance> getPerformerClass() {
    return MyPerformer.class;
  }

  @Override
  public Plan clone() {
    // Only override this method if you need to deep clone reference-typed fields.
    return super.clone();
  }
}
```

## How to commit a plan to a scheduler

### Step 1: Create and store a reference to a scheduler instance

```java
public class MyActivity extends Activity {
  private final Scheduler scheduler = new Scheduler();
}
```

### Step 2: Associate plans with targets

```java
Plan plan;
View target;

scheduler.addPlan(plan, target);
```

## How to commit a named plan to a scheduler

### Step 1: Create and store a reference to a scheduler instance

```java
public class MyActivity extends Activity {
  private final Scheduler scheduler = new Scheduler();
}
```

### Step 2: Associate plans with targets

```java
NamedPlan plan;
String name;
View target;

scheduler.addNamedPlan(plan, name, target);
```

## How to configure performers with plans

The `addPlan()` method will be invoked with plans that require use of this performer.

```java
public class MyPerformer extends Performer {
  @Override
  public void addPlan(BasePlan plan) {
    MyPlan myPlan = (MyPlan) plan;

    // Do something with myPlan.
  }
}
```

***Handling multiple plan types***

```java
public class MyPerformer extends Performer {
  @Override
  public void addPlan(BasePlan plan) {
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
public class MyPerformer extends Performer {
  @Override
  public void addPlan(NamedPlan plan, String name) {
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

### Step 1: Conform to ComposablePerformance and store the plan emitter

```java
public class MyPerformer extends Performer implements ComposablePerformance {
  // Store the emitter in your class' definition.
  private PlanEmitter emitter;

  @Override
  public void setPlanEmitter(PlanEmitter planEmitter) {
    this.emitter = planEmitter;
  }
}
```

### Step 2: Emit plans

Performers are only able to emit plans for their associated target.

```java
Plan plan;

emitter.emit(plan);
```

## How to indicate continuous performance

Performers will often perform their actions over a period of time or while an interaction is
active. These types of performers are called continuous performers.

A continuous performer is able to affect the active state of the scheduler by generating is-active
tokens. The scheduler is considered active so long as an is-active token exists and has not been
terminated. Continuous performers are expected to terminate a token when its corresponding work has
completed.

For example, a performer that registers a platform animation might generate a token when the
animation starts. When the animation completes the token would be terminated.

### Step 1: Conform to ContinuousPerformance and store the token generator

```java
public class MyPerformer extends Performer implements ComposablePerformance {
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

## Contributing

We welcome contributions!

Check out our [upcoming milestones](https://github.com/material-motion/material-motion-runtime-android/milestones).

Learn more about [our team](https://material-motion.gitbooks.io/material-motion-team/content/),
[our community](https://material-motion.gitbooks.io/material-motion-team/content/community/),
and our [contributor essentials](https://material-motion.gitbooks.io/material-motion-team/content/essentials/).

## License

Licensed under the Apache 2.0 license. See LICENSE for details.
