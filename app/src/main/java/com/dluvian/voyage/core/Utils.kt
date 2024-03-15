package com.dluvian.voyage.core

import androidx.compose.material3.SnackbarHostState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import rust.nostr.protocol.PublicKey

fun PublicKey.shortenedBech32(): String {
    return this.toBech32().shortenBech32()
}

fun PubkeyHex.shortenBech32(): String {
    return "${this.take(10)}:${this.takeLast(5)}"
}

fun SnackbarHostState.showToast(scope: CoroutineScope, msg: String) {
    this.currentSnackbarData?.dismiss()
    scope.launch {
        this@showToast.showSnackbar(message = msg, withDismissAction = true)
    }
}
