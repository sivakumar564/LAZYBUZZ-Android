# Development roadmap

| # | Milestone | Status |
|---|---|---|
| 1 | Project setup, theme, navigation, splash, dashboard | Written. Not compiled (see below). |
| 2 | CameraX integration, permissions, onboarding, preview | Not started |
| 3 | MediaPipe hand tracking | Not started |
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

## Milestone 1 verification status
- Not compiled: no Gradle, Kotlin compiler or Android SDK in the authoring environment, and no network.
- Not run on a device.
- Static checks only (XML well-formedness, structure). Details in the milestone report.
