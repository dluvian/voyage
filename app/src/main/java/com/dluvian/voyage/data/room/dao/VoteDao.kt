package com.dluvian.voyage.data.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.dluvian.voyage.core.EventIdHex
import com.dluvian.voyage.data.room.entity.main.VoteEntity

@Dao
interface VoteDao {
    @Query("SELECT * FROM vote WHERE eventId = :postId AND pubkey = (SELECT pubkey FROM account LIMIT 1)")
    suspend fun getMyVote(postId: EventIdHex): VoteEntity?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertOrIgnoreVotes(voteEntities: Collection<VoteEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrReplaceVote(voteEntity: VoteEntity)

    @Query("SELECT MAX(createdAt) FROM vote WHERE eventId = :postId")
    suspend fun getNewestVoteCreatedAt(postId: EventIdHex): Long?
}
