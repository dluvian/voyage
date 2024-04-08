package com.dluvian.voyage.data.room.dao.tx

import android.util.Log
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.dluvian.voyage.core.PubkeyHex
import com.dluvian.voyage.data.room.entity.ProfileEntity


private const val TAG = "VoteUpsertDao"

@Dao
interface ProfileUpsertDao {
    @Transaction
    suspend fun upsertProfile(profile: ProfileEntity) {
        val newestCreatedAt = internalGetNewestCreatedAt(pubkey = profile.pubkey) ?: 0L
        if (profile.createdAt <= newestCreatedAt) return

        runCatching {
            internalUpsertProfile(profileEntity = profile)
        }.onFailure {
            Log.w(TAG, "Failed to upsert profile: ${it.message}")
        }
    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun internalUpsertProfile(profileEntity: ProfileEntity)

    @Query("SELECT MAX(createdAt) FROM profile WHERE pubkey = :pubkey")
    suspend fun internalGetNewestCreatedAt(pubkey: PubkeyHex): Long?
}
