# Architecture

## Pipeline
```
CameraX (ImageAnalysis, KEEP_ONLY_LATEST)
  -> HandTracker (MediaPipe Hand Landmarker, LIVE_STREAM)
  -> LandmarkProcessor (rotation, mirror, normalize, smooth)
  -> GestureEngine (arbiter + detectors)
  -> VirtualCursor | DirectGestureController
  -> ActionManager -> GestureAccessibilityService (dispatchGesture / performGlobalAction)
```
All stages run on a dedicated analysis executor, never the main thread. The whole pipeline lives in `CameraForegroundService`, not in an Activity, so the in-app preview, background mode and floating bubble share one instance.

## Shared state
`core/GestureRuntime.kt` holds one process-wide `StateFlow<RuntimeState>` (status, handDetected, fps, lastGesture). The camera service is the only writer. The dashboard, live preview and floating bubble only collect it. One-shot events (a click happened) will be a separate `SharedFlow` added with the gesture engine in Milestone 5.

Deviation from the original tree: `GestureRuntime.kt` sits at `core/` root, and `SplashScreen.kt` is in `features/onboarding/`. Everything else follows the requested structure.

## Coordinates (one transform, one place)
1. CameraX gives `rotationDegrees`; pass it to MediaPipe as image rotation so landmarks come back upright and normalized 0..1.
2. Mirror the front camera exactly once, in `LandmarkProcessor` (`x = 1 - x`), so moving your hand right moves the pointer right. Direction semantics are defined in this mirrored space.
3. The preview overlay uses the same function, then corrects for `PreviewView` crop scaling. It must not mirror again.
4. Calibration maps the user's comfortable range to 0..1 and clamps.
5. A One Euro filter stabilizes the cursor point only.
6. 0..1 maps to pixels using the current display size, re-read on rotation, because `dispatchGesture` uses screen pixels.

Unit tests (Milestone 4): synthetic landmarks across 4 rotations x front/back.

## Frame rate
Start at 15 FPS by timestamp throttling (drop frames, never queue). After about 1.5 s without a hand, drop to 5 FPS and return to 15 FPS on detection. Further changes only after benchmarks (see roadmap).

## Dependencies by milestone
M1 Compose, Navigation, DataStore. M2 CameraX. M3 MediaPipe tasks-vision (model bundled in assets, so fully offline). M8 Accessibility service (no library). M11 Room.

## Privacy rules enforced in code
No INTERNET permission, no analytics libraries, no frame persistence, no logging of frame data.
