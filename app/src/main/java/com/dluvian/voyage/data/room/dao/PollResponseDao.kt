package com.dluvian.voyage.data.room.dao

import androidx.room.Insert
import androidx.room.OnConflictStrategy
import com.dluvian.voyage.data.room.entity.main.poll.PollResponseEntity

interface PollResponseDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertOrIgnoreResponses(responses: Collection<PollResponseEntity>)
}
