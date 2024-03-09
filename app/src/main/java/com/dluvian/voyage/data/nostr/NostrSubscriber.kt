package com.dluvian.voyage.data.nostr

import android.util.Log
import com.dluvian.nostr_kt.Kind
import com.dluvian.voyage.core.DEBOUNCE
import com.dluvian.voyage.core.EventIdHex
import com.dluvian.voyage.data.provider.RelayProvider
import com.dluvian.voyage.data.provider.WebOfTrustProvider
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import rust.nostr.protocol.EventId
import rust.nostr.protocol.Filter

class NostrSubscriber(
    private val nostrService: NostrService,
    private val relayProvider: RelayProvider,
    private val webOfTrustProvider: WebOfTrustProvider,
) {
    private val tag = "NostrSubscriber"
    private val scope = CoroutineScope(Dispatchers.IO)
    fun subFeed(until: Long, size: Int) {
        TODO()
    }

    // TODO: remove ids after x seconds to enable resubbing
    private val votesAndRepliesCache = mutableSetOf<EventIdHex>()
    private var votesAndRepliesJob: Job? = null
    fun subVotesAndReplies(postIds: Collection<EventIdHex>) {
        if (postIds.isEmpty()) return

        val newIds = postIds - votesAndRepliesCache
        if (newIds.isEmpty()) return

        votesAndRepliesJob?.cancel(CancellationException("Debounce"))
        votesAndRepliesJob = scope.launch {
            delay(DEBOUNCE)
            val ids = newIds.map { EventId.fromHex(it) }
            val voteFilter = Filter().events(ids)
                .kind(Kind.REACTION.toULong())
                .authors(webOfTrustProvider.getWebOfTrustPubkeys())
            val replyFilter = Filter().events(ids)
                .kind(Kind.TEXT_NOTE.toULong())
            val filters = listOf(voteFilter, replyFilter)

            relayProvider.getReadRelays().forEach { relay ->
                nostrService.subscribe(filters = filters, relayUrl = relay)
            }
        }
        votesAndRepliesJob?.invokeOnCompletion { ex ->
            if (ex == null) votesAndRepliesCache.addAll(newIds)
            else Log.d(tag, "Subbing votes and replies failed: ${ex.message}")
        }
    }
}
