package com.dluvian.voyage.data.room.dao.reply

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

    fun getReplyCountFlow(parentRef: String): Flow<Int> {
        return combine(
            internalGetLegacyReplyCountFlow(parentId = parentRef),
            internalGetCommentCountFlow(parentRef = parentRef),
        ) { legacyCount, commentCount ->
            legacyCount + commentCount
        }
    }

    suspend fun getNewestReplyCreatedAt(parentRef: String): Long? {
        val legacy = internalGetNewestLegacyReplyCreatedAt(parentId = parentRef)
        val comment = internalGetNewestCommentCreatedAt(parentRef = parentRef)

        return if (legacy == null && comment == null) null
        else maxOf(legacy ?: 0, comment ?: 0)
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

    @Query(
        "SELECT MAX(createdAt) " +
                "FROM mainEvent " +
                "WHERE id IN (SELECT eventId FROM legacyReply WHERE parentId = :parentId)"
    )
    suspend fun internalGetNewestLegacyReplyCreatedAt(parentId: EventIdHex): Long?

    @Query(
        "SELECT MAX(createdAt) " +
                "FROM mainEvent " +
                "WHERE id IN (SELECT eventId FROM comment WHERE parentRef = :parentRef)"
    )
    suspend fun internalGetNewestCommentCreatedAt(parentRef: String): Long?
}
