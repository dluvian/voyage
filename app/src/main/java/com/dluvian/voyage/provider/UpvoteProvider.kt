package com.dluvian.voyage.provider

import android.util.Log
import com.dluvian.voyage.NostrService
import com.dluvian.voyage.PAGE_SIZE
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import rust.nostr.sdk.Event
import rust.nostr.sdk.EventId
import rust.nostr.sdk.Filter
import rust.nostr.sdk.Kind
import rust.nostr.sdk.KindStandard

class UpvoteProvider(private val service: NostrService) {
    private val logTag = "UpvoteProvider"
    private val mutex = Mutex()
    private val upvotes = mutableMapOf<EventId, MutableSet<EventId>>() // PostId to UpvoteIds

    suspend fun init() {
        val pubkey = service.pubkey()
        val upvoteFilter = Filter()
            .author(pubkey)
            .kind(Kind.fromStd(KindStandard.REACTION))
            .limit(PAGE_SIZE.toULong().times(2u)) // Load 2 pages worth of upvotes

        val dbResult = service.dbQuery(upvoteFilter).toVec()
        if (dbResult.isEmpty()) {
            Log.i(logTag, "Upvotes of $pubkey not found in database")
            return
        }

        val entries =
            dbResult.groupBy(keySelector = { it.tags().eventIds().firstOrNull() }) { it.id() }
        mutex.withLock {
            entries.forEach { (postId, upvoteIds) ->
                if (postId != null) upvotes.put(postId, upvoteIds.toMutableSet())
            }
        }
    }

    suspend fun switchSigner() {
        mutex.withLock { upvotes.clear() }
        init()
    }

    suspend fun update(event: Event) {
        when (event.kind().asStd()) {
            KindStandard.EVENT_DELETION -> {
                val eventIds = event.tags().eventIds()
                if (eventIds.isEmpty()) return
                if (event.author() != service.pubkey()) return

                mutex.withLock {
                    upvotes.values.forEach { upvoteIds -> upvoteIds.removeAll(eventIds) }
                }
            }

            KindStandard.REACTION -> {
                if (event.content() == "-") return
                if (event.author() != service.pubkey()) return
                val postId = event.tags().eventIds().firstOrNull()
                if (postId == null) return

                mutex.withLock {
                    upvotes[postId]?.add(event.id())
                }
            }

            null -> {
                Log.w(logTag, "${event.kind().asU16()} has no KindStandard")
            }

            else -> {
                Log.d(logTag, "Updating ${event.kind().asU16()} is not supported")
            }
        }
    }

    suspend fun filterUpvoted(postIds: Collection<EventId>): List<EventId> {
        if (postIds.isEmpty()) return emptyList()
        val fullMap = mutex.withLock { upvotes.toMap() }

        return postIds.filterNot { id -> fullMap[id].isNullOrEmpty() }
    }

    suspend fun reserveUpvotes(postIds: Collection<EventId>, dbOnly: Boolean) {
        if (postIds.isEmpty()) return
        val upvoted = filterUpvoted(postIds).toSet()
        val neutral = postIds.filterNot { upvoted.contains(it) }
        if (neutral.isEmpty()) return

        val pubkey = service.pubkey()
        val upvoteFilter = Filter()
            .author(pubkey)
            .kind(Kind.fromStd(KindStandard.REACTION))
            .events(neutral) // Referenced as e-tag
            .limit(neutral.size.toULong() * 2u)

        val dbResult = service.dbQuery(upvoteFilter).toVec()
        val dbPostIds = dbResult.flatMap { it.tags().eventIds() }.toSet()
        dbResult.forEach { update(it) }

        val dbNeutral = neutral.filterNot { dbPostIds.contains(it) }
        if (dbNeutral.isEmpty()) return

        if (!dbOnly) {
            val subFilter = Filter()
                .kind(Kind.fromStd(KindStandard.REACTION))
                .author(pubkey)
                .events(dbNeutral) // Referenced as e-tag
                .limit(dbNeutral.size.toULong() * 2u) // We may have duplicate upvotes
            service.subscribe(subFilter)
        }
    }
}