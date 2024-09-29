package com.dluvian.voyage.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

data class Sizing(
    val dot: Dp = 8.dp,
    val trustIndicator: Dp = 13.dp,
    val smallIndicator: Dp = 18.dp,
    val footerIconButton: Dp = 18.dp,
    val relayActionButton: Dp = 32.dp,
    val scrollUpButton: Dp = 32.dp,
    val smallIndicatorStrokeWidth: Dp = 3.dp,
    val baseHint: Dp = 48.dp,
    val bigDivider: Dp = 8.dp,
    val topicChipMinSize: Dp = 128.dp,
    val dialogLazyListHeight: Dp = 210.dp,
)

val LocalSizing = compositionLocalOf { Sizing() }

val sizing: Sizing
    @Composable
    @ReadOnlyComposable
    get() = LocalSizing.current
