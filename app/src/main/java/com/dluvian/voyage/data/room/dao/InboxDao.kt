package com.dluvian.voyage.data.room.dao

import androidx.room.Dao
import androidx.room.Query
import com.dluvian.voyage.data.room.view.ReplyView
import com.dluvian.voyage.data.room.view.RootPostView
import kotlinx.coroutines.flow.Flow


private const val DIRECT_CROSS_POST_FEED_QUERY = "SELECT * " +
        "FROM RootPostView " +
        "WHERE createdAt <= :until " +
        "AND authorIsOneself = 0 " +
        "AND crossPostedAuthorIsOneself = 1 " +
        "ORDER BY createdAt DESC " +
        "LIMIT :size"

private const val DIRECT_REPLY_FEED_QUERY = "SELECT * " +
        "FROM ReplyView " +
        "WHERE createdAt <= :until " +
        "AND parentId IN (SELECT id FROM post WHERE pubkey IN (SELECT pubkey FROM account))" +
        "AND authorIsOneself = 0 " +
        "ORDER BY createdAt DESC " +
        "LIMIT :size"

private const val INBOX_EXISTS_QUERY = """
    SELECT EXISTS(
        SELECT * 
        FROM post 
        WHERE pubkey NOT IN (SELECT pubkey FROM account)
        AND (
            crossPostedPubkey = (SELECT pubkey FROM account LIMIT 1)
            OR 
            parentId IN (SELECT id FROM post WHERE pubkey IN (SELECT pubkey FROM account)) 
        )
    )
"""

@Dao
interface InboxDao {
    @Query(DIRECT_CROSS_POST_FEED_QUERY)
    fun getDirectCrossPostFlow(until: Long, size: Int): Flow<List<RootPostView>>

    @Query(DIRECT_CROSS_POST_FEED_QUERY)
    suspend fun getDirectCrossPosts(until: Long, size: Int): List<RootPostView>

    @Query(DIRECT_REPLY_FEED_QUERY)
    fun getDirectReplyFlow(until: Long, size: Int): Flow<List<ReplyView>>

    @Query(DIRECT_REPLY_FEED_QUERY)
    suspend fun getDirectReplies(until: Long, size: Int): List<ReplyView>

    @Query(INBOX_EXISTS_QUERY)
    fun hasInboxFlow(): Flow<Boolean>
}
