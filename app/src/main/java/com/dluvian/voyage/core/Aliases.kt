package com.dluvian.voyage.core

import androidx.compose.foundation.layout.RowScope
import androidx.compose.runtime.Composable
import com.dluvian.voyage.data.event.ValidatedEvent
import com.dluvian.voyage.data.model.RelayedItem

typealias EventIdHex = String
typealias PubkeyHex = String
typealias Topic = String
typealias OnUpdate = (UIEvent) -> Unit
typealias ComposableContent = @Composable () -> Unit
typealias ComposableRowContent = @Composable RowScope.() -> Unit
typealias Fn = () -> Unit

typealias RelayedValidatedEvent = RelayedItem<ValidatedEvent>
