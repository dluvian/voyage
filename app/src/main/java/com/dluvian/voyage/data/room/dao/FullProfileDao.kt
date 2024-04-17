package com.dluvian.voyage.data.room.dao

import androidx.room.Dao
import androidx.room.Query
import com.dluvian.voyage.data.room.entity.FullProfileEntity


@Dao
interface FullProfileDao {
    @Query("SELECT * FROM fullProfile WHERE pubkey = (SELECT pubkey FROM account LIMIT 1)")
    suspend fun getFullProfile(): FullProfileEntity?
}
