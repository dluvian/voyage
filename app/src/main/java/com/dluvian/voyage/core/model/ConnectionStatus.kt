package com.dluvian.voyage.core.model

import androidx.compose.ui.graphics.Color

sealed class ConnectionStatus {
    fun getColor(): Color {
        return when (this) {
            Connected -> Color.Green
            Disconnected -> Color.Red
            Waiting -> Color.Gray
        }
    }
}

data object Connected : ConnectionStatus()
data object Disconnected : ConnectionStatus()
data object Waiting : ConnectionStatus()
