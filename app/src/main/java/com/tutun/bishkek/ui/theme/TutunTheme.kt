package com.tutun.bishkek.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val TutunLightColorScheme = lightColorScheme(
    primary = Color(0xFF1A6BAA),
    secondary = Color(0xFF0D9488),
    tertiary = Color(0xFFD97706),
    background = Color(0xFFF8FAFC),
    surface = Color(0xFFFFFFFF),
    error = Color(0xFFDC2626),
)

@Composable
fun TutunTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = TutunLightColorScheme,
        content = content
    )
}

