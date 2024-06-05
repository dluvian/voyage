package com.dluvian.voyage.data.room.dao

import androidx.room.Dao
import androidx.room.Query
import com.dluvian.voyage.data.room.view.RootPostView
import kotlinx.coroutines.flow.Flow


private const val DIRECT_CROSS_POST_FEED_QUERY = "SELECT * " +
        "FROM RootPostView " +
        "WHERE createdAt <= :until " +
        "AND authorIsOneself = 0 " +
        "AND crossPostedAuthorIsOneself = 1 " +
        "ORDER BY createdAt DESC " +
        "LIMIT :size"

private const val DIRECT_CROSS_POSTS_EXISTS_QUERY = "SELECT EXISTS(SELECT * " +
        "FROM RootPostView " +
        "WHERE crossPostedAuthorIsOneself = 1 AND authorIsOneself = 0)"

@Dao
interface DirectCrossPostDao {
    @Query(DIRECT_CROSS_POST_FEED_QUERY)
    fun getDirectCrossPostFlow(until: Long, size: Int): Flow<List<RootPostView>>

    @Query(DIRECT_CROSS_POST_FEED_QUERY)
    suspend fun getDirectCrossPosts(until: Long, size: Int): List<RootPostView>

    @Query(DIRECT_CROSS_POSTS_EXISTS_QUERY)
    fun hasDirectCrossPostsFlow(): Flow<Boolean>
}
