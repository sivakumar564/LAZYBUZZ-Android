package com.lazybuzz.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

private val Scheme = darkColorScheme(
    primary = Amber, onPrimary = Color(0xFF1B1406),
    secondary = Sage, onSecondary = Color(0xFF0B1A15),
    tertiary = Sage,
    background = Ink, onBackground = TextHi,
    surface = Surface1, onSurface = TextHi,
    surfaceVariant = Surface2, onSurfaceVariant = TextLo,
    outline = Line, outlineVariant = Line,
    error = Rose,
)

private val Type = Typography(
    headlineLarge = TextStyle(fontWeight = FontWeight.Light, fontSize = 36.sp, lineHeight = 40.sp, letterSpacing = (-0.5).sp),
    titleLarge = TextStyle(fontWeight = FontWeight.Medium, fontSize = 22.sp, lineHeight = 28.sp),
    titleMedium = TextStyle(fontWeight = FontWeight.Medium, fontSize = 16.sp, lineHeight = 22.sp),
    bodyLarge = TextStyle(fontSize = 16.sp, lineHeight = 24.sp),
    bodyMedium = TextStyle(fontSize = 14.sp, lineHeight = 20.sp),
    labelLarge = TextStyle(fontWeight = FontWeight.Medium, fontSize = 14.sp, lineHeight = 20.sp),
)

/** Dark only by design. */
@Composable
fun LazyBuzzTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = Scheme, typography = Type, content = content)
}
