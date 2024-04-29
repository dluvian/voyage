package com.dluvian.voyage.data.event

import android.util.Log
import com.dluvian.voyage.core.EventIdHex
import com.dluvian.voyage.core.PubkeyHex
import com.dluvian.voyage.core.toRelevantMetadata
import com.dluvian.voyage.data.account.IPubkeyProvider
import com.dluvian.voyage.data.inMemory.MetadataInMemory
import com.dluvian.voyage.data.room.AppDatabase
import com.dluvian.voyage.data.room.entity.FullProfileEntity
import com.dluvian.voyage.data.room.entity.ProfileEntity
import com.dluvian.voyage.data.room.entity.VoteEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


private const val TAG = "EventProcessor"

class EventProcessor(
    private val room: AppDatabase,
    private val metadataInMemory: MetadataInMemory,
    private val pubkeyProvider: IPubkeyProvider,
) {
    private val scope = CoroutineScope(Dispatchers.IO)

    fun processEvents(events: Collection<ValidatedEvent>) {
        if (events.isEmpty()) return

        val crossPosted = events.mapNotNull { if (it is ValidatedCrossPost) it.crossPost else null }
        val allEvents = (events + crossPosted).distinct()

        val rootPosts = mutableListOf<ValidatedRootPost>()
        val replies = mutableListOf<ValidatedReply>()
        val crossPosts = mutableListOf<ValidatedCrossPost>()
        val votes = mutableListOf<ValidatedVote>()
        val contactLists = mutableListOf<ValidatedContactList>()
        val topicLists = mutableListOf<ValidatedTopicList>()
        val nip65s = mutableListOf<ValidatedNip65>()
        val profiles = mutableListOf<ValidatedProfile>()

        allEvents.forEach { event ->
            when (event) {
                is ValidatedRootPost -> rootPosts.add(event)
                is ValidatedReply -> replies.add(event)
                is ValidatedCrossPost -> crossPosts.add(event)
                is ValidatedVote -> votes.add(event)
                is ValidatedContactList -> contactLists.add(event)
                is ValidatedTopicList -> topicLists.add(event)
                is ValidatedNip65 -> nip65s.add(event)
                is ValidatedProfile -> profiles.add(event)
            }
        }
        processRootPosts(rootPosts = rootPosts)
        processReplies(replies = replies)
        processCrossPosts(crossPosts = crossPosts)
        processVotes(votes = votes)
        processContactLists(contactLists = contactLists)
        processTopicLists(topicLists = topicLists)
        processNip65s(nip65s = nip65s)
        processProfiles(profiles = profiles)
    }

    private fun processRootPosts(rootPosts: Collection<ValidatedRootPost>) {
        if (rootPosts.isEmpty()) return

        scope.launch {
            room.postInsertDao().insertRootPosts(posts = rootPosts)
        }.invokeOnCompletion { exception ->
            if (exception != null) Log.w(TAG, "Failed to process root posts", exception)
        }
    }

    private fun processReplies(replies: Collection<ValidatedReply>) {
        if (replies.isEmpty()) return

        scope.launch {
            room.postInsertDao().insertReplies(replies = replies)
        }.invokeOnCompletion { exception ->
            if (exception != null) Log.w(TAG, "Failed to process replies", exception)
        }
    }

    private fun processCrossPosts(crossPosts: Collection<ValidatedCrossPost>) {
        Log.i("LOLOL", crossPosts.size.toString())
        if (crossPosts.isEmpty()) return

        scope.launch {
            room.postInsertDao().insertCrossPosts(crossPosts = crossPosts)
        }.invokeOnCompletion { exception ->
            if (exception != null) Log.w(TAG, "Failed to process reposts", exception)
        }
    }

    private fun processVotes(votes: Collection<ValidatedVote>) {
        if (votes.isEmpty()) return

        val entities = filterNewestVotes(votes = votes).map { VoteEntity.from(it) }
        if (entities.isEmpty()) return

        scope.launch {
            Log.d(TAG, "Insert ${entities.size}/${votes.size} votes")
            // We don't update new incoming votes. If a vote is in db, it will stay
            // RunCatching bc EventSweeper might delete parent post
            runCatching { room.voteDao().insertOrIgnoreVotes(voteEntities = entities) }
        }.invokeOnCompletion { ex ->
            if (ex != null) Log.w(TAG, "Failed to process ${entities.size}", ex)
        }
    }

    private fun processContactLists(contactLists: Collection<ValidatedContactList>) {
        if (contactLists.isEmpty()) return

        val newestLists = filterNewestLists(lists = contactLists)

        val myPubkey = pubkeyProvider.getPubkeyHex()
        val myList = newestLists.find { it.pubkey == myPubkey }
        val otherLists = newestLists.let { if (myList != null) it - myList else it }

        processFriendList(myFriendList = myList)
        processWebOfTrustList(validatedWoTs = otherLists)
    }

    private fun processFriendList(myFriendList: ValidatedContactList?) {
        if (myFriendList == null) return

        scope.launch {
            Log.d(TAG, "Upsert my friend list of ${myFriendList.friendPubkeys.size} friends")
            room.friendUpsertDao().upsertFriends(validatedContactList = myFriendList)
        }.invokeOnCompletion { exception ->
            if (exception != null) Log.w(TAG, "Failed to process friend list", exception)
        }
    }

    private fun processWebOfTrustList(validatedWoTs: Collection<ValidatedContactList>) {
        if (validatedWoTs.isEmpty()) return

        scope.launch {
            Log.d(TAG, "Upsert ${validatedWoTs.size} wot contact lists")
            room.webOfTrustUpsertDao().upsertWebOfTrust(validatedWoTs = validatedWoTs)
        }.invokeOnCompletion { ex ->
            if (ex != null) Log.w(TAG, "Failed to process web of trust list", ex)
        }
    }

    private fun processTopicLists(topicLists: Collection<ValidatedTopicList>) {
        if (topicLists.isEmpty()) return

        val myNewestList = filterNewestLists(lists = topicLists)
            .firstOrNull { it.myPubkey == pubkeyProvider.getPubkeyHex() } ?: return

        scope.launch {
            Log.d(TAG, "Upsert topic list of ${myNewestList.topics.size} topics")
            room.topicUpsertDao().upsertTopics(validatedTopicList = myNewestList)
        }.invokeOnCompletion { ex ->
            if (ex != null) Log.w(TAG, "Failed to process topic list", ex)
        }
    }

    private fun processNip65s(nip65s: Collection<ValidatedNip65>) {
        if (nip65s.isEmpty()) return

        val newestNip65s = filterNewestLists(lists = nip65s).filter { it.relays.isNotEmpty() }
        if (newestNip65s.isEmpty()) return

        scope.launch {
            Log.d(TAG, "Upsert ${nip65s.size} nip65s")
            room.nip65UpsertDao().upsertNip65s(validatedNip65s = newestNip65s)
        }.invokeOnCompletion { ex ->
            if (ex != null) Log.w(TAG, "Failed to process nip65", ex)
        }
    }

    private fun processProfiles(profiles: MutableList<ValidatedProfile>) {
        if (profiles.isEmpty()) return
        Log.d(TAG, "Process ${profiles.size} profiles")

        val uniqueProfiles = profiles
            .sortedByDescending { it.createdAt }
            .distinctBy { it.pubkey }

        uniqueProfiles.forEach { profile ->
            metadataInMemory.submit(
                pubkey = profile.pubkey,
                metadata = profile.metadata.toRelevantMetadata(createdAt = profile.createdAt)
            )
        }

        val entities = uniqueProfiles.map { ProfileEntity.from(it) }

        scope.launch {
            Log.d(TAG, "Upsert ${entities.size}/${profiles.size} profiles")
            room.profileUpsertDao().upsertProfiles(profiles = entities)
        }.invokeOnCompletion { ex ->
            if (ex != null) Log.w(TAG, "Failed to process profile ${entities.size}", ex)
        }

        val myPubkey = pubkeyProvider.getPubkeyHex()
        val myProfile = uniqueProfiles.find { it.pubkey == myPubkey } ?: return
        val myProfileEntity = FullProfileEntity.from(profile = myProfile)

        scope.launch {
            room.fullProfileUpsertDao().upsertProfile(profile = myProfileEntity)
        }.invokeOnCompletion { ex ->
            if (ex != null) Log.w(TAG, "Failed to process my profile $myProfileEntity", ex)
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
