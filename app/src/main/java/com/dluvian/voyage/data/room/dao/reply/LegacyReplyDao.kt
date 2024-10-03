package com.dluvian.voyage.data.room.dao.reply

import androidx.room.Dao
import androidx.room.Query
import com.dluvian.voyage.core.EventIdHex
import com.dluvian.voyage.core.PubkeyHex
import com.dluvian.voyage.data.room.view.LegacyReplyView
import kotlinx.coroutines.flow.Flow


@Dao
interface LegacyReplyDao {

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

    @Query("SELECT * FROM LegacyReplyView WHERE id = :id")
    fun getReplyFlow(id: EventIdHex): Flow<LegacyReplyView?>

    @Query(PROFILE_REPLY_FEED_QUERY)
    fun getProfileReplyFlow(pubkey: PubkeyHex, until: Long, size: Int): Flow<List<LegacyReplyView>>

    @Query(PROFILE_REPLY_FEED_QUERY)
    suspend fun getProfileReplies(pubkey: PubkeyHex, until: Long, size: Int): List<LegacyReplyView>

    @Query(PROFILE_REPLY_FEED_EXISTS_QUERY)
    fun hasProfileRepliesFlow(pubkey: PubkeyHex): Flow<Boolean>

    @Query(PROFILE_REPLY_FEED_CREATED_AT_QUERY)
    suspend fun getProfileRepliesCreatedAt(pubkey: PubkeyHex, until: Long, size: Int): List<Long>
}
