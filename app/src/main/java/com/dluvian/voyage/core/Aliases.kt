package com.dluvian.voyage.core

import android.content.Intent
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.result.ActivityResult
import androidx.compose.foundation.layout.RowScope
import androidx.compose.runtime.Composable

typealias EventIdHex = String
typealias PubkeyHex = String
typealias Topic = String
typealias Bech32 = String
typealias OptionId = String
typealias Label = String
typealias OnUpdate = (UIEvent) -> Unit
typealias ComposableContent = @Composable () -> Unit
typealias ComposableRowContent = @Composable RowScope.() -> Unit
typealias Fn = () -> Unit
typealias ManagedLauncher = ManagedActivityResultLauncher<Intent, ActivityResult>
