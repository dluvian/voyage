package com.dluvian.voyage.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

data class Sizing(
    val smallIndicator: Dp = 18.dp,
    val iconButton: Dp = 32.dp,
    val smallIndicatorStrokeWidth: Dp = 3.dp,
    val baseHint: Dp = 48.dp,
    val bigDivider: Dp = 8.dp,
    val topicChipMinSize: Dp = 128.dp,
)

val LocalSizing = compositionLocalOf { Sizing() }

val sizing: Sizing
    @Composable
    @ReadOnlyComposable
    get() = LocalSizing.current
