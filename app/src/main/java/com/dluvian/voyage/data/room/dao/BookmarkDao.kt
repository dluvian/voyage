package com.dluvian.voyage.data.room.dao

import androidx.room.Dao
import androidx.room.Query
import com.dluvian.voyage.core.EventIdHex
import com.dluvian.voyage.data.room.view.CommentView
import com.dluvian.voyage.data.room.view.LegacyReplyView
import com.dluvian.voyage.data.room.view.PollView
import com.dluvian.voyage.data.room.view.RootPostView
import kotlinx.coroutines.flow.Flow

private const val COND = "WHERE createdAt <= :until " +
        "AND id IN (SELECT eventId FROM bookmark) " +
        "ORDER BY createdAt DESC " +
        "LIMIT :size"

// No crossposts in bookmark feed
private const val AND_NO_CROSS = "AND id NOT IN (SELECT eventId FROM crossPost) "

private const val ROOT_QUERY = "SELECT * FROM RootPostView $COND"
private const val REPLY_QUERY = "SELECT * FROM LegacyReplyView $COND"
private const val COMMENT_QUERY = "SELECT * FROM CommentView $COND"
private const val POLL_QUERY = "SELECT * FROM PollView $COND"

private const val BOOKMARKED_EVENTS_EXIST_QUERY = "SELECT EXISTS(" +
        "SELECT * " +
        "FROM mainEvent " +
        "WHERE id IN (SELECT eventId FROM bookmark) " +
        AND_NO_CROSS +
        ")"

@Dao
interface BookmarkDao {
    @Query("SELECT MAX(createdAt) FROM bookmark")
    suspend fun getMaxCreatedAt(): Long?

    @Query("SELECT eventId FROM bookmark")
    suspend fun getMyBookmarks(): List<EventIdHex>

    @Query("SELECT eventId FROM bookmark WHERE eventId NOT IN (SELECT id FROM mainEvent)")
    suspend fun getUnknownBookmarks(): List<EventIdHex>

    @Query(ROOT_QUERY)
    fun getRootPostsFlow(until: Long, size: Int): Flow<List<RootPostView>>

    @Query(ROOT_QUERY)
    suspend fun getRootPosts(until: Long, size: Int): List<RootPostView>

    @Query(REPLY_QUERY)
    fun getReplyFlow(until: Long, size: Int): Flow<List<LegacyReplyView>>

    @Query(REPLY_QUERY)
    suspend fun getReplies(until: Long, size: Int): List<LegacyReplyView>

    @Query(COMMENT_QUERY)
    fun getCommentFlow(until: Long, size: Int): Flow<List<CommentView>>

    @Query(COMMENT_QUERY)
    suspend fun getComments(until: Long, size: Int): List<CommentView>

    @Query(POLL_QUERY)
    fun getPollFlow(until: Long, size: Int): Flow<List<PollView>>

    @Query(POLL_QUERY)
    suspend fun getPolls(until: Long, size: Int): List<PollView>

    @Query(BOOKMARKED_EVENTS_EXIST_QUERY)
    fun hasBookmarkedPostsFlow(): Flow<Boolean>

    @Query(
        "SELECT createdAt " +
                "FROM mainEvent " +
                "WHERE createdAt <= :until " +
                "AND id IN (SELECT eventId FROM bookmark) " +
                AND_NO_CROSS +
                "ORDER BY createdAt DESC " +
                "LIMIT :size"
    )
    suspend fun getBookmarkedPostsCreatedAt(until: Long, size: Int): List<Long>
}
