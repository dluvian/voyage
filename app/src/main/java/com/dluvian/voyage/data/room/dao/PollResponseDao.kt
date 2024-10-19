package com.dluvian.voyage.data.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.dluvian.voyage.core.EventIdHex
import com.dluvian.voyage.data.room.entity.main.poll.PollResponseEntity

@Dao
interface PollResponseDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertOrIgnoreResponses(responses: Collection<PollResponseEntity>)

    @Query("SELECT MAX(createdAt) FROM pollResponse WHERE pollId = :pollId")
    suspend fun getLatestResponseTime(pollId: EventIdHex): Long?
}
