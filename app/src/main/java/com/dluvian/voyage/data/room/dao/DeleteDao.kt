package com.dluvian.voyage.data.room.dao

import androidx.room.Dao
import androidx.room.Query

@Dao
interface DeleteDao {
    @Query(
        "DELETE FROM post " +
                "WHERE parentId IS NULL " +
                "AND createdAt < :oldestCreatedAtInUse " +
                "AND id NOT IN " +
                "(SELECT id FROM post WHERE parentId IS NULL " +
                "ORDER BY createdAt DESC LIMIT :threshold) "
    )
    suspend fun deleteOldestRootPosts(threshold: Int, oldestCreatedAtInUse: Long)
}
