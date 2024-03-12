package com.dluvian.voyage.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

data class Spacing(
    /**
     * 1 dp
     */
    val tiny: Dp = 1.dp,
    /**
     * 2 dp
     */
    val small: Dp = 2.dp,
    /**
     * 4 dp
     */
    val medium: Dp = 4.dp,
    /**
     * 8 dp
     */
    val large: Dp = 8.dp,
    /**
     * 12 dp
     */
    val xl: Dp = 12.dp,
    /**
     * 16 dp
     */
    val xxl: Dp = 16.dp,
    /**
     * 8 dp
     */
    val screenEdge: Dp = 8.dp,
    /**
     * 12 dp
     */
    val bigScreenEdge: Dp = 12.dp,
)

val LocalSpacing = compositionLocalOf { Spacing() }

val spacing: Spacing
    @Composable
    @ReadOnlyComposable
    get() = LocalSpacing.current
