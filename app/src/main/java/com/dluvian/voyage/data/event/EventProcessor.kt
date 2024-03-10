package com.dluvian.voyage.data.event

import android.util.Log
import com.dluvian.voyage.core.EventIdHex
import com.dluvian.voyage.core.PubkeyHex
import com.dluvian.voyage.core.RelayedValidatedEvent
import com.dluvian.voyage.data.keys.IPubkeyProvider
import com.dluvian.voyage.data.model.RelayedItem
import com.dluvian.voyage.data.model.ValidatedContactList
import com.dluvian.voyage.data.model.ValidatedList
import com.dluvian.voyage.data.model.ValidatedReplyPost
import com.dluvian.voyage.data.model.ValidatedRootPost
import com.dluvian.voyage.data.model.ValidatedTopicList
import com.dluvian.voyage.data.model.ValidatedVote
import com.dluvian.voyage.data.room.dao.tx.FriendUpsertDao
import com.dluvian.voyage.data.room.dao.tx.PostInsertDao
import com.dluvian.voyage.data.room.dao.tx.TopicUpsertDao
import com.dluvian.voyage.data.room.dao.tx.VoteUpsertDao
import com.dluvian.voyage.data.room.dao.tx.WebOfTrustUpsertDao
import com.dluvian.voyage.data.room.entity.VoteEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

private const val TAG = "EventProcessor"

class EventProcessor(
    private val postInsertDao: PostInsertDao,
    private val voteUpsertDao: VoteUpsertDao,
    private val friendUpsertDao: FriendUpsertDao,
    private val webOfTrustUpsertDao: WebOfTrustUpsertDao,
    private val topicUpsertDao: TopicUpsertDao,
    private val pubkeyProvider: IPubkeyProvider,
) {
    private val scope = CoroutineScope(Dispatchers.IO)

    fun processEvents(events: Set<RelayedValidatedEvent>) {
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
        processVotes(votes = votes)
        processContactLists(contactLists = contactLists)
        processTopicLists(topicLists = topicLists)
    }

    private fun processRootPosts(relayedRootPosts: Collection<RelayedItem<ValidatedRootPost>>) {
        if (relayedRootPosts.isEmpty()) return

        scope.launch {
            postInsertDao.insertRootPosts(relayedPosts = relayedRootPosts)
        }.invokeOnCompletion { exception ->
            if (exception != null) Log.w(TAG, "Failed to process root posts", exception)
        }
    }

    private fun processReplyPosts(relayedReplyPosts: Collection<RelayedItem<ValidatedReplyPost>>) {
        if (relayedReplyPosts.isEmpty()) return

        scope.launch {
            postInsertDao.insertReplyPosts(relayedPosts = relayedReplyPosts)
        }.invokeOnCompletion { exception ->
            if (exception != null) Log.w(TAG, "Failed to process reply posts", exception)
        }
    }

    private fun processVotes(votes: Collection<ValidatedVote>) {
        if (votes.isEmpty()) return

        val entitiesToInsert = filterNewestVotes(votes = votes).map { VoteEntity.from(it) }

        entitiesToInsert.forEach { vote ->
            scope.launch {
                voteUpsertDao.upsertVote(voteEntity = vote)
            }.invokeOnCompletion { exception ->
                if (exception != null) Log.w(TAG, "Failed to process vote ${vote.id}", exception)
            }
        }
    }

    private fun processContactLists(contactLists: Collection<ValidatedContactList>) {
        if (contactLists.isEmpty()) return

        val newestLists = filterNewestLists(lists = contactLists)

        val myPubkey = pubkeyProvider.getPubkeyHex()
        val myList = newestLists.find { it.pubkey.toHex() == myPubkey }
        val otherLists = newestLists.let { if (myList != null) it - myList else it }

        processFriendList(myFriendList = myList)
        processWebOfTrustList(webOfTrustList = otherLists)
    }

    private fun processFriendList(myFriendList: ValidatedContactList?) {
        if (myFriendList == null) return

        scope.launch {
            friendUpsertDao.upsertFriends(validatedContactList = myFriendList)
        }.invokeOnCompletion { exception ->
            if (exception != null) Log.w(TAG, "Failed to process friend list", exception)
        }
    }

    private fun processWebOfTrustList(webOfTrustList: Collection<ValidatedContactList>) {
        if (webOfTrustList.isEmpty()) return

        webOfTrustList.forEach { wot ->
            scope.launch {
                webOfTrustUpsertDao.upsertWebOfTrust(validatedWebOfTrust = wot)
            }.invokeOnCompletion { exception ->
                if (exception != null) Log.w(TAG, "Failed to process web of trust list", exception)
            }
        }
    }

    private fun processTopicLists(topicLists: Collection<ValidatedTopicList>) {
        if (topicLists.isEmpty()) return

        val myNewestList = filterNewestLists(lists = topicLists)
            .firstOrNull { it.myPubkey.toHex() == pubkeyProvider.getPubkeyHex() } ?: return

        scope.launch {
            topicUpsertDao.upsertTopics(validatedTopicList = myNewestList)
        }.invokeOnCompletion { exception ->
            if (exception != null) Log.w(TAG, "Failed to process topic list", exception)
        }
    }

    private fun filterNewestVotes(votes: Collection<ValidatedVote>): List<ValidatedVote> {
        val cache = mutableMapOf<PubkeyHex, EventIdHex>()
        val newest = mutableListOf<ValidatedVote>()
        for (vote in votes.sortedByDescending { it.createdAt }) {
            val isNew = cache.putIfAbsent(vote.pubkey.toHex(), vote.postId.toHex()) == null
            if (isNew) newest.add(vote)
        }

        return newest
    }

    private fun <T : ValidatedList> filterNewestLists(lists: Collection<T>): List<T> {
        val cache = mutableSetOf<PubkeyHex>()
        val newest = mutableListOf<T>()
        for (list in lists.sortedByDescending { it.createdAt }) {
            val isNew = cache.add(list.owner.toHex())
            if (isNew) newest.add(list)
        }

        return newest
    }
}
