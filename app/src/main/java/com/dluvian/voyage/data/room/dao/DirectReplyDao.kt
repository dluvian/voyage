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
        "ORDER BY createdAt DESC " +
        "LIMIT :size"

private const val DIRECT_REPLY_EXISTS_QUERY = "SELECT EXISTS(SELECT * " +
        "FROM ReplyView " +
        "WHERE parentId IN (SELECT id FROM post WHERE pubkey IN (SELECT pubkey FROM account)) " +
        "AND authorIsOneself = 0)"

@Dao
interface DirectReplyDao {
    @Query(DIRECT_REPLY_FEED_QUERY)
    fun getDirectReplyFlow(until: Long, size: Int): Flow<List<ReplyView>>

    @Query(DIRECT_REPLY_FEED_QUERY)
    suspend fun getDirectReplies(until: Long, size: Int): List<ReplyView>

    @Query(DIRECT_REPLY_EXISTS_QUERY)
    fun hasDirectRepliesFlow(): Flow<Boolean>
}
