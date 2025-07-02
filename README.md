# Android Video Player App

This project contains a simple Android application written in Java that plays a sample video using ExoPlayer.

## Building

You need Android Studio or the Android command line tools. Run:

```bash
./gradlew assembleDebug
```

The Gradle wrapper will download the build system on first run, so an
internet connection is required when invoking the command for the first time.
The wrapper JAR is intentionally not included in the repository to avoid
storing binaries. The `gradlew` script will download it automatically.

The app streams a sample video from the web by default. You can also tap the
"Pick Video" button to choose a local video using Android's media picker.
Edit `MainActivity` if you want to supply your own default URL.
