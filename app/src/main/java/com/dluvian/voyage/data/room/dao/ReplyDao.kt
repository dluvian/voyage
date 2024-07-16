package com.dluvian.voyage.data.room.dao

import androidx.room.Dao
import androidx.room.Query
import com.dluvian.voyage.core.EventIdHex
import com.dluvian.voyage.core.PubkeyHex
import com.dluvian.voyage.data.room.view.ReplyView
import kotlinx.coroutines.flow.Flow

private const val PROFILE_REPLY_FEED_BASE_QUERY = "FROM ReplyView " +
        "WHERE createdAt <= :until " +
        "AND pubkey = :pubkey " +
        "ORDER BY createdAt DESC " +
        "LIMIT :size"

private const val PROFILE_REPLY_FEED_QUERY = "SELECT * $PROFILE_REPLY_FEED_BASE_QUERY"
private const val PROFILE_REPLY_FEED_CREATED_AT_QUERY =
    "SELECT createdAt $PROFILE_REPLY_FEED_BASE_QUERY"


private const val PROFILE_REPLY_FEED_EXISTS_QUERY = "SELECT EXISTS(SELECT * " +
        "FROM ReplyView " +
        "WHERE pubkey = :pubkey)"

@Dao
interface ReplyDao {
    @Query("SELECT MAX(createdAt) FROM post WHERE parentId = :parentId")
    suspend fun getNewestReplyCreatedAt(parentId: EventIdHex): Long?

    @Query(
        """
        SELECT * 
        FROM ReplyView 
        WHERE parentId IN (:parentIds) 
        AND authorIsMuted = 0
        ORDER BY createdAt ASC
    """
    )
    fun getRepliesFlow(parentIds: Collection<EventIdHex>): Flow<List<ReplyView>>

    @Query("SELECT * FROM ReplyView WHERE id = :id")
    fun getReplyFlow(id: EventIdHex): Flow<ReplyView?>

    @Query("SELECT parentId FROM post WHERE id = :id")
    suspend fun getParentId(id: EventIdHex): EventIdHex?

    @Query(PROFILE_REPLY_FEED_QUERY)
    fun getProfileReplyFlow(pubkey: PubkeyHex, until: Long, size: Int): Flow<List<ReplyView>>

    @Query(PROFILE_REPLY_FEED_QUERY)
    suspend fun getProfileReplies(pubkey: PubkeyHex, until: Long, size: Int): List<ReplyView>

    @Query(PROFILE_REPLY_FEED_EXISTS_QUERY)
    fun hasProfileRepliesFlow(pubkey: PubkeyHex): Flow<Boolean>

    @Query(PROFILE_REPLY_FEED_CREATED_AT_QUERY)
    suspend fun getProfileRepliesCreatedAt(pubkey: PubkeyHex, until: Long, size: Int): List<Long>
}
