package com.dluvian.voyage.data.interactor

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import com.dluvian.voyage.core.EventIdHex
import com.dluvian.voyage.data.event.POLL_U16
import com.dluvian.voyage.data.model.PostDetails
import com.dluvian.voyage.data.nostr.getClientTag
import com.dluvian.voyage.data.nostr.getEndsAt
import com.dluvian.voyage.data.room.dao.HashtagDao
import com.dluvian.voyage.data.room.dao.MainEventDao
import rust.nostr.sdk.Event

class PostDetailInspector(
    private val mainEventDao: MainEventDao,
    private val hashtagDao: HashtagDao,
) {
    val currentDetails: MutableState<PostDetails?> = mutableStateOf(null)

    suspend fun setPostDetails(postId: EventIdHex) {
        if (currentDetails.value?.base?.id == postId) return

        currentDetails.value = mainEventDao.getPostDetails(id = postId)?.let { base ->
            val event = kotlin.runCatching { Event.fromJson(json = base.json) }.getOrNull()
            PostDetails(
                indexedTopics = hashtagDao.getHashtags(postId = postId),
                client = event?.getClientTag(),
                pollEndsAt = if (event?.kind()?.asU16() == POLL_U16) event.getEndsAt() else null,
                base = base.copy(json = event?.asPrettyJson() ?: base.json),
            )
        }
    }

    fun closePostDetails() {
        currentDetails.value = null
    }
}
