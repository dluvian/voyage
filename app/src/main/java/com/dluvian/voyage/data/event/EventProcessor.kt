package com.dluvian.voyage.data.event

import android.util.Log
import com.dluvian.nostr_kt.getReplyToId
import com.dluvian.nostr_kt.isContactList
import com.dluvian.nostr_kt.isPostOrReply
import com.dluvian.nostr_kt.isRootPost
import com.dluvian.nostr_kt.isTopicList
import com.dluvian.nostr_kt.isVote
import com.dluvian.voyage.data.model.RelayedItem
import com.dluvian.voyage.data.room.dao.PostInsertDao
import com.dluvian.voyage.data.room.entity.PostEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import rust.nostr.protocol.Event

private const val TAG = "EventProcessor"
private const val MAX_SQL_PARAMS = 200

class EventProcessor(
    private val postInsertDao: PostInsertDao,
) {
    private val scope = CoroutineScope(Dispatchers.IO)

    private val failedPosts = mutableSetOf<RelayedItem<Event>>()
    fun processEvents(events: Set<RelayedItem<Event>>) {
        if (events.isEmpty() && failedPosts.isEmpty()) return
        Log.d(TAG, "Process ${events.size} events and ${failedPosts.size} retries")

        val posts = mutableListOf<RelayedItem<Event>>()
        val votes = mutableListOf<Event>()
        val contactLists = mutableListOf<Event>()
        val topicLists = mutableListOf<Event>()

        if (events.isEmpty()) {
            synchronized(failedPosts) {
                posts.addAll(failedPosts)
                failedPosts.clear()
            }
        }

        events.forEach {
            if (it.item.isPostOrReply()) posts.add(it)
            else if (it.item.isVote()) votes.add(it.item)
            else if (it.item.isContactList()) contactLists.add(it.item)
            else if (it.item.isTopicList()) topicLists.add(it.item)
        }

        processPosts(relayedEvents = posts)
        processVotes(events = votes)
        processContactLists(events = contactLists)
        processTopicLists(events = topicLists)
    }

    private fun processPosts(relayedEvents: Collection<RelayedItem<Event>>) {
        if (relayedEvents.isEmpty()) return

        val postsByLevel = sortPostsByLevel(posts = relayedEvents)
        Log.d(TAG, "Process ${relayedEvents.size} posts with ${postsByLevel.size} levels")

        scope.launch {
            postsByLevel.forEach { level ->
                level.chunked(MAX_SQL_PARAMS).forEach { batch ->
                    postInsertDao.insertPosts(relayedPosts = batch.map { relayedItem ->
                        RelayedItem(
                            item = PostEntity.from(relayedItem.item),
                            relayUrl = relayedItem.relayUrl
                        )
                    })
                }
            }
        }.invokeOnCompletion { exception ->
            if (exception != null) {
                val allPosts = postsByLevel.flatten()
                Log.w(TAG, "Failed to process posts", exception)
                synchronized(failedPosts) { failedPosts.addAll(allPosts) }
            }
        }

    }

    private fun processVotes(events: Collection<Event>) {
    }

    private fun processContactLists(events: Collection<Event>) {
    }

    private fun processTopicLists(events: Collection<Event>) {
    }

    private fun sortPostsByLevel(posts: Collection<RelayedItem<Event>>): List<Set<RelayedItem<Event>>> {
        val buffer = posts.toMutableSet()
        val postsByLevel = mutableListOf<Set<RelayedItem<Event>>>()
        var madeProgress = true
        while (buffer.isNotEmpty() && madeProgress) {
            val deepestLevel = postsByLevel.lastOrNull()
            if (deepestLevel == null) {
                val root = buffer.filter { it.item.isRootPost() }.toSet()
                postsByLevel.add(root)
                buffer.removeAll(root)
                madeProgress = root.isNotEmpty()
                continue
            }
            val deepestIds = deepestLevel.map { it.item.id().toHex() }.toSet()
            val nextLevel = buffer.filter { deepestIds.contains(it.item.getReplyToId()) }.toSet()
            postsByLevel.add(nextLevel)
            buffer.removeAll(nextLevel.toSet())
            madeProgress = nextLevel.isNotEmpty()
        }
        if (buffer.isNotEmpty()) postsByLevel.add(buffer)

        return postsByLevel
    }
}