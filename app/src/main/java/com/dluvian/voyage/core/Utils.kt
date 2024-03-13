package com.dluvian.voyage.core

import android.content.Context
import androidx.compose.material3.SnackbarHostState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import rust.nostr.protocol.PublicKey

fun PublicKey.shortenedBech32(): String {
    val bech32 = this.toBech32()
    return "${bech32.take(10)}:${bech32.takeLast(5)}"
}

fun SnackbarHostState.showToast(scope: CoroutineScope, context: Context, resId: Int) {
    this.currentSnackbarData?.dismiss()
    val msg = context.getString(resId)
    scope.launch {
        this@showToast.showSnackbar(message = msg, withDismissAction = true)
    }
}
