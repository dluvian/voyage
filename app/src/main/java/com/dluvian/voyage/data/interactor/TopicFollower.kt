package com.dluvian.voyage.data.interactor

import android.content.Context
import android.util.Log
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.mutableStateOf
import com.dluvian.nostr_kt.getHashtags
import com.dluvian.nostr_kt.secs
import com.dluvian.voyage.R
import com.dluvian.voyage.core.DEBOUNCE
import com.dluvian.voyage.core.Topic
import com.dluvian.voyage.core.showToast
import com.dluvian.voyage.data.event.ValidatedTopicList
import com.dluvian.voyage.data.nostr.NostrService
import com.dluvian.voyage.data.provider.RelayProvider
import com.dluvian.voyage.data.provider.TopicProvider
import com.dluvian.voyage.data.room.dao.tx.TopicUpsertDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException

class TopicFollower(
    private val nostrService: NostrService,
    private val topicProvider: TopicProvider,
    private val relayProvider: RelayProvider,
    private val topicUpsertDao: TopicUpsertDao,
    private val snackbar: SnackbarHostState,
    private val context: Context,
) {
    private val tag = "TopicFollower"
    private val scope = CoroutineScope(Dispatchers.IO)
    val forcedStates = mutableStateOf(mapOf<Topic, Boolean>())

    fun follow(topic: Topic) {
        handleAction(topic = topic, isFollowed = true)
    }

    fun unfollow(topic: Topic) {
        handleAction(topic = topic, isFollowed = false)
    }

    private val jobs: MutableMap<Topic, Job?> = mutableMapOf()
    private fun handleAction(topic: Topic, isFollowed: Boolean) {
        updateForcedState(topic = topic, isFollowed = isFollowed)

        jobs[topic]?.cancel(CancellationException("User clicks fast"))
        jobs[topic] = scope.launch {
            delay(DEBOUNCE)

            val allTopics = topicProvider.getMyTopics().toMutableSet()
            if (isFollowed) allTopics.add(topic) else allTopics.remove(topic)

            nostrService.publishTopicList(
                topics = allTopics.toList(),
                relayUrls = relayProvider.getReadRelays()
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
        synchronized(forcedStates) {
            val mutable = forcedStates.value.toMutableMap()
            mutable[topic] = isFollowed
            forcedStates.value = mutable
        }
    }
}
