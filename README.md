# Android Video Player App

This project contains a simple Android application written in Java that plays a sample video using ExoPlayer.

## Building

You need Android Studio or a local Gradle installation. Run the following to
generate the wrapper scripts and then build the debug APK:

```bash
gradle wrapper
./gradlew assembleDebug
```

The wrapper JAR is intentionally not included in the repository to avoid
committing binaries. Running `gradle wrapper` creates `gradlew` along with the
required JAR so the build can be reproduced without a pre-installed Gradle
version. You can also build directly with your system Gradle via
`gradle assembleDebug`.

The app streams a sample video from the web by default. You can tap the "Pick
Video" button to choose a local file using Android's media picker. The player
shows a progress bar and lets you change playback speed using the spinner below
the video. Edit `MainActivity` if you want to supply your own default URL.
