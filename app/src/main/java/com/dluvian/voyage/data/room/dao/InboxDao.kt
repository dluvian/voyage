package com.dluvian.voyage.data.room.dao

import androidx.room.Dao
import androidx.room.Query
import com.dluvian.voyage.data.room.view.ReplyView
import kotlinx.coroutines.flow.Flow


private const val DIRECT_REPLY_FEED_QUERY = "SELECT * " +
        "FROM ReplyView " +
        "WHERE createdAt <= :until " +
        "AND parentId IN (SELECT id FROM post WHERE pubkey IN (SELECT pubkey FROM account))" +
        "AND authorIsOneself = 0 " +
        "AND authorIsMuted = 0 " +
        "ORDER BY createdAt DESC " +
        "LIMIT :size"

private const val INBOX_EXISTS_QUERY = "SELECT EXISTS($DIRECT_REPLY_FEED_QUERY)"

@Dao
interface InboxDao {
    @Query(DIRECT_REPLY_FEED_QUERY)
    fun getDirectReplyFlow(until: Long, size: Int): Flow<List<ReplyView>>

    @Query(DIRECT_REPLY_FEED_QUERY)
    suspend fun getDirectReplies(until: Long, size: Int): List<ReplyView>

    @Query(INBOX_EXISTS_QUERY)
    fun hasInboxFlow(until: Long = Long.MAX_VALUE, size: Int = 1): Flow<Boolean>
}
