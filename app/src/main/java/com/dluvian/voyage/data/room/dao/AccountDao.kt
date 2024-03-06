package com.dluvian.voyage.data.room.dao

import androidx.room.Dao
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AccountDao {
    @Query("SELECT pubkey FROM account LIMIT 1")
    fun getPubkeyFlow(): Flow<String?>
}
