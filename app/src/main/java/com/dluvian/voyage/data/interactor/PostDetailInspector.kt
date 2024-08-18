package com.dluvian.voyage.data.interactor

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import com.dluvian.voyage.core.EventIdHex
import com.dluvian.voyage.data.model.PostDetails
import com.dluvian.voyage.data.room.dao.PostDao

class PostDetailInspector(private val postDao: PostDao) {
    val currentDetails: MutableState<PostDetails?> = mutableStateOf(null)

    suspend fun setPostDetails(postId: EventIdHex) {
        if (currentDetails.value?.id == postId) return

        currentDetails.value = postDao.getPostDetails(id = postId)
    }

    fun closePostDetails() {
        currentDetails.value = null
    }
}
