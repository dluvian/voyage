package com.dluvian.voyage.core

import androidx.compose.runtime.Composable
import com.dluvian.voyage.data.model.RelayedItem
import com.dluvian.voyage.data.model.ValidatedEvent

typealias EventIdHex = String
typealias PubkeyHex = String
typealias Topic = String
typealias OnUpdate = (UIEvent) -> Unit
typealias ComposableContent = @Composable () -> Unit
typealias Lambda = () -> Unit

typealias RelayedValidatedEvent = RelayedItem<ValidatedEvent>