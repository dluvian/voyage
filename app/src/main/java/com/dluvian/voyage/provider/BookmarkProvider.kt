package com.dluvian.voyage.provider

import android.util.Log
import com.dluvian.voyage.nostr.NostrService
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import rust.nostr.sdk.Event
import rust.nostr.sdk.EventId
import rust.nostr.sdk.Filter
import rust.nostr.sdk.Kind
import rust.nostr.sdk.KindStandard


class BookmarkProvider(private val service: NostrService) : IEventUpdate {
    private val logTag = "BookmarkProvider"
    private val mutex = Mutex()
    private var bookmarkEvent: Event? = null

    suspend fun init() {
        val pubkey = service.pubkey()
        val bookmarkFilter = Filter()
            .author(pubkey)
            .kind(Kind.fromStd(KindStandard.BOOKMARKS))
            .limit(1u)

        val dbResult = service.dbQuery(bookmarkFilter).firstOrNull()
        if (dbResult == null) {
            Log.i(logTag, "Bookmarks of $pubkey is not found in database")
            return
        }

        mutex.withLock {
            bookmarkEvent = dbResult
        }
    }

    suspend fun switchSigner() {
        mutex.withLock { bookmarkEvent = null }
        init()
    }

    override suspend fun update(event: Event) {
        if (event.kind() != Kind.fromStd(KindStandard.BOOKMARKS)) return

        val currentTime = mutex.withLock { bookmarkEvent?.createdAt()?.asSecs() ?: 0u }
        if (event.createdAt().asSecs() <= currentTime) return
        if (event.author() != service.pubkey()) return

        mutex.withLock {
            bookmarkEvent = event
        }
    }

    suspend fun bookmarks(): List<EventId> {
        return mutex.withLock { bookmarkEvent?.tags()?.eventIds().orEmpty() }
    }
}
