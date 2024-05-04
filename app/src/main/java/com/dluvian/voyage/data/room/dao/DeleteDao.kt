package com.dluvian.voyage.data.room.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import com.dluvian.voyage.core.EventIdHex

@Dao
interface DeleteDao {

    @Query("DELETE FROM vote WHERE id = :voteId")
    suspend fun deleteVote(voteId: EventIdHex)

    @Query("DELETE FROM post WHERE id = :postId")
    suspend fun deletePost(postId: EventIdHex)

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
                "WHERE createdAt < :oldestCreatedAtInUse " +
                "AND parentId IS NULL " +
                "AND pubkey NOT IN (SELECT pubkey FROM account) " +
                "AND id NOT IN (SELECT id FROM post WHERE parentId IS NULL ORDER BY createdAt DESC LIMIT :threshold) " +
                "AND id NOT IN (SELECT crossPostedId FROM post WHERE parentId IS NULL AND crossPostedId IS NOT NULL ORDER BY createdAt DESC LIMIT :threshold) " +
                "AND id NOT IN (SELECT crossPostedId FROM post WHERE pubkey IN (SELECT pubkey FROM account) AND crossPostedId IS NOT NULL) "
    )
    suspend fun internalDeleteOldestRootPosts(threshold: Int, oldestCreatedAtInUse: Long)

    @Query(
        "DELETE FROM post " +
                "WHERE createdAt < :oldestCreatedAtInUse " +
                "AND parentId IS NOT NULL " +
                "AND pubkey NOT IN (SELECT pubkey FROM account)" +
                "AND parentId NOT IN (SELECT id FROM post) " +
                "AND id NOT IN (SELECT crossPostedId FROM post WHERE crossPostedId IS NOT NULL) "
    )
    suspend fun internalDeleteOrphanedPosts(oldestCreatedAtInUse: Long)
}
