package com.dluvian.voyage.data.event

import android.util.Log
import com.dluvian.voyage.core.EventIdHex
import com.dluvian.voyage.core.PubkeyHex
import com.dluvian.voyage.core.utils.toRelevantMetadata
import com.dluvian.voyage.data.account.IMyPubkeyProvider
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
    private val myPubkeyProvider: IMyPubkeyProvider,
) {
    private val scope = CoroutineScope(Dispatchers.IO)
    private val listEventProcessor = ListEventProcessor(
        scope = scope,
        myPubkeyProvider = myPubkeyProvider,
        room = room
    )

    fun processEvents(events: Collection<ValidatedEvent>) {
        if (events.isEmpty()) return

        val crossPosted = events.mapNotNull {
            if (it is ValidatedCrossPost) it.crossPosted else null
        }
        val allEvents = (events + crossPosted).distinct()

        val rootPosts = mutableListOf<ValidatedRootPost>()
        val replies = mutableListOf<ValidatedReply>()
        val crossPosts = mutableListOf<ValidatedCrossPost>()
        val votes = mutableListOf<ValidatedVote>()
        val profiles = mutableListOf<ValidatedProfile>()
        val profileSets = mutableListOf<ValidatedProfileSet>()
        val topicSets = mutableListOf<ValidatedTopicSet>()
        val lists = mutableListOf<ValidatedList>()
        val locks = mutableListOf<ValidatedLock>()

        allEvents.forEach { event ->
            when (event) {
                is ValidatedRootPost -> rootPosts.add(event)
                is ValidatedReply -> replies.add(event)
                is ValidatedCrossPost -> crossPosts.add(event)
                is ValidatedVote -> votes.add(event)
                is ValidatedProfile -> profiles.add(event)
                is ValidatedProfileSet -> profileSets.add(event)
                is ValidatedTopicSet -> topicSets.add(event)
                is ValidatedList -> lists.add(event)
                is ValidatedLock -> locks.add(event)
            }
        }
        processRootPosts(rootPosts = rootPosts)
        processReplies(replies = replies)
        processCrossPosts(crossPosts = crossPosts)
        processVotes(votes = votes)
        processProfiles(profiles = profiles)
        processProfileSets(sets = profileSets)
        processTopicSets(sets = topicSets)
        processLocks(locks = locks)
        listEventProcessor.processLists(lists = lists)
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
        if (crossPosts.isEmpty()) return

        scope.launch {
            room.postInsertDao().insertCrossPosts(crossPosts = crossPosts)
        }.invokeOnCompletion { exception ->
            if (exception != null) Log.w(TAG, "Failed to process cross posts ", exception)
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

    private fun processProfileSets(sets: Collection<ValidatedProfileSet>) {
        if (sets.isEmpty()) return

        val myNewestSets = filterNewestSets(sets = sets)
            .filter { it.myPubkey == myPubkeyProvider.getPubkeyHex() }
        if (myNewestSets.isEmpty()) return

        scope.launch {
            myNewestSets.forEach {
                Log.d(TAG, "Upsert set with ${it.pubkeys.size} pubkeys")
                room.profileSetUpsertDao().upsertSet(set = it)
            }
        }
    }

    private fun processTopicSets(sets: Collection<ValidatedTopicSet>) {
        if (sets.isEmpty()) return

        val myNewestSets = filterNewestSets(sets = sets)
            .filter { it.myPubkey == myPubkeyProvider.getPubkeyHex() }
        if (myNewestSets.isEmpty()) return

        scope.launch {
            myNewestSets.forEach {
                Log.d(TAG, "Upsert set with ${it.topics.size} topics")
                room.topicSetUpsertDao().upsertSet(set = it)
            }
        }
    }

    private fun processLocks(locks: Collection<ValidatedLock>) {
        if (locks.isEmpty()) return

        val unique = locks.distinctBy { it.pubkey }

        scope.launch {
            Log.i(TAG, "Insert ${unique.size} locks")
            room.lockInsertDao().insertLocksTx(locks = locks)
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
                metadata = profile.metadata.toRelevantMetadata(
                    pubkey = profile.pubkey,
                    createdAt = profile.createdAt
                )
            )
        }

        val entities = uniqueProfiles.map { ProfileEntity.from(it) }

        scope.launch {
            Log.d(TAG, "Upsert ${entities.size}/${profiles.size} profiles")
            room.profileUpsertDao().upsertProfiles(profiles = entities)
        }.invokeOnCompletion { ex ->
            if (ex != null) Log.w(TAG, "Failed to process profile ${entities.size}", ex)
        }

        val myPubkey = myPubkeyProvider.getPubkeyHex()
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

    private fun <T : ValidatedSet> filterNewestSets(sets: Collection<T>): List<T> {
        return sets.sortedByDescending { it.createdAt }.distinctBy { it.identifier }
    }
}
