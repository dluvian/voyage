package com.dluvian.voyage.data.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.dluvian.voyage.core.EventIdHex
import com.dluvian.voyage.core.PubkeyHex
import com.dluvian.voyage.data.room.entity.VoteEntity

@Dao
interface VoteUpsertDao {
    @Transaction
    suspend fun upsertVote(voteEntity: VoteEntity) {
        val newestCreatedAt = internalGetNewestCreatedAt(
            pubkey = voteEntity.pubkey,
            postId = voteEntity.postId
        ) ?: 0L
        if (voteEntity.createdAt < newestCreatedAt) return

        internalUpsertVote(voteEntity = voteEntity)
    }


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun internalUpsertVote(voteEntity: VoteEntity)

    @Query("SELECT MAX(createdAt) FROM vote WHERE pubkey = :pubkey AND postId = :postId")
    suspend fun internalGetNewestCreatedAt(pubkey: PubkeyHex, postId: EventIdHex): Long?
}