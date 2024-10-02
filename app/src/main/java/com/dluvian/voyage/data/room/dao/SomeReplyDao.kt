package com.dluvian.voyage.data.room.dao

import androidx.room.Dao
import androidx.room.Query
import com.dluvian.voyage.core.EventIdHex
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

@Dao
interface SomeReplyDao {

    suspend fun getParentRef(id: EventIdHex): EventIdHex? {
        return internalGetLegacyReplyParentId(id = id) ?: internalGetCommentParentId(id = id)
    }

    fun getReplyCountFlow(parentId: EventIdHex): Flow<Int> {
        return combine(
            internalGetLegacyReplyCountFlow(parentId = parentId),
            internalGetCommentCountFlow(parentRef = parentId),
        ) { legacyCount, commentCount ->
            legacyCount + commentCount
        }
    }

    @Query("SELECT parentId FROM legacyReply WHERE eventId = :id")
    suspend fun internalGetLegacyReplyParentId(id: EventIdHex): EventIdHex?

    @Query("SELECT parentRef FROM comment WHERE eventId = :id")
    suspend fun internalGetCommentParentId(id: EventIdHex): EventIdHex?

    // Should be like LegacyReplyDao.getRepliesFlow
    @Query("SELECT COUNT(*) FROM LegacyReplyView WHERE parentId = :parentId AND authorIsMuted = 0")
    fun internalGetLegacyReplyCountFlow(parentId: EventIdHex): Flow<Int>

    // Should be like CommentDao.getCommentsFlow
    @Query("SELECT COUNT(*) FROM CommentView WHERE parentRef = :parentRef AND authorIsMuted = 0")
    fun internalGetCommentCountFlow(parentRef: String): Flow<Int>
}
