package com.lazybuzz.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun LbCard(modifier: Modifier = Modifier, radius: Dp = 20.dp, content: @Composable ColumnScope.() -> Unit) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(radius),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
    ) { Column(content = content) }
}

@Composable
fun StatusPill(text: String, active: Boolean, modifier: Modifier = Modifier) {
    val cs = MaterialTheme.colorScheme
    val tone = if (active) cs.secondary else cs.onSurfaceVariant
    Surface(modifier = modifier, shape = CircleShape, color = tone.copy(alpha = 0.14f)) {
        Row(Modifier.padding(horizontal = 12.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(8.dp).background(tone, CircleShape))
            Spacer(Modifier.width(8.dp))
            Text(text, style = MaterialTheme.typography.labelLarge, color = tone)
        }
    }
}

/** Stand-in for screens that arrive in later milestones. */
@Composable
fun PlaceholderScreen(title: String, blurb: String, milestone: String, onBack: () -> Unit) {
    Scaffold { pad ->
        Column(Modifier.padding(pad).padding(horizontal = 20.dp, vertical = 8.dp)) {
            TextButton(onClick = onBack) { Text("Back") }
            Spacer(Modifier.height(16.dp))
            Text(title, style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(8.dp))
            Text(blurb, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(24.dp))
            StatusPill("Planned for $milestone", active = false)
        }
    }
}
