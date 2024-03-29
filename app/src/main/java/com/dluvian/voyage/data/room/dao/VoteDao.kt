package com.dluvian.voyage.data.room.dao

import androidx.room.Dao
import androidx.room.Query
import com.dluvian.voyage.core.EventIdHex
import com.dluvian.voyage.data.room.entity.VoteEntity

@Dao
interface VoteDao {
    @Query("SELECT * FROM vote WHERE postId = :postId AND pubkey = (SELECT pubkey FROM account LIMIT 1)")
    suspend fun getMyVote(postId: EventIdHex): VoteEntity?

    @Query(
        "DELETE FROM vote " +
                "WHERE postId = :postId " +
                "AND pubkey = (SELECT pubkey FROM account LIMIT 1)"
    )
    suspend fun deleteMyVote(postId: EventIdHex)
}
