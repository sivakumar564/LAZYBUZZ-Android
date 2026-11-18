package com.lazybuzz.ui.animations

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.drawscope.Stroke

/** The virtual-cursor motif: a fingertip dot inside a ring. Rings pulse only while tracking is active. */
@Composable
fun PointerMark(active: Boolean, modifier: Modifier = Modifier) {
    val color = MaterialTheme.colorScheme.primary
    val phase: State<Float> =
        if (active) {
            rememberInfiniteTransition(label = "pointer").animateFloat(
                initialValue = 0f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(tween(2200, easing = LinearEasing)),
                label = "phase",
            )
        } else {
            remember { mutableFloatStateOf(0f) }
        }
    Canvas(modifier) {
        val r = size.minDimension / 2f
        drawCircle(color, r * 0.13f)
        drawCircle(color.copy(alpha = 0.6f), r * 0.45f, style = Stroke(2.dp.toPx()))
        if (active) {
            val p = phase.value
            drawCircle(color.copy(alpha = 0.5f * (1f - p)), r * (0.45f + 0.55f * p), style = Stroke(1.5.dp.toPx()))
        } else {
            drawCircle(color.copy(alpha = 0.22f), r * 0.8f, style = Stroke(1.dp.toPx()))
        }
    }
}
