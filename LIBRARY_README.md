# Convention

Describe your library here.

### Depending on the library

Add it to your build.gradle with:
```gradle
allprojects {
    repositories {
        maven { url "https://jitpack.io" }
    }
}
```
and:

```gradle
dependencies {
    compile 'com.github.github-group:github-repo:master-SNAPSHOT'
}
```

### Contributing to the library

Open Android Studio,
choose `File > New > Import`,
choose the root `build.gradle` file.

### Building the sample

Run `./gradlew installDebug` from the project root.
