package com.dluvian.voyage.data.event

import android.util.Log
import com.dluvian.voyage.data.model.RelayedItem
import com.dluvian.voyage.data.model.ValidatedContactList
import com.dluvian.voyage.data.model.ValidatedEvent
import com.dluvian.voyage.data.model.ValidatedReplyPost
import com.dluvian.voyage.data.model.ValidatedRootPost
import com.dluvian.voyage.data.model.ValidatedTopicList
import com.dluvian.voyage.data.model.ValidatedVote
import com.dluvian.voyage.data.room.dao.PostInsertDao
import com.dluvian.voyage.data.room.entity.PostEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.LinkedList

private const val TAG = "EventProcessor"
private const val MAX_SQL_PARAMS = 200

class EventProcessor(
    private val postInsertDao: PostInsertDao,
) {
    private val scope = CoroutineScope(Dispatchers.IO)

    fun processEvents(events: Set<RelayedItem<ValidatedEvent>>) {
        if (events.isEmpty()) return

        val rootPosts = mutableListOf<RelayedItem<ValidatedRootPost>>()
        val replyPosts = mutableListOf<RelayedItem<ValidatedReplyPost>>()
        val votes = mutableListOf<ValidatedVote>()
        val contactLists = mutableListOf<ValidatedContactList>()
        val topicLists = mutableListOf<ValidatedTopicList>()

        events.forEach { relayedItem ->
            when (val item = relayedItem.item) {
                is ValidatedRootPost -> rootPosts.add(
                    RelayedItem(
                        item = item,
                        relayUrl = relayedItem.relayUrl
                    )
                )

                is ValidatedReplyPost -> replyPosts.add(
                    RelayedItem(
                        item = item,
                        relayUrl = relayedItem.relayUrl
                    )
                )

                is ValidatedVote -> votes.add(item)
                is ValidatedContactList -> contactLists.add(item)
                is ValidatedTopicList -> topicLists.add(item)
            }
        }

        processRootPosts(relayedRootPosts = rootPosts)
        processReplyPosts(relayedReplyPosts = replyPosts)
        processVotes(events = votes)
        processContactLists(events = contactLists)
        processTopicLists(events = topicLists)
    }

    private fun processRootPosts(relayedRootPosts: Collection<RelayedItem<ValidatedRootPost>>) {
        if (relayedRootPosts.isEmpty()) return

        val relayedEntities = relayedRootPosts.map { relayedItem ->
            RelayedItem(item = PostEntity.from(relayedItem.item), relayUrl = relayedItem.relayUrl)
        }
        val batches = relayedEntities.chunked(MAX_SQL_PARAMS)

        scope.launch {
            batches.forEach { postInsertDao.insertPosts(relayedPosts = it) }
        }.invokeOnCompletion { exception ->
            if (exception != null) Log.w(TAG, "Failed to process root posts", exception)
        }
    }

    private fun processReplyPosts(relayedReplyPosts: Collection<RelayedItem<ValidatedReplyPost>>) {
        if (relayedReplyPosts.isEmpty()) return

        val sorted = sortRepliesByLevel(replies = relayedReplyPosts.toSet())
        val relayedEntities = sorted.map { relayedItem ->
            RelayedItem(item = PostEntity.from(relayedItem.item), relayUrl = relayedItem.relayUrl)
        }
        val batches = relayedEntities.chunked(MAX_SQL_PARAMS)

        scope.launch {
            batches.forEach { postInsertDao.insertPosts(relayedPosts = it) }
        }.invokeOnCompletion { exception ->
            if (exception != null) Log.w(TAG, "Failed to process reply posts", exception)
        }
    }

    private fun processVotes(events: Collection<ValidatedEvent>) {
        TODO()
    }

    private fun processContactLists(events: Collection<ValidatedEvent>) {
        TODO()
    }

    private fun processTopicLists(events: Collection<ValidatedEvent>) {
        TODO()
    }

    private fun sortRepliesByLevel(
        replies: Set<RelayedItem<ValidatedReplyPost>>
    ): List<RelayedItem<ValidatedReplyPost>> {
        if (replies.isEmpty()) return emptyList()

        val repliesByLevel = LinkedList<MutableSet<RelayedItem<ValidatedReplyPost>>>()

        val groupedByReplyTo = replies.groupBy { it.item.replyToId }
        groupedByReplyTo.forEach { (replyTo, replies) ->
            val parentLevel =
                repliesByLevel.find { level -> level.map { it.item.id.toHex() }.contains(replyTo) }
            val index = repliesByLevel.indexOf(parentLevel)
            if (index < 0) repliesByLevel.addFirst(replies.toMutableSet())
            else {
                val success = repliesByLevel.getOrNull(index + 1)?.addAll(replies)
                if (success != true) repliesByLevel.add(replies.toMutableSet())
            }
        }

        return repliesByLevel.flatten()
    }
}