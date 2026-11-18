package com.lazybuzz.features.onboarding

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

/** One orchestrated moment: the ring opens around the dot the system splash already showed, then the name fades in. */
@Composable
fun SplashScreen(onDone: () -> Unit) {
    val reveal = remember { Animatable(0f) }
    val color = MaterialTheme.colorScheme.primary
    LaunchedEffect(Unit) {
        reveal.animateTo(1f, tween(900, easing = FastOutSlowInEasing))
        delay(450)
        onDone()
    }
    Column(
        Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Canvas(Modifier.size(108.dp)) {
            val r = size.minDimension / 2f
            drawCircle(color, r * 0.15f)
            drawCircle(color.copy(alpha = reveal.value), r * (0.15f + 0.30f * reveal.value), style = Stroke(3.dp.toPx()))
        }
        Spacer(Modifier.height(24.dp))
        Text(
            "lazybuzz",
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.graphicsLayer { alpha = reveal.value },
        )
        Spacer(Modifier.height(6.dp))
        Text(
            "Control your phone without touching it.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.graphicsLayer { alpha = reveal.value },
        )
    }
}
