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
    suspend fun deleteList(identifier: String) {
        internalEmptyProfileList(identifier = identifier)
        internalEmptyTopicList(identifier = identifier)
        internalSoftDeleteProfileList(identifier = identifier)
        internalSoftDeleteTopicList(identifier = identifier)
    }

    @Transaction
    suspend fun sweepPosts(threshold: Int, oldestCreatedAtInUse: Long) {
        val createdAtWithOffset = internalOldestCreatedAt(threshold = threshold) ?: return
        val oldestCreatedAt = minOf(createdAtWithOffset, oldestCreatedAtInUse)

        internalDeleteOldestPosts(oldestCreatedAt = oldestCreatedAt)
        internalDeleteOldVotes(oldestCreatedAt = oldestCreatedAt)
    }

    @Query(
        "SELECT createdAt " +
                "FROM post " +
                "WHERE parentId IS NULL " +
                "ORDER BY createdAt DESC " +
                "LIMIT 1 " +
                "OFFSET :threshold"
    )
    suspend fun internalOldestCreatedAt(threshold: Int): Long?

    // I don't know why but it will not work without "crossPostedId IS NOT NULL"
    @Query(
        "DELETE FROM post " +
                "WHERE createdAt < :oldestCreatedAt " +
                "AND pubkey NOT IN (SELECT pubkey FROM account) " +
                "AND id NOT IN (SELECT postId FROM bookmark) " +
                "AND id NOT IN (SELECT crossPostedId FROM post WHERE createdAt >= :oldestCreatedAt AND crossPostedId IS NOT NULL) " +
                "AND id NOT IN (SELECT crossPostedId FROM post WHERE pubkey IN (SELECT pubkey FROM account) AND crossPostedId IS NOT NULL) "
    )
    suspend fun internalDeleteOldestPosts(oldestCreatedAt: Long)

    @Query(
        "DELETE FROM vote " +
                "WHERE pubkey NOT IN (SELECT pubkey FROM account) " +
                "AND postId IN (SELECT id FROM post WHERE createdAt < :oldestCreatedAt)"
    )
    suspend fun internalDeleteOldVotes(oldestCreatedAt: Long)

    @Query("DELETE FROM profileSetItem WHERE identifier = :identifier")
    suspend fun internalEmptyProfileList(identifier: String)

    @Query("DELETE FROM topicSetItem WHERE identifier = :identifier")
    suspend fun internalEmptyTopicList(identifier: String)

    @Query("UPDATE profileSet SET deleted = 1 WHERE identifier = :identifier")
    suspend fun internalSoftDeleteProfileList(identifier: String)

    @Query("UPDATE topicSet SET deleted = 1 WHERE identifier = :identifier")
    suspend fun internalSoftDeleteTopicList(identifier: String)
}
