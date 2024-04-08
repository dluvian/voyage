package com.dluvian.voyage.core

import androidx.compose.material3.SnackbarHostState
import com.dluvian.voyage.data.model.FilterWrapper
import com.dluvian.voyage.data.model.RelevantMetadata
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import rust.nostr.protocol.EventId
import rust.nostr.protocol.Filter
import rust.nostr.protocol.Kind
import rust.nostr.protocol.KindEnum
import rust.nostr.protocol.Metadata
import rust.nostr.protocol.PublicKey
import rust.nostr.protocol.Timestamp

fun PublicKey.toShortenedNpub(): String {
    return shortenBech32(bech32 = this.toBech32())
}

fun shortenBech32(bech32: Bech32) = "${bech32.take(10)}:${bech32.takeLast(5)}"

fun PubkeyHex.toShortenedBech32(): String {
    if (this.isEmpty()) return ""
    val pubkey = runCatching { PublicKey.fromHex(this) }.getOrNull() ?: return ""
    return pubkey.toShortenedNpub()
}

private val hashtagRegex = Regex("""#\w+""")
fun extractCleanHashtags(content: String): List<Topic> {
    return hashtagRegex.findAll(content)
        .distinct()
        .map { it.value.removePrefix("#") }
        .toList()
}

fun extractHashtags(extractFrom: String) = hashtagRegex.findAll(extractFrom).toList()

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

fun <K, V> MutableMap<K, MutableSet<V>>.syncedPutOrAdd(key: K, value: Collection<V>) {
    val alreadyPresent: MutableSet<V>?
    synchronized(this) {
        alreadyPresent = this.putIfAbsent(key, value.toMutableSet())
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

@OptIn(FlowPreview::class)
fun <T> Flow<T>.firstThenDistinctDebounce(millis: Long): Flow<T> {
    return flow {
        emitAll(this@firstThenDistinctDebounce.take(1))
        emitAll(
            this@firstThenDistinctDebounce.drop(1)
                .distinctUntilChanged()
                .debounce(millis)
        )
    }
}

fun CoroutineScope.launchIO(block: suspend CoroutineScope.() -> Unit): Job {
    return this.launch(Dispatchers.IO) { block() }
}

fun <T> Collection<T>.takeRandom(n: Int): List<T> {
    return if (this.size <= n) return this.toList() else this.shuffled().take(n)
}

private val urlRegex = Regex(pattern = "https?://[^\\s]+")
fun extractUrls(extractFrom: String) = urlRegex.findAll(extractFrom).toList()


private val nostrMentionRegex = Regex("(nostr:|@)(npub1|note1|nevent1|nprofile1)[a-zA-Z0-9]+")
fun extractNostrMentions(extractFrom: String) = nostrMentionRegex.findAll(extractFrom).toList()

fun shortenUrl(url: String) = url.removePrefix("https://").removePrefix("http://")

fun createReplyAndVoteFilters(
    ids: List<EventId>,
    votePubkeys: List<PublicKey>,
    timestamp: Timestamp,
): List<FilterWrapper> {
    val voteFilter = Filter().kind(Kind.fromEnum(KindEnum.Reaction))
        .events(ids = ids)
        .authors(authors = votePubkeys)
        .until(timestamp = timestamp)
        .limit(limit = MAX_EVENTS_TO_SUB)
    val replyFilter = Filter().kind(Kind.fromEnum(KindEnum.TextNote))
        .events(ids = ids)
        .until(timestamp = timestamp)
        .limit(limit = MAX_EVENTS_TO_SUB)

    return listOf(
        FilterWrapper(filter = voteFilter),
        FilterWrapper(filter = replyFilter, e = ids.map { it.toHex() })
    )
}

fun List<Topic>.normalizeTopics(): List<Topic> {
    return this
        .map { it.removePrefix("#").trim().take(MAX_TOPIC_LEN).lowercase() }
        .filter { it.isBareTopicStr() }
        .distinct()
}
