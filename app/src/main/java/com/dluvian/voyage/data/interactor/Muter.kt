package com.dluvian.voyage.data.interactor

import android.content.Context
import android.util.Log
import androidx.compose.material3.SnackbarHostState
import com.dluvian.voyage.R
import com.dluvian.voyage.core.MAX_KEYS_SQL
import com.dluvian.voyage.core.MuteEvent
import com.dluvian.voyage.core.MuteProfile
import com.dluvian.voyage.core.MuteTopic
import com.dluvian.voyage.core.PubkeyHex
import com.dluvian.voyage.core.Topic
import com.dluvian.voyage.core.UnmuteProfile
import com.dluvian.voyage.core.UnmuteTopic
import com.dluvian.voyage.core.launchIO
import com.dluvian.voyage.core.showToast
import com.dluvian.voyage.data.event.ValidatedMuteList
import com.dluvian.voyage.data.nostr.NostrService
import com.dluvian.voyage.data.nostr.secs
import com.dluvian.voyage.data.provider.RelayProvider
import com.dluvian.voyage.data.room.dao.MuteDao
import com.dluvian.voyage.data.room.dao.tx.MuteUpsertDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

private const val TAG = "Muter"

class Muter(
    private val forcedTopicMuteFlow: MutableStateFlow<Map<Topic, Boolean>>,
    private val nostrService: NostrService,
    private val relayProvider: RelayProvider,
    private val muteUpsertDao: MuteUpsertDao,
    private val muteDao: MuteDao,
    private val snackbar: SnackbarHostState,
    private val context: Context,
) {
    private val scope = CoroutineScope(Dispatchers.IO)

    private val _forcedProfileMutes = MutableStateFlow(mapOf<PubkeyHex, Boolean>())
    val forcedProfileMuteFlow = _forcedProfileMutes.stateIn(
        scope,
        SharingStarted.Eagerly,
        _forcedProfileMutes.value
    )

    fun handle(action: MuteEvent) {
        when (action) {
            is MuteProfile -> handleProfileAction(pubkey = action.pubkey, isMuted = true)
            is UnmuteProfile -> handleProfileAction(pubkey = action.pubkey, isMuted = false)
            is MuteTopic -> handleTopicAction(topic = action.topic, isMuted = true)
            is UnmuteTopic -> handleTopicAction(topic = action.topic, isMuted = false)
        }
    }

    private fun handleProfileAction(pubkey: PubkeyHex, isMuted: Boolean) {
        updateForcedProfileStates(pubkey = pubkey, isMuted = isMuted)
        handleMutes()
    }

    private fun handleTopicAction(topic: Topic, isMuted: Boolean) {
        updateForcedTopicStates(topic = topic, isMuted = isMuted)
        handleMutes()
    }

    private fun updateForcedProfileStates(pubkey: PubkeyHex, isMuted: Boolean) {
        _forcedProfileMutes.update {
            val mutable = it.toMutableMap()
            mutable[pubkey] = isMuted
            mutable
        }
    }

    private fun updateForcedTopicStates(topic: Topic, isMuted: Boolean) {
        forcedTopicMuteFlow.update {
            val mutable = it.toMutableMap()
            mutable[topic] = isMuted
            mutable
        }
    }

    private var job: Job? = null
    private fun handleMutes() {
        if (job?.isActive == true) return

        job = scope.launchIO {
            val toHandleProfiles = _forcedProfileMutes.value.toMap()
            val toHandleTopics = forcedTopicMuteFlow.value.toMap()

            val beforeProfiles = muteDao.getMyProfileMutes().toSet()
            val beforeTopics = muteDao.getMyTopicMutes().toSet()

            val adjustedProfiles = beforeProfiles.toMutableSet().apply {
                addAll(
                    toHandleProfiles.filter { (_, bool) -> bool }.map { (pubkey, _) -> pubkey }
                )
                removeAll(
                    toHandleProfiles.filterNot { (_, bool) -> bool }
                        .map { (pubkey, _) -> pubkey }
                        .toSet()
                )
            }

            val adjustedTopics = beforeTopics.toMutableSet().apply {
                addAll(
                    toHandleTopics.filter { (_, bool) -> bool }.map { (pubkey, _) -> pubkey }
                )
                removeAll(
                    toHandleTopics.filterNot { (_, bool) -> bool }
                        .map { (pubkey, _) -> pubkey }
                        .toSet()
                )
            }

            if (beforeProfiles == adjustedProfiles && beforeTopics == adjustedTopics) {
                return@launchIO
            }

            val beforeSum = beforeProfiles.size + beforeTopics.size
            val afterSum = adjustedProfiles.size + adjustedTopics.size
            if (afterSum > MAX_KEYS_SQL && afterSum > beforeSum) {
                Log.w(TAG, "New mute list is too large ($afterSum)")
                adjustedProfiles
                    .minus(beforeProfiles)
                    .forEach { updateForcedProfileStates(pubkey = it, isMuted = false) }
                adjustedTopics
                    .minus(beforeTopics)
                    .forEach { updateForcedTopicStates(topic = it, isMuted = false) }
                val msg = context.getString(
                    R.string.bookmarking_more_than_n_is_not_allowed,
                    MAX_KEYS_SQL
                )
                snackbar.showToast(scope = scope, msg = msg)
                return@launchIO
            }

            nostrService.publishMuteList(
                pubkeys = adjustedProfiles.toList(),
                topics = adjustedTopics.toList(),
                relayUrls = relayProvider.getPublishRelays(addConnected = false),
            ).onSuccess { event ->
                val mutes = ValidatedMuteList(
                    myPubkey = event.author().toHex(),
                    pubkeys = event.publicKeys().map { it.toHex() }.toSet(),
                    topics = event.hashtags().toSet(),
                    createdAt = event.createdAt().secs()
                )
                muteUpsertDao.upsertMuteList(muteList = mutes)
            }
                .onFailure {
                    Log.w(TAG, "Failed to publish mute list: ${it.message}", it)
                    snackbar.showToast(
                        scope = scope,
                        msg = context.getString(R.string.failed_to_sign_mute_list)
                    )
                }
        }
    }
}