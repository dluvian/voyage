package com.dluvian.voyage.data.room.dao.util

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import com.dluvian.voyage.core.EventIdHex

@Dao
interface DeleteDao {

    @Query("DELETE FROM vote WHERE id = :voteId")
    suspend fun deleteVote(voteId: EventIdHex)

    @Query("DELETE FROM mainEvent WHERE id = :id")
    suspend fun deleteMainEvent(id: EventIdHex)

    @Query("DELETE FROM mainEvent")
    suspend fun deleteAllPost()

    @Transaction
    suspend fun deleteList(identifier: String) {
        internalEmptyProfileList(identifier = identifier)
        internalEmptyTopicList(identifier = identifier)
        internalSoftDeleteProfileList(identifier = identifier)
        internalSoftDeleteTopicList(identifier = identifier)
    }

    // No tx bc we don't care if it's atomic
    suspend fun sweepDb(threshold: Int, oldestCreatedAtInUse: Long) {
        val createdAtWithOffset = internalOldestCreatedAt(threshold = threshold) ?: return
        val oldestCreatedAt = minOf(createdAtWithOffset, oldestCreatedAtInUse)

        // Delete cross posts first, bc they reference roots and replies
        internalDeleteOldestMainEvents(oldestCreatedAt = oldestCreatedAt)
    }

    @Query(
        "SELECT createdAt " +
                "FROM mainEvent " +
                "ORDER BY createdAt DESC " +
                "LIMIT 1 " +
                "OFFSET :threshold"
    )
    suspend fun internalOldestCreatedAt(threshold: Int): Long?

    @Query(
        """
            DELETE FROM mainEvent 
            WHERE createdAt < :oldestCreatedAt 
            AND pubkey NOT IN (SELECT pubkey FROM account) 
            AND id NOT IN (SELECT eventId FROM bookmark)
        """
    )
    suspend fun internalDeleteOldestMainEvents(oldestCreatedAt: Long)

    @Query("DELETE FROM profileSetItem WHERE identifier = :identifier")
    suspend fun internalEmptyProfileList(identifier: String)

    @Query("DELETE FROM topicSetItem WHERE identifier = :identifier")
    suspend fun internalEmptyTopicList(identifier: String)

    @Query("UPDATE profileSet SET deleted = 1 WHERE identifier = :identifier")
    suspend fun internalSoftDeleteProfileList(identifier: String)

    @Query("UPDATE topicSet SET deleted = 1 WHERE identifier = :identifier")
    suspend fun internalSoftDeleteTopicList(identifier: String)
}
