package com.dluvian.voyage.core

import androidx.compose.material3.SnackbarHostState
import com.dluvian.voyage.data.model.RelevantMetadata
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import rust.nostr.protocol.Metadata
import rust.nostr.protocol.PublicKey

fun PublicKey.shortenBech32(): String {
    return this.toBech32().shortenBech32()
}

fun PubkeyHex.shortenBech32(): String {
    return if (this.isEmpty()) "" else "${this.take(10)}:${this.takeLast(5)}"
}

fun Metadata.toRelevantMetadata(createdAt: Long): RelevantMetadata {
    return RelevantMetadata(about = this.getAbout(), createdAt = createdAt)
}
fun SnackbarHostState.showToast(scope: CoroutineScope, msg: String) {
    this.currentSnackbarData?.dismiss()
    scope.launch {
        this@showToast.showSnackbar(message = msg, withDismissAction = true)
    }
}

private val bareTopicRegex = Regex("[^#\\s]+\$")
fun String.isBareTopicStr(): Boolean = bareTopicRegex.matches(this)

fun <K, V> MutableMap<K, MutableList<V>>.syncedPutOrAdd(key: K, value: MutableList<V>) {
    val alreadyPresent: MutableList<V>?
    synchronized(this) {
        alreadyPresent = this.putIfAbsent(key, value)
    }
    if (alreadyPresent != null) {
        synchronized(alreadyPresent) {
            alreadyPresent.addAll(value)
        }
    }
}
