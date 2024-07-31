package com.dluvian.voyage.data.interactor

import android.content.Context
import androidx.compose.material3.SnackbarHostState
import com.dluvian.voyage.core.LIST_CHANGE_DEBOUNCE
import com.dluvian.voyage.core.MuteEvent
import com.dluvian.voyage.core.MuteProfile
import com.dluvian.voyage.core.MuteTopic
import com.dluvian.voyage.core.MuteWord
import com.dluvian.voyage.core.PubkeyHex
import com.dluvian.voyage.core.Topic
import com.dluvian.voyage.core.UnmuteProfile
import com.dluvian.voyage.core.UnmuteTopic
import com.dluvian.voyage.core.UnmuteWord
import com.dluvian.voyage.core.utils.launchIO
import com.dluvian.voyage.data.nostr.NostrService
import com.dluvian.voyage.data.provider.RelayProvider
import com.dluvian.voyage.data.room.dao.MuteDao
import com.dluvian.voyage.data.room.dao.tx.MuteUpsertDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
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

    private val _forcedWordMutes = MutableStateFlow(mapOf<String, Boolean>())

    fun handle(action: MuteEvent) {
        when (action) {
            is MuteProfile -> handleProfileAction(
                pubkey = action.pubkey,
                isMuted = true,
                debounce = action.debounce
            )

            is UnmuteProfile -> handleProfileAction(
                pubkey = action.pubkey,
                isMuted = false,
                debounce = action.debounce
            )

            is MuteTopic -> handleTopicAction(
                topic = action.topic,
                isMuted = true,
                debounce = action.debounce
            )

            is UnmuteTopic -> handleTopicAction(
                topic = action.topic,
                isMuted = false,
                debounce = action.debounce
            )

            is MuteWord -> TODO()
            is UnmuteWord -> TODO()
        }
    }

    private fun handleProfileAction(pubkey: PubkeyHex, isMuted: Boolean, debounce: Boolean) {
        updateForcedProfileStates(pubkey = pubkey, isMuted = isMuted)
        handleMutes(debounce = debounce)
    }

    private fun handleTopicAction(topic: Topic, isMuted: Boolean, debounce: Boolean) {
        updateForcedTopicStates(topic = topic, isMuted = isMuted)
        handleMutes(debounce = debounce)
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
    private fun handleMutes(debounce: Boolean) {
        if (job?.isActive == true) return

        job = scope.launchIO {
            if (debounce) delay(LIST_CHANGE_DEBOUNCE)

            val toHandleProfiles = _forcedProfileMutes.value.toMap()
            val toHandleTopics = forcedTopicMuteFlow.value.toMap()
            val toHandleWords = _forcedWordMutes.value.toMap()

            val beforeMutes = muteDao.getMyMutes().toSet()

            val adjustedProfiles = beforeMutes.filter { it.tag == "p" }
                .map { it.mutedItem }
                .toMutableSet()
                .apply {
                    addAll(
                        toHandleProfiles.filter { (_, bool) -> bool }.map { (pubkey, _) -> pubkey }
                    )
                    removeAll(
                        toHandleProfiles.filterNot { (_, bool) -> bool }
                            .map { (pubkey, _) -> pubkey }
                            .toSet()
                    )
                }

            val adjustedTopics = beforeMutes.filter { it.tag == "t" }
                .map { it.mutedItem }
                .toMutableSet()
                .apply {
                    addAll(
                        toHandleTopics.filter { (_, bool) -> bool }.map { (pubkey, _) -> pubkey }
                    )
                    removeAll(
                        toHandleTopics.filterNot { (_, bool) -> bool }
                            .map { (pubkey, _) -> pubkey }
                            .toSet()
                    )
                }

            val adjustedWords = beforeMutes.filter { it.tag == "word" }
                .map { it.mutedItem }
                .toMutableSet()
                .apply {
                    addAll(
                        toHandleWords.filter { (_, bool) -> bool }.map { (word, _) -> word }
                    )
                    removeAll(
                        toHandleWords.filterNot { (_, bool) -> bool }
                            .map { (word, _) -> word }
                            .toSet()
                    )
                }

            TODO("Make it more readable")

//            if (beforeProfiles == adjustedProfiles && beforeTopics == adjustedTopics) {
//                return@launchIO
//            }
//
//            val beforeSum = beforeProfiles.size + beforeTopics.size
//            val afterSum = adjustedProfiles.size + adjustedTopics.size
//            if (afterSum > MAX_KEYS_SQL && afterSum > beforeSum) {
//                Log.w(TAG, "New mute list is too large ($afterSum)")
//                adjustedProfiles
//                    .minus(beforeProfiles)
//                    .forEach { updateForcedProfileStates(pubkey = it, isMuted = false) }
//                adjustedTopics
//                    .minus(beforeTopics)
//                    .forEach { updateForcedTopicStates(topic = it, isMuted = false) }
//                val msg = context.getString(
//                    R.string.muting_more_than_n_is_not_allowed,
//                    MAX_KEYS_SQL
//                )
//                snackbar.showToast(scope = scope, msg = msg)
//                return@launchIO
//            }
//
//            nostrService.publishMuteList(
//                pubkeys = adjustedProfiles.toList(),
//                topics = adjustedTopics.toList(),
//                words = adjustedWords.toList(),
//                relayUrls = relayProvider.getPublishRelays(addConnected = false),
//            ).onSuccess { event ->
//                val mutes = ValidatedMuteList(
//                    myPubkey = event.author().toHex(),
//                    pubkeys = event.publicKeys().map { it.toHex() }.toSet(),
//                    topics = event.hashtags().toSet(),
//                    words = event.getMuteWords().toSet(),
//                    createdAt = event.createdAt().secs()
//                )
//                muteUpsertDao.upsertMuteList(muteList = mutes)
//            }
//                .onFailure {
//                    Log.w(TAG, "Failed to publish mute list: ${it.message}", it)
//                    snackbar.showToast(
//                        scope = scope,
//                        msg = context.getString(R.string.failed_to_sign_mute_list)
//                    )
//                }
        }
    }
}
