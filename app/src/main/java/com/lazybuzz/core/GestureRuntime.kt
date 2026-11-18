package com.lazybuzz.core

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

enum class RuntimeStatus { STOPPED, STARTING, RUNNING, PAUSED, INTERRUPTED }

data class RuntimeState(
    val status: RuntimeStatus = RuntimeStatus.STOPPED,
    val handDetected: Boolean = false,
    val fps: Int = 0,
    val lastGesture: String? = null,
)

/** Single process-wide pipeline state. Only the camera service writes; every UI surface just collects [state]. */
object GestureRuntime {
    private val _state = MutableStateFlow(RuntimeState())
    val state: StateFlow<RuntimeState> = _state.asStateFlow()

    internal fun publish(block: (RuntimeState) -> RuntimeState) = _state.update(block)
}
