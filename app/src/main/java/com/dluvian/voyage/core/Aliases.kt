package com.dluvian.voyage.core

import androidx.compose.foundation.layout.RowScope
import androidx.compose.runtime.Composable

typealias EventIdHex = String
typealias PubkeyHex = String
typealias Topic = String
typealias Bech32 = String
typealias OnUpdate = (UIEvent) -> Unit
typealias ComposableContent = @Composable () -> Unit
typealias ComposableRowContent = @Composable RowScope.() -> Unit
typealias Fn = () -> Unit
