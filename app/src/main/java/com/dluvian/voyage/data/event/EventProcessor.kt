package com.dluvian.voyage.data.event

import android.util.Log
import com.dluvian.voyage.core.EventIdHex
import com.dluvian.voyage.core.PubkeyHex
import com.dluvian.voyage.core.RelayedValidatedEvent
import com.dluvian.voyage.data.model.RelayedItem
import com.dluvian.voyage.data.room.dao.tx.FriendUpsertDao
import com.dluvian.voyage.data.room.dao.tx.Nip65UpsertDao
import com.dluvian.voyage.data.room.dao.tx.PostInsertDao
import com.dluvian.voyage.data.room.dao.tx.TopicUpsertDao
import com.dluvian.voyage.data.room.dao.tx.VoteUpsertDao
import com.dluvian.voyage.data.room.dao.tx.WebOfTrustUpsertDao
import com.dluvian.voyage.data.room.entity.VoteEntity
import com.dluvian.voyage.data.signer.IPubkeyProvider
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
    private val nip65UpsertDao: Nip65UpsertDao,
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
        val nip65s = mutableListOf<ValidatedNip65>()

        events.forEach { relayedItem ->
            when (val item = relayedItem.item) {
                is ValidatedRootPost ->
                    rootPosts.add(
                        RelayedItem(
                            item = item,
                            relayUrl = relayedItem.relayUrl
                        )
                    )

                is ValidatedReplyPost ->
                    replyPosts.add(
                        RelayedItem(
                            item = item,
                            relayUrl = relayedItem.relayUrl
                        )
                    )


                is ValidatedVote -> votes.add(item)
                is ValidatedContactList -> contactLists.add(item)
                is ValidatedTopicList -> topicLists.add(item)
                is ValidatedNip65 -> nip65s.add(item)
            }
        }

        processRootPosts(relayedRootPosts = rootPosts)
        processReplyPosts(relayedReplyPosts = replyPosts)
        processVotes(votes = votes)
        processContactLists(contactLists = contactLists)
        processTopicLists(topicLists = topicLists)
        processNip65s(nip65s = nip65s)
    }

    private fun processRootPosts(relayedRootPosts: Collection<RelayedItem<ValidatedRootPost>>) {
        if (relayedRootPosts.isEmpty()) return

        val sorted = relayedRootPosts.sortedBy { it.item.createdAt }
        scope.launch {
            postInsertDao.insertRootPosts(relayedPosts = sorted)
        }.invokeOnCompletion { exception ->
            if (exception != null) Log.w(TAG, "Failed to process root posts", exception)
        }
    }

    private fun processReplyPosts(relayedReplyPosts: Collection<RelayedItem<ValidatedReplyPost>>) {
        if (relayedReplyPosts.isEmpty()) return

        val sorted = relayedReplyPosts.sortedBy { it.item.createdAt }
        scope.launch {
            postInsertDao.insertReplyPosts(relayedPosts = sorted)
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
                if (exception != null) Log.w(
                    TAG,
                    "Failed to process vote ${vote.id}",
                    exception
                )
            }
        }
    }

    private fun processContactLists(contactLists: Collection<ValidatedContactList>) {
        if (contactLists.isEmpty()) return

        val newestLists = filterNewestLists(lists = contactLists)

        val myPubkey = pubkeyProvider.getPubkeyHex()
        val myList = newestLists.find { it.pubkey == myPubkey }
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
                if (exception != null) Log.w(
                    TAG,
                    "Failed to process web of trust list",
                    exception
                )
            }
        }
    }

    private fun processTopicLists(topicLists: Collection<ValidatedTopicList>) {
        if (topicLists.isEmpty()) return

        val myNewestList = filterNewestLists(lists = topicLists)
            .firstOrNull { it.myPubkey == pubkeyProvider.getPubkeyHex() } ?: return

        scope.launch {
            topicUpsertDao.upsertTopics(validatedTopicList = myNewestList)
        }.invokeOnCompletion { exception ->
            if (exception != null) Log.w(TAG, "Failed to process topic list", exception)
        }
    }

    private fun processNip65s(nip65s: Collection<ValidatedNip65>) {
        if (nip65s.isEmpty()) return

        val newestNip65s = filterNewestLists(lists = nip65s)

        newestNip65s.forEach { nip65 ->
            scope.launch {
                nip65UpsertDao.upsertNip65(validatedNip65 = nip65)
            }.invokeOnCompletion { exception ->
                if (exception != null) Log.w(TAG, "Failed to process nip65", exception)
            }
        }
    }

    private fun filterNewestVotes(votes: Collection<ValidatedVote>): List<ValidatedVote> {
        val cache = mutableMapOf<PubkeyHex, EventIdHex>()
        val newest = mutableListOf<ValidatedVote>()
        for (vote in votes.sortedByDescending { it.createdAt }) {
            val isNew = cache.putIfAbsent(vote.pubkey, vote.postId) == null
            if (isNew) newest.add(vote)
        }

        return newest
    }

    private fun <T : ValidatedList> filterNewestLists(lists: Collection<T>): List<T> {
        val cache = mutableSetOf<PubkeyHex>()
        val newest = mutableListOf<T>()
        for (list in lists.sortedByDescending { it.createdAt }) {
            val isNew = cache.add(list.owner)
            if (isNew) newest.add(list)
        }

        return newest
    }
}
