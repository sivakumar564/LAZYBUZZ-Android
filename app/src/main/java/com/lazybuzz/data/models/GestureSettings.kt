package com.lazybuzz.data.models

enum class InteractionMode { VIRTUAL_CURSOR, DIRECT_GESTURE }

data class GestureSettings(
    val mode: InteractionMode = InteractionMode.VIRTUAL_CURSOR,
    val bendClickEnabled: Boolean = true,     // primary click
    val forwardTapEnabled: Boolean = false,   // optional secondary click (z-depth is noisy)
    val cursorVisible: Boolean = true,
    val sensitivity: Float = 0.5f,
    val cooldownMs: Int = 400,
    val targetFps: Int = 15,                  // starting point; tune only after benchmarking
    val idleFps: Int = 5,                     // used while no hand is detected
)
