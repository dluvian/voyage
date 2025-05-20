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
import com.dluvian.voyage.data.room.entity.main.VoteEntity
import com.dluvian.voyage.data.room.entity.main.poll.PollResponseEntity
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
            if (it is ValidatedCrossPost) it.crossPostedThreadableEvent else null
        }
        val allEvents = (events + crossPosted).distinct()

        val rootPosts = mutableListOf<ValidatedRootPost>()
        val legacyReplies = mutableListOf<ValidatedLegacyReply>()
        val votes = mutableListOf<ValidatedVote>()
        val comments = mutableListOf<ValidatedComment>()
        val crossPosts = mutableListOf<ValidatedCrossPost>()
        val polls = mutableListOf<ValidatedPoll>()
        val pollResponses = mutableListOf<ValidatedPollResponse>()
        val profiles = mutableListOf<ValidatedProfile>()
        val profileSets = mutableListOf<ValidatedProfileSet>()
        val topicSets = mutableListOf<ValidatedTopicSet>()
        val lists = mutableListOf<ValidatedList>()
        val locks = mutableListOf<ValidatedLock>()

        allEvents.forEach { event ->
            when (event) {
                is ValidatedRootPost -> rootPosts.add(event)
                is ValidatedLegacyReply -> legacyReplies.add(event)
                is ValidatedCrossPost -> crossPosts.add(event)
                is ValidatedVote -> votes.add(event)
                is ValidatedComment -> comments.add(event)
                is ValidatedPoll -> polls.add(event)
                is ValidatedPollResponse -> pollResponses.add(event)
                is ValidatedProfile -> profiles.add(event)
                is ValidatedProfileSet -> profileSets.add(event)
                is ValidatedTopicSet -> topicSets.add(event)
                is ValidatedList -> lists.add(event)
                is ValidatedLock -> locks.add(event)
            }
        }
        processRootPosts(roots = rootPosts)
        processComments(comments = comments)
        processCrossPosts(crossPosts = crossPosts)
        processPolls(polls = polls)
        processLegacyReplies(legacyReplies = legacyReplies)
        processPollResponses(responses = pollResponses)
        processVotes(votes = votes)
        processProfiles(profiles = profiles)
        processProfileSets(sets = profileSets)
        processTopicSets(sets = topicSets)
        processLocks(locks = locks)
        listEventProcessor.processLists(lists = lists)
    }

    private fun processRootPosts(roots: Collection<ValidatedRootPost>) {
        if (roots.isEmpty()) return

        scope.launch {
            room.mainEventInsertDao().insertRootPosts(roots = roots)
        }.invokeOnCompletion { exception ->
            if (exception != null) Log.w(TAG, "Failed to process root posts", exception)
        }
    }

    private fun processLegacyReplies(legacyReplies: Collection<ValidatedLegacyReply>) {
        if (legacyReplies.isEmpty()) return

        scope.launch {
            room.mainEventInsertDao().insertLegacyReplies(replies = legacyReplies)
        }.invokeOnCompletion { exception ->
            if (exception != null) Log.w(TAG, "Failed to process replies", exception)
        }
    }

    private fun processComments(comments: Collection<ValidatedComment>) {
        if (comments.isEmpty()) return

        scope.launch {
            room.mainEventInsertDao().insertComments(comments = comments)
        }.invokeOnCompletion { exception ->
            if (exception != null) Log.w(TAG, "Failed to process comments", exception)
        }
    }

    private fun processCrossPosts(crossPosts: Collection<ValidatedCrossPost>) {
        if (crossPosts.isEmpty()) return

        scope.launch {
            room.mainEventInsertDao().insertCrossPosts(crossPosts = crossPosts)
        }.invokeOnCompletion { exception ->
            if (exception != null) Log.w(TAG, "Failed to process cross posts", exception)
        }
    }

    private fun processVotes(votes: Collection<ValidatedVote>) {
        if (votes.isEmpty()) return

        val entities = filterNewestVotes(votes = votes).map { VoteEntity.from(it) }
        if (entities.isEmpty()) return

        scope.launch {
            // We don't update new incoming votes. If a vote is in db, it will stay
            // RunCatching bc EventSweeper might delete parent post
            runCatching { room.voteDao().insertOrIgnoreVotes(voteEntities = entities) }
        }.invokeOnCompletion { ex ->
            if (ex != null) {
                Log.w(TAG, "Failed to process ${entities.size} votes", ex)
            }
        }
    }

    private fun processPolls(polls: Collection<ValidatedPoll>) {
        if (polls.isEmpty()) return

        scope.launch {
            room.mainEventInsertDao().insertPolls(polls = polls)
        }.invokeOnCompletion { exception ->
            if (exception != null) Log.w(TAG, "Failed to process polls", exception)
        }
    }

    private fun processPollResponses(responses: Collection<ValidatedPollResponse>) {
        if (responses.isEmpty()) return

        val entities = responses.map { PollResponseEntity.from(response = it) }

        scope.launch {
            // We don't update new incoming responses. If a response is in db, it will stay
            // RunCatching bc EventSweeper might delete parent poll
            runCatching { room.pollResponseDao().insertOrIgnoreResponses(responses = entities) }
        }.invokeOnCompletion { ex ->
            if (ex != null) {
                Log.w(TAG, "Failed to process ${entities.size} responses", ex)
            }
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

        scope.launch {
            Log.i(TAG, "Insert ${locks.size} locks")
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
            val isNew = cache.putIfAbsent(vote.pubkey, vote.eventId) == null
            if (isNew) newest.add(vote)
        }

        return newest
    }

    private fun <T : ValidatedSet> filterNewestSets(sets: Collection<T>): List<T> {
        return sets.sortedByDescending { it.createdAt }.distinctBy { it.identifier }
    }
}
