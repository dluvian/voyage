package com.dluvian.voyage.data.event

import android.util.Log
import com.dluvian.voyage.core.EventIdHex
import com.dluvian.voyage.core.PubkeyHex
import com.dluvian.voyage.core.RelayedValidatedEvent
import com.dluvian.voyage.core.toRelevantMetadata
import com.dluvian.voyage.data.account.IPubkeyProvider
import com.dluvian.voyage.data.inMemory.MetadataInMemory
import com.dluvian.voyage.data.model.RelayedItem
import com.dluvian.voyage.data.room.AppDatabase
import com.dluvian.voyage.data.room.entity.ProfileEntity
import com.dluvian.voyage.data.room.entity.VoteEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class EventProcessor(
    private val room: AppDatabase,
    private val metadataInMemory: MetadataInMemory,
    private val pubkeyProvider: IPubkeyProvider,
) {
    private val tag = "EventProcessor"
    private val scope = CoroutineScope(Dispatchers.IO)

    fun processEvents(events: Set<RelayedValidatedEvent>) {
        if (events.isEmpty()) return

        val rootPosts = mutableListOf<RelayedItem<ValidatedRootPost>>()
        val replyPosts = mutableListOf<RelayedItem<ValidatedReplyPost>>()
        val votes = mutableListOf<ValidatedVote>()
        val contactLists = mutableListOf<ValidatedContactList>()
        val topicLists = mutableListOf<ValidatedTopicList>()
        val nip65s = mutableListOf<ValidatedNip65>()
        val profiles = mutableListOf<ValidatedProfile>()

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
                is ValidatedProfile -> profiles.add(item)
            }
        }

        processRootPosts(relayedRootPosts = rootPosts)
        processReplyPosts(relayedReplyPosts = replyPosts)
        processVotes(votes = votes)
        processContactLists(contactLists = contactLists)
        processTopicLists(topicLists = topicLists)
        processNip65s(nip65s = nip65s)
        processProfiles(profiles = profiles)
    }

    private fun processRootPosts(relayedRootPosts: Collection<RelayedItem<ValidatedRootPost>>) {
        if (relayedRootPosts.isEmpty()) return

        val sorted = relayedRootPosts.sortedBy { it.item.createdAt }
        scope.launch {
            room.postInsertDao().insertRootPosts(relayedPosts = sorted)
        }.invokeOnCompletion { exception ->
            if (exception != null) Log.w(tag, "Failed to process root posts", exception)
        }
    }

    private fun processReplyPosts(relayedReplyPosts: Collection<RelayedItem<ValidatedReplyPost>>) {
        if (relayedReplyPosts.isEmpty()) return

        val sorted = relayedReplyPosts.sortedBy { it.item.createdAt }
        scope.launch {
            room.postInsertDao().insertReplyPosts(relayedPosts = sorted)
        }.invokeOnCompletion { exception ->
            if (exception != null) Log.w(tag, "Failed to process reply posts", exception)
        }
    }

    private fun processVotes(votes: Collection<ValidatedVote>) {
        if (votes.isEmpty()) return

        filterNewestVotes(votes = votes)
            .map { VoteEntity.from(it) }
            .forEach { vote ->
                scope.launch {
                    room.voteUpsertDao().upsertVote(voteEntity = vote)
                }.invokeOnCompletion { ex ->
                    if (ex != null) Log.w(tag, "Failed to process vote ${vote.id}", ex)
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
        Log.d(tag, "Process my friend list")

        scope.launch {
            room.friendUpsertDao().upsertFriends(validatedContactList = myFriendList)
        }.invokeOnCompletion { exception ->
            if (exception != null) Log.w(tag, "Failed to process friend list", exception)
        }
    }

    private fun processWebOfTrustList(webOfTrustList: Collection<ValidatedContactList>) {
        if (webOfTrustList.isEmpty()) return
        Log.d(tag, "Process ${webOfTrustList.size} wot contact lists")

        webOfTrustList.forEach { wot ->
            scope.launch {
                room.webOfTrustUpsertDao().upsertWebOfTrust(validatedWebOfTrust = wot)
            }.invokeOnCompletion { ex ->
                if (ex != null) Log.w(tag, "Failed to process web of trust list", ex)
            }
        }
    }

    private fun processTopicLists(topicLists: Collection<ValidatedTopicList>) {
        if (topicLists.isEmpty()) return
        Log.d(tag, "Process ${topicLists.size} topic lists")

        val myNewestList = filterNewestLists(lists = topicLists)
            .firstOrNull { it.myPubkey == pubkeyProvider.getPubkeyHex() } ?: return

        scope.launch {
            room.topicUpsertDao().upsertTopics(validatedTopicList = myNewestList)
        }.invokeOnCompletion { exception ->
            if (exception != null) Log.w(tag, "Failed to process topic list", exception)
        }
    }

    private fun processNip65s(nip65s: Collection<ValidatedNip65>) {
        if (nip65s.isEmpty()) return
        Log.d(tag, "Process ${nip65s.size} nip65s")

        val newestNip65s = filterNewestLists(lists = nip65s)

        newestNip65s.forEach { nip65 ->
            scope.launch {
                room.nip65UpsertDao().upsertNip65(validatedNip65 = nip65)
            }.invokeOnCompletion { exception ->
                if (exception != null) Log.w(tag, "Failed to process nip65", exception)
            }
        }
    }

    private fun processProfiles(profiles: MutableList<ValidatedProfile>) {
        if (profiles.isEmpty()) return
        Log.d(tag, "Process ${profiles.size} profiles")

        profiles
            .sortedByDescending { it.createdAt }
            .distinctBy { it.pubkey }
            .forEach { profile ->
                metadataInMemory.submit(
                    pubkey = profile.pubkey,
                    metadata = profile.metadata.toRelevantMetadata(createdAt = profile.createdAt)
                )
                scope.launch {
                    val entity = ProfileEntity.from(validatedProfile = profile)
                    room.profileUpsertDao().upsertProfile(profile = entity)
                }.invokeOnCompletion { ex ->
                    if (ex != null) Log.w(tag, "Failed to process profile ${profile.id}", ex)
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
