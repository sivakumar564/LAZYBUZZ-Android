# LAZYBUZZ

Touchless gesture control for Android: the front camera tracks your index finger and turns small, casual movements into touch actions. Windows is planned later.

## Status: Milestone 1 of 12
Done: Gradle Kotlin DSL project, Material 3 dark theme, navigation (all 11 screens routed), splash, dashboard.
**Not built yet:** camera, hand tracking, gestures, accessibility service, background service, floating controls. The dashboard's Start button is intentionally disabled.

Nothing in this repository has been built or run on a device yet. See `docs/DEVELOPMENT_ROADMAP.md` for what has and has not been verified.

## Build
Requirements: JDK 17, Android SDK 35 (Android Studio, or command-line SDK).

1. Open the folder in Android Studio and let it sync, or from a terminal run `gradle wrapper --gradle-version 8.9` once (the wrapper JAR is not included), then `./gradlew assembleDebug`.
2. Unit tests: `./gradlew testDebugUnitTest`.
3. Install on a real phone (USB or wireless debugging): `./gradlew installDebug`.

Low-RAM machine: Gradle is capped at 1.5 GB in `gradle.properties`. Close the emulator and use a physical phone.

## Privacy
The manifest removes the `INTERNET` permission (`tools:node="remove"`), so camera data cannot leave the device even if a dependency asks for it. Backups are disabled. Frames are never recorded.

## Docs
`docs/ARCHITECTURE.md`, `docs/GESTURE_SPECIFICATION.md`, `docs/DEVELOPMENT_ROADMAP.md`
