package com.dluvian.voyage.core

import androidx.compose.material3.SnackbarHostState
import com.dluvian.voyage.data.model.RelevantMetadata
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import rust.nostr.protocol.Metadata
import rust.nostr.protocol.PublicKey

fun PublicKey.shortenBech32(): String {
    val bech32 = this.toBech32()
    return "${bech32.take(10)}:${bech32.takeLast(5)}"
}

fun PubkeyHex.toShortenedBech32(): String {
    if (this.isEmpty()) return ""
    val pubkey = runCatching { PublicKey.fromHex(this) }.getOrNull() ?: return ""
    return pubkey.shortenBech32()
}

fun Metadata.toRelevantMetadata(createdAt: Long): RelevantMetadata {
    return RelevantMetadata(about = this.getAbout()?.trim(), createdAt = createdAt)
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

fun <K, V> MutableMap<K, MutableSet<V>>.putOrAdd(key: K, value: Collection<V>) {
    val alreadyPresent = this.putIfAbsent(key, value.toMutableSet())
    alreadyPresent?.addAll(value)
}
