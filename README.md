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

Add the Jitpack repository to your project's `build.gradle`:
```gradle
allprojects {
    repositories {
        maven { url "https://jitpack.io" }
    }
}
```

Add the dependency to your module's `build.gradle`:

```gradle
dependencies {
    compile 'com.github.material-motion:material-motion-runtime-android:1.0.0'
}
```

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

Run `./gradlew installDebug` from the project root.

## License

Licensed under the Apache 2.0 license. See LICENSE for details.
