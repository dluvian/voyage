package com.dluvian.voyage.data.room.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction

@Dao
interface DeleteDao {
    @Transaction
    suspend fun sweepPosts(threshold: Int, oldestCreatedAtInUse: Long) {
        internalDeleteOldestRootPosts(
            threshold = threshold,
            oldestCreatedAtInUse = oldestCreatedAtInUse
        )
        internalDeleteOrphanedPosts(oldestCreatedAtInUse = oldestCreatedAtInUse)
    }

    @Query(
        "DELETE FROM post " +
                "WHERE parentId IS NULL " +
                "AND createdAt < :oldestCreatedAtInUse " +
                "AND id NOT IN " +
                "(SELECT id FROM post WHERE parentId IS NULL " +
                "ORDER BY createdAt DESC LIMIT :threshold) " +
                "AND id NOT IN (SELECT id FROM post WHERE pubkey NOT IN (SELECT pubkey FROM account))"
    )
    suspend fun internalDeleteOldestRootPosts(threshold: Int, oldestCreatedAtInUse: Long)

    @Query(
        "DELETE FROM post " +
                "WHERE createdAt < :oldestCreatedAtInUse " +
                "AND parentId IS NOT NULL " +
                "AND pubkey NOT IN (SELECT pubkey FROM account)" +
                "AND parentId NOT IN (SELECT id FROM post) "
    )
    suspend fun internalDeleteOrphanedPosts(oldestCreatedAtInUse: Long)
}
