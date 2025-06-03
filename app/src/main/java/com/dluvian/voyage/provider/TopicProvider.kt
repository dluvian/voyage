package com.dluvian.voyage.provider

import android.util.Log
import com.dluvian.voyage.Topic
import com.dluvian.voyage.nostr.NostrService
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import rust.nostr.sdk.Event
import rust.nostr.sdk.Filter
import rust.nostr.sdk.Kind
import rust.nostr.sdk.KindStandard
import kotlin.collections.orEmpty

class TopicProvider(private val service: NostrService) {
    private val logTag = "TopicProvider"
    private val mutex = Mutex()
    private var topicEvent: Event? = null

    suspend fun init() {
        val pubkey = service.pubkey()
        val topicFilter = Filter()
            .author(pubkey)
            .kind(Kind.fromStd(KindStandard.INTERESTS))
            .limit(1u)

        val dbResult = service.dbQuery(topicFilter).firstOrNull()
        if (dbResult == null) {
            Log.i(logTag, "Topics of $pubkey is not found in database")
            return
        }

        mutex.withLock { topicEvent = dbResult }
    }

    suspend fun switchSigner() {
        mutex.withLock { topicEvent = null }
        init()
    }

    suspend fun update(event: Event) {
        if (event.kind() != Kind.fromStd(KindStandard.INTERESTS)) return

        val currentTime = mutex.withLock { topicEvent?.createdAt()?.asSecs() ?: 0u }
        if (event.createdAt().asSecs() <= currentTime) return
        if (event.author() != service.pubkey()) return
        mutex.withLock { topicEvent = event }
    }

    suspend fun topics(): List<Topic> {
        return mutex.withLock { topicEvent?.tags()?.hashtags().orEmpty() }
    }
}
