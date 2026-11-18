# Gesture specification

All thresholds below are **unvalidated starting points** and are expressed relative to hand scale (wrist to middle-finger knuckle) so distance from the camera does not matter. They get tuned on real data.

## Gestures (unchanged from the original brief)
| Gesture | Action |
|---|---|
| Index finger up / down | Scroll up / down |
| Index finger left / right | Navigate left / right |
| Finger bend: bend index, straighten | Click (primary) |
| Forward tap: fingertip toward the screen, release | Click (optional, off by default) |
| Slowly hover over an app, then tap | Select and open |
| Thumb-index distance increases / decreases | Zoom in / out |
| Double tap with index finger | Like |

Users can enable either click method or both. No conventional hand signs.

## Click methods
- **Finger bend (primary):** index flexion angle (knuckle to tip) crosses a bend threshold then returns. The cursor point is latched when flexion starts and the click fires there, so the bend itself cannot move the click.
- **Forward tap (secondary):** fingertip scale change relative to hand size, confirmed by z-delta, then release. MediaPipe z is noisy, so this is optional and off by default.
- If both are enabled and both fire within 150 ms, only one click is sent.

## Zoom (natural pinch)
Driven by thumb-tip to index-tip distance divided by hand scale. It does **not** require either finger to be fully extended or any other finger pose. Zoom starts when that distance changes steadily beyond a threshold over about 150 ms while the hand itself is roughly still, and ends when the distance holds still for about 300 ms or the hand is lost. Zoom factor follows the distance ratio against the start distance.

## Priority and conflict prevention
Highest first: **1 Zoom, 2 Double tap, 3 Click, 4 Scroll/Navigate, 5 Cursor move.**
- One gesture owns the pipeline at a time. A lower-priority gesture cannot start while a higher one is active or pending, and stays suppressed for the cooldown after it ends.
- Every gesture needs a short confirmation window before it fires (no single-frame triggers).
- Bend vs scroll: scroll ignores fingertip motion that comes from flexion (flexion rate above a threshold suppresses scroll). Scroll needs sustained directional velocity.
- Bend vs zoom: if thumb-index distance is changing, it is a zoom candidate; if it is stable and flexion is changing, it is a click candidate. If both change, nothing fires until it is clear.
- Double tap = two clicks within about 300 ms and a small radius. When a Like action is assigned, a single click is held for that window so it is not sent before the second tap. This adds latency and will be benchmarked.
- Losing the hand for more than a few frames resets every state, so zoom or scroll can never stay stuck.
- Configurable sensitivity and cooldown (default 400 ms, per-gesture values to be tuned).

## Open questions (to decide with the owner, not assumed)
1. What should "Like" trigger? Current assumption: a double tap at the pointer position.
2. In Virtual Cursor mode, how are left/right navigation flicks distinguished from cursor movement?
