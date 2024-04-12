package com.dluvian.voyage.data.interactor

import android.content.Context
import android.util.Log
import androidx.compose.material3.SnackbarHostState
import com.dluvian.nostr_kt.getHashtags
import com.dluvian.nostr_kt.secs
import com.dluvian.voyage.R
import com.dluvian.voyage.core.DEBOUNCE
import com.dluvian.voyage.core.FollowTopic
import com.dluvian.voyage.core.SignerLauncher
import com.dluvian.voyage.core.Topic
import com.dluvian.voyage.core.TopicEvent
import com.dluvian.voyage.core.UnfollowTopic
import com.dluvian.voyage.core.showToast
import com.dluvian.voyage.data.event.ValidatedTopicList
import com.dluvian.voyage.data.nostr.NostrService
import com.dluvian.voyage.data.provider.RelayProvider
import com.dluvian.voyage.data.room.dao.TopicDao
import com.dluvian.voyage.data.room.dao.tx.TopicUpsertDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException

class TopicFollower(
    private val nostrService: NostrService,
    private val relayProvider: RelayProvider,
    private val topicUpsertDao: TopicUpsertDao,
    private val topicDao: TopicDao,
    private val snackbar: SnackbarHostState,
    private val context: Context,
    private val forcedFollowStates: MutableStateFlow<Map<Topic, Boolean>>
) {
    private val tag = "TopicFollower"
    private val scope = CoroutineScope(Dispatchers.IO)

    fun handle(action: TopicEvent) {
        when (action) {
            is FollowTopic -> handleAction(
                topic = action.topic,
                isFollowed = true,
                signerLauncher = action.signerLauncher
            )

            is UnfollowTopic -> handleAction(
                topic = action.topic,
                isFollowed = false,
                signerLauncher = action.signerLauncher
            )
        }
    }

    private val jobs: MutableMap<Topic, Job?> = mutableMapOf()
    private fun handleAction(topic: Topic, isFollowed: Boolean, signerLauncher: SignerLauncher) {
        updateForcedState(topic = topic, isFollowed = isFollowed)

        jobs[topic]?.cancel(CancellationException("User clicks fast"))
        jobs[topic] = scope.launch {
            delay(DEBOUNCE)

            val allTopics = topicDao.getMyTopics().toMutableSet()
            if (isFollowed) allTopics.add(topic) else allTopics.remove(topic)

            nostrService.publishTopicList(
                topics = allTopics.toList(),
                relayUrls = relayProvider.getPublishRelays(),
                signerLauncher = signerLauncher,
            ).onSuccess { event ->
                val topicList = ValidatedTopicList(
                    myPubkey = event.author().toHex(),
                    topics = event.getHashtags().toSet(),
                    createdAt = event.createdAt().secs()
                )
                topicUpsertDao.upsertTopics(validatedTopicList = topicList)
            }
                .onFailure {
                    Log.w(tag, "Failed to publish topic list: ${it.message}", it)
                    snackbar.showToast(
                        scope = scope,
                        msg = context.getString(R.string.failed_to_sign_topic_list)
                    )
                }
        }
    }

    private fun updateForcedState(topic: Topic, isFollowed: Boolean) {
        synchronized(forcedFollowStates) {
            val mutable = forcedFollowStates.value.toMutableMap()
            mutable[topic] = isFollowed
            forcedFollowStates.value = mutable
        }
    }
}
