package com.astroboii47.commander

import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

@Composable
fun AccentSelectionProvider(content: @Composable () -> Unit) {
    val accent = AccentSettings.color.value
    CompositionLocalProvider(
        LocalTextSelectionColors provides TextSelectionColors(
            handleColor = accent,
            backgroundColor = accent.copy(alpha = .34f),
        ),
        content = content,
    )
}
