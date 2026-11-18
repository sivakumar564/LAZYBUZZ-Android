# Development roadmap

| # | Milestone | Status |
|---|---|---|
| 1 | Project setup, theme, navigation, splash, dashboard | Implemented; unit tests pass and debug APK builds |
| 2 | CameraX integration, permissions, onboarding, preview | CameraX, runtime permission flow and live preview implemented; onboarding and device verification pending |
| 3 | MediaPipe hand tracking | Implemented: CameraX ImageAnalysis feeds a bundled, on-device MediaPipe Hand Landmarker; one hand and all 21 landmarks are rendered with normalized thumb/index fingertip coordinates. `testDebugUnitTest` and `assembleDebug` pass. Successful physical-device landmark detection is not yet verified, so this milestone remains in validation. |
| 4 | Finger movement recognition, coordinate transforms | Not started |
| 5 | Gesture recognition and arbitration, mapping screen | Not started |
| 6 | Virtual cursor, calibration | Not started |
| 7 | Direct gesture mode, interaction settings | Not started |
| 8 | Accessibility service | Not started |
| 9 | Background operation | Not started |
| 10 | Floating controls | Not started |
| 11 | Custom Gesture Studio, Room | Not started |
| 12 | Testing and optimization | Not started |

## Benchmark plan (before any optimization)
Compare 10, 15 and 20 FPS, CPU vs GPU delegate, and idle 5 FPS. Record: end-to-end latency (frame timestamp to action dispatch, p50 and p95), achieved FPS, battery drain per hour over 20+ minutes with `dumpsys batterystats`, and thermal status. Results go in `docs/BENCHMARKS.md` once measured.

## Mandatory real-device tests (all NOT YET TESTED)
- **Background:** start from the visible app, press Home, open another app. Gestures keep working, camera-active notification is visible, Stop works immediately.
- **Lock screen:** lock and unlock. Record whether frames stop, whether tracking resumes, and confirm no action is ever sent to the lock screen. Check OEM battery-saver kills.
- **Camera interruptions:** incoming call, another app opening the camera, camera permission revoked while running, system camera privacy toggle (Android 12+). Expect status INTERRUPTED, updated notification, retry with backoff, no crash.
- **Lifecycle:** swipe app from Recents, accessibility service disabled while running, rotation, split-screen, Doze.
- **OS versions:** at least Android 12, 13, 14 and 15 (foreground-service camera rules differ).

## Build verification
- `testDebugUnitTest` passed.
- `assembleDebug` succeeded with JDK 21 and Android SDK Platform 35.
- The merged manifest includes CAMERA and does not include INTERNET.
- The debug APK is at `app/build/outputs/apk/debug/app-debug.apk`.
- Not run on a physical device or emulator; camera permission, preview, denial and camera-interruption flows still need device verification.
