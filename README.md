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
Video" button to choose a local file using Android's media picker. Videos you
select are added to a playlist that you can open from the navigation drawer.
Use the Next and Prev buttons to skip tracks. A progress bar shows playback,
and there are sliders for volume and brightness control. The spinner below the
player changes playback speed. Use the Fullscreen button for immersive viewing
and lock orientation or share the current video from the toolbar menu. When you
press the home button, the player enters picture-in-picture mode so video
continues in a small window. Sample subtitles are bundled in `assets/sample.srt`.
