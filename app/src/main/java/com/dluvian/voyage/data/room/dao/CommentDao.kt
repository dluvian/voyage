package com.dluvian.voyage.data.room.dao

import androidx.room.Dao
import androidx.room.Query
import com.dluvian.voyage.core.EventIdHex
import com.dluvian.voyage.core.PubkeyHex
import com.dluvian.voyage.data.room.view.CommentView
import kotlinx.coroutines.flow.Flow

// TODO: Combine with LegacyReplyDao in SomeReplyDao

private const val PROFILE_COMMENT_FEED_BASE_QUERY = "FROM CommentView " +
        "WHERE createdAt <= :until " +
        "AND pubkey = :pubkey " +
        "ORDER BY createdAt DESC " +
        "LIMIT :size"

private const val PROFILE_COMMENT_FEED_QUERY = "SELECT * $PROFILE_COMMENT_FEED_BASE_QUERY"
private const val PROFILE_COMMENT_FEED_CREATED_AT_QUERY =
    "SELECT createdAt $PROFILE_COMMENT_FEED_BASE_QUERY"


private const val PROFILE_COMMENT_FEED_EXISTS_QUERY = "SELECT EXISTS(SELECT * " +
        "FROM CommentView " +
        "WHERE pubkey = :pubkey)"

@Dao
interface CommentDao {
    @Query(
        "SELECT MAX(createdAt) " +
                "FROM mainEvent " +
                "WHERE id IN (SELECT eventId FROM comment WHERE parentRef = :parentRef)"
    )
    suspend fun getNewestCommentCreatedAt(parentRef: EventIdHex): Long?

    @Query(
        // getCommentCountFlow depends on this
        """
        SELECT * 
        FROM CommentView 
        WHERE parentRef IN (:parentRefs) 
        AND authorIsMuted = 0
        ORDER BY createdAt ASC
    """
    )
    fun getCommentsFlow(parentRefs: Collection<EventIdHex>): Flow<List<CommentView>>

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
