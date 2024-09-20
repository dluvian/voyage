package com.dluvian.voyage.data.room.dao

import androidx.room.Dao
import androidx.room.Query
import com.dluvian.voyage.core.EventIdHex
import com.dluvian.voyage.core.PubkeyHex
import com.dluvian.voyage.data.room.view.LegacyReplyView
import kotlinx.coroutines.flow.Flow

private const val PROFILE_REPLY_FEED_BASE_QUERY = "FROM LegacyReplyView " +
        "WHERE createdAt <= :until " +
        "AND pubkey = :pubkey " +
        "ORDER BY createdAt DESC " +
        "LIMIT :size"

private const val PROFILE_REPLY_FEED_QUERY = "SELECT * $PROFILE_REPLY_FEED_BASE_QUERY"
private const val PROFILE_REPLY_FEED_CREATED_AT_QUERY =
    "SELECT createdAt $PROFILE_REPLY_FEED_BASE_QUERY"


private const val PROFILE_REPLY_FEED_EXISTS_QUERY = "SELECT EXISTS(SELECT * " +
        "FROM LegacyReplyView " +
        "WHERE pubkey = :pubkey)"

@Dao
interface ReplyDao {
    @Query(
        "SELECT MAX(createdAt) " +
                "FROM mainEvent " +
                "WHERE id IN (SELECT eventId FROM legacyReply WHERE parentId = :parentId)"
    )
    suspend fun getNewestReplyCreatedAt(parentId: EventIdHex): Long?

    @Query(
        // getReplyCountFlow depends on this
        """
        SELECT * 
        FROM LegacyReplyView 
        WHERE parentId IN (:parentIds) 
        AND authorIsMuted = 0
        ORDER BY createdAt ASC
    """
    )
    fun getRepliesFlow(parentIds: Collection<EventIdHex>): Flow<List<LegacyReplyView>>

    @Query(
        // Should be like getRepliesFlow
        """
        SELECT COUNT(*) 
        FROM LegacyReplyView 
        WHERE parentId = :parentId
        AND authorIsMuted = 0
    """
    )
    fun getReplyCountFlow(parentId: EventIdHex): Flow<Int>

    @Query("SELECT * FROM LegacyReplyView WHERE id = :id")
    fun getReplyFlow(id: EventIdHex): Flow<LegacyReplyView?>

    @Query("SELECT parentId FROM legacyReply WHERE eventId = :id")
    suspend fun getParentId(id: EventIdHex): EventIdHex?

    @Query(PROFILE_REPLY_FEED_QUERY)
    fun getProfileReplyFlow(pubkey: PubkeyHex, until: Long, size: Int): Flow<List<LegacyReplyView>>

    @Query(PROFILE_REPLY_FEED_QUERY)
    suspend fun getProfileReplies(pubkey: PubkeyHex, until: Long, size: Int): List<LegacyReplyView>

    @Query(PROFILE_REPLY_FEED_EXISTS_QUERY)
    fun hasProfileRepliesFlow(pubkey: PubkeyHex): Flow<Boolean>

    @Query(PROFILE_REPLY_FEED_CREATED_AT_QUERY)
    suspend fun getProfileRepliesCreatedAt(pubkey: PubkeyHex, until: Long, size: Int): List<Long>
}
