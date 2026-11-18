# Architecture

## Pipeline
```
CameraPreviewScreen (activity lifecycle)
  -> CameraX Preview + ImageAnalysis (KEEP_ONLY_LATEST, 640x480)
  -> LocalFrameAnalyzer (single-hand MediaPipe Hand Landmarker, VIDEO)
  -> HandDetection (21 normalized landmarks)
  -> Compose landmark overlay
```
Frame inference runs on a dedicated analysis executor, never the main thread. Gesture recognition, cursor control, accessibility actions and a foreground camera service are future milestones; no background tracking is implemented yet.

## Shared state
Milestone 3 keeps detections local to `CameraPreviewScreen` and displays them directly in its overlay. `GestureRuntime` is not connected to this preview; shared runtime state and background camera ownership are future work.

Deviation from the original tree: `GestureRuntime.kt` sits at `core/` root, and `SplashScreen.kt` is in `features/onboarding/`. Everything else follows the requested structure.

## Coordinates (one transform, one place)
1. CameraX `rotationDegrees` is passed to MediaPipe so landmarks are returned in upright normalized image coordinates.
2. The front-camera preview overlay mirrors x once and accounts for `PreviewView`'s `FILL_CENTER` crop.
3. Fingertip values displayed in the preview are the original normalized MediaPipe coordinates; gesture-coordinate transforms and smoothing are future work.

Successful detection and overlay alignment still require physical-device verification.

## Frame rate
The current preview requests 640x480 frames and uses CameraX `KEEP_ONLY_LATEST`; synchronous inference runs on a dedicated single-thread executor, so stale frames do not build up and the UI thread is not blocked. Fixed-rate throttling and adaptive idle FPS are not implemented. Benchmark before tuning inference rate or delegate.

## Dependencies by milestone
M1 Compose, Navigation, DataStore. M2 CameraX. M3 MediaPipe tasks-vision (model bundled in assets, so fully offline). M8 Accessibility service (no library). M11 Room.

## Privacy rules enforced in code
No INTERNET permission, no analytics libraries, no frame persistence, no logging of frame data.
