package com.dluvian.voyage.data.room.dao

import androidx.room.Dao
import androidx.room.Query
import com.dluvian.voyage.data.room.view.ReplyView
import com.dluvian.voyage.data.room.view.RootPostView
import kotlinx.coroutines.flow.Flow

private const val INBOX_CONDITION = "WHERE createdAt <= :until " +
        "AND isMentioningMe = 1 " +
        "AND authorIsOneself = 0 " +
        "AND authorIsMuted = 0 "

private const val INBOX_ORDER = "ORDER BY createdAt DESC LIMIT :size "

private const val MENTION_REPLY_FEED_QUERY = "SELECT * " +
        "FROM ReplyView " +
        INBOX_CONDITION +
        INBOX_ORDER

private const val MENTION_ROOT_FEED_QUERY = "SELECT * " +
        "FROM RootPostView " +
        INBOX_CONDITION +
        INBOX_ORDER

private const val MENTION_REPLY_ID_QUERY = "SELECT id " +
        "FROM ReplyView " +
        INBOX_CONDITION

private const val MENTION_ROOT_ID_QUERY = "SELECT id " +
        "FROM RootPostView " +
        INBOX_CONDITION

private const val INBOX_EXISTS_QUERY =
    "SELECT EXISTS($MENTION_ROOT_ID_QUERY UNION $MENTION_REPLY_ID_QUERY)"

@Dao
interface InboxDao {
    @Query(MENTION_REPLY_FEED_QUERY)
    fun getMentionReplyFlow(until: Long, size: Int): Flow<List<ReplyView>>

    @Query(MENTION_REPLY_FEED_QUERY)
    suspend fun getMentionReplies(until: Long, size: Int): List<ReplyView>

    @Query(MENTION_ROOT_FEED_QUERY)
    fun getMentionRootFlow(until: Long, size: Int): Flow<List<RootPostView>>

    @Query(MENTION_ROOT_FEED_QUERY)
    suspend fun getMentionRoots(until: Long, size: Int): List<RootPostView>

    @Query(INBOX_EXISTS_QUERY)
    fun hasInboxFlow(until: Long = Long.MAX_VALUE): Flow<Boolean>
}
