package com.dluvian.voyage.core.model

import androidx.compose.ui.graphics.Color

sealed class ConnectionStatus {
    fun getColor(): Color {
        return when (this) {
            Connected -> Color.Green
            is Disconnected -> Color.Red
            Waiting -> Color.Gray
        }
    }
}

data object Connected : ConnectionStatus()
data object Waiting : ConnectionStatus()

sealed class Disconnected : ConnectionStatus()
data object BadConnection : Disconnected()
data object Spam : Disconnected()
