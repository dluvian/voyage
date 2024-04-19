package com.dluvian.voyage.data.room.dao.tx

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.dluvian.voyage.data.room.entity.FullProfileEntity


@Dao
interface FullProfileUpsertDao {
    @Transaction
    suspend fun upsertProfile(profile: FullProfileEntity) {
        val newestCreatedAt = internalGetNewestCreatedAt() ?: 0L
        if (profile.createdAt <= newestCreatedAt) return

        internalUpsertProfile(profile = profile)
    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun internalUpsertProfile(profile: FullProfileEntity)

    // Only one full profile in db
    @Query("SELECT MAX(createdAt) FROM fullProfile")
    suspend fun internalGetNewestCreatedAt(): Long?
}
