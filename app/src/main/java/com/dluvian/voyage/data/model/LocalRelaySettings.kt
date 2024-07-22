package com.dluvian.voyage.data.model

import androidx.compose.runtime.Stable

sealed class LocalRelaySetting {
    @Stable
    fun isEnabled(): Boolean {
        return when (this) {
            is LocalRelayDisabled -> false
            is LocalRelayEnabled -> true
        }
    }
}

data object LocalRelayDisabled : LocalRelaySetting()
data class LocalRelayEnabled(val port: Int) : LocalRelaySetting()
