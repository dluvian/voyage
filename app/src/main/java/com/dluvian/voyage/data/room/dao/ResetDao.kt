package com.dluvian.voyage.data.room.dao

import androidx.room.Dao
import androidx.room.Query

@Dao
interface ResetDao {
    @Query("DELETE FROM post WHERE pubkey IS NOT (SELECT pubkey FROM account LIMIT 1)")
    suspend fun resetAfterAccountChange()
}
