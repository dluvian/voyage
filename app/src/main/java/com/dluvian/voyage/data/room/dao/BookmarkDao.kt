package com.dluvian.voyage.data.room.dao

import androidx.room.Dao
import androidx.room.Query
import com.dluvian.voyage.core.EventIdHex
import com.dluvian.voyage.data.room.view.ReplyView
import com.dluvian.voyage.data.room.view.RootPostView
import kotlinx.coroutines.flow.Flow


private const val REPLY_FEED_QUERY = "SELECT * " +
        "FROM ReplyView " +
        "WHERE createdAt <= :until " +
        "AND id IN (SELECT postId FROM bookmark) " +
        "ORDER BY createdAt DESC " +
        "LIMIT :size"

private const val ROOT_POST_FEED_QUERY = "SELECT * " +
        "FROM RootPostView " +
        "WHERE createdAt <= :until " +
        "AND id IN (SELECT postId FROM bookmark) " +
        "ORDER BY createdAt DESC " +
        "LIMIT :size"

private const val BOOKMARKED_EVENTS_EXIST_QUERY = "SELECT EXISTS(SELECT * " +
        "FROM post " +
        "WHERE id IN (SELECT postId FROM bookmark))"

@Dao
interface BookmarkDao {
    @Query("SELECT MAX(createdAt) FROM bookmark")
    suspend fun getMaxCreatedAt(): Long?

    @Query("SELECT postId FROM bookmark WHERE postId NOT IN (SELECT id FROM post)")
    suspend fun getUnknownBookmarks(): List<EventIdHex>

    @Query(REPLY_FEED_QUERY)
    fun getReplyFlow(until: Long, size: Int): Flow<List<ReplyView>>

    @Query(REPLY_FEED_QUERY)
    suspend fun getReplies(until: Long, size: Int): List<ReplyView>

    @Query(ROOT_POST_FEED_QUERY)
    fun getRootPostsFlow(until: Long, size: Int): Flow<List<RootPostView>>

    @Query(ROOT_POST_FEED_QUERY)
    suspend fun getRootPosts(until: Long, size: Int): List<RootPostView>

    @Query(BOOKMARKED_EVENTS_EXIST_QUERY)
    fun hasBookmarkedPostsFlow(): Flow<Boolean>

    @Query(
        "SELECT createdAt " +
                "FROM post " +
                "WHERE createdAt <= :until " +
                "AND id IN (SELECT postId FROM bookmark) " +
                "LIMIT :size"
    )
    suspend fun getBookmarkedPostsCreatedAt(until: Long, size: Int): List<Long>
}
