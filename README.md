# Material Motion Android Runtime

The Material Motion Runtime is a tool for describing motion declaratively.

## Declarative motion, aka motion as data

This library does not do much on its own. What it does do, however, is enable the expression of
motion as data.

This library encourages you to describe motion as data, or what we call *plans*. Plans are committed
to a *scheduler*. The scheduler then coordinates the creation of *performers*, objects responsible
for translating plans into concrete execution.

Learn more about the APIs defined in the library by reading our
[technical documentation](https://material-motion.github.io/material-motion-runtime-android/) and our
[Starmap](https://material-motion.gitbooks.io/material-motion-starmap/content/specifications/runtime/).

## Depending on the library

### Using Jitpack

Jitpack allows you to easily add a dependency on any of the [published releases](https://github.com/material-motion/material-motion-runtime-android/releases)
for this library.

Add the Jitpack repository to your project's `build.gradle`:

```gradle
allprojects {
    repositories {
        maven { url "https://jitpack.io" }
    }
}
```

When starting out with a project it is likely that you will want to use
the latest version of the library.
Add the dependency to your module's `build.gradle`:

```gradle
dependencies {
    compile 'com.github.material-motion:material-motion-runtime-android:+'
}
```

Later on in the project you may want to freeze to a specific version of
the library. This is **highly recommended** because it makes your builds
predictable and reproducible. Take care to occasionally [check for updates](https://github.com/ben-manes/gradle-versions-plugin).

```gradle
dependencies {
    compile 'com.github.material-motion:material-motion-runtime-android:1.0.0'
}
```

It is also possible to specify a *dynamic version* range. This is useful
to stay up to date on a major version, without the risk of new library releases
introducing breaking changes into your project.

```gradle
dependencies {
    compile 'com.github.material-motion:material-motion-runtime-android:1.+'
}
```

For more information regarding versioning, see:

- [Gradle Documentation on Dynamic Versions](https://docs.gradle.org/current/userguide/dependency_management.html#sub:dynamic_versions_and_changing_modules)
- [Material Motion Versioning Policies](https://material-motion.gitbooks.io/material-motion-team/content/essentials/core_team_contributors/release_process.html#versioning)

### Using the files from a folder local to the machine

If you would like to edit this library in tandem with its client project
you can use `:local`.

```gradle
dependencies {
    compile 'com.github.material-motion:material-motion-runtime-android:local'
}
```

To use this option, you must run `gradle install` from the library's
project root every time you want local changes in the library to
propagate to the clients.

## Contributing

We welcome contributions!

Check out our [upcoming milestones](https://github.com/material-motion/material-motion-runtime-android/milestones).

Learn more about [our team](https://material-motion.gitbooks.io/material-motion-team/content/),
[our community](https://material-motion.gitbooks.io/material-motion-team/content/community/), and
our [contributor essentials](https://material-motion.gitbooks.io/material-motion-team/content/essentials/).

### Editing the library in Android Studio

Open Android Studio,
choose `File > New > Import`,
choose the root `build.gradle` file.

### Building the sample

Run `gradle installDebug` from the project root.

## License

Licensed under the Apache 2.0 license. See LICENSE for details.
