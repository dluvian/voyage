package com.dluvian.voyage.data.room.dao.reply

import androidx.room.Dao
import androidx.room.Query
import com.dluvian.voyage.core.EventIdHex
import com.dluvian.voyage.core.PubkeyHex
import com.dluvian.voyage.data.room.view.CommentView
import kotlinx.coroutines.flow.Flow



@Dao
interface CommentDao {

    @Query(
        // getCommentCountFlow depends on this
        """
        SELECT * 
        FROM CommentView 
        WHERE parentId IN (:parentIds) 
        AND authorIsMuted = 0
        ORDER BY createdAt ASC
    """
    )
    fun getCommentsFlow(parentIds: Collection<EventIdHex>): Flow<List<CommentView>>

    @Query("SELECT * FROM CommentView WHERE id = :id")
    fun getCommentFlow(id: EventIdHex): Flow<CommentView?>

    @Query(PROFILE_COMMENT_FEED_QUERY)
    fun getProfileCommentFlow(pubkey: PubkeyHex, until: Long, size: Int): Flow<List<CommentView>>

    @Query(PROFILE_COMMENT_FEED_QUERY)
    suspend fun getProfileComments(pubkey: PubkeyHex, until: Long, size: Int): List<CommentView>

    @Query(PROFILE_COMMENT_FEED_EXISTS_QUERY)
    fun hasProfileCommentsFlow(pubkey: PubkeyHex): Flow<Boolean>

    @Query(PROFILE_COMMENT_FEED_CREATED_AT_QUERY)
    suspend fun getProfileCommentsCreatedAt(pubkey: PubkeyHex, until: Long, size: Int): List<Long>
}
