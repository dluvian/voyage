package com.dluvian.voyage.data.room.dao.tx

import android.util.Log
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.dluvian.voyage.data.event.ValidatedMuteList
import com.dluvian.voyage.data.room.entity.MuteEntity

private const val TAG = "MuteUpsertDao"

@Dao
interface MuteUpsertDao {
    @Transaction
    suspend fun upsertMuteList(muteList: ValidatedMuteList) {
        val newestCreatedAt = internalGetNewestCreatedAt() ?: 1L
        if (muteList.createdAt <= newestCreatedAt) return

        val list = MuteEntity.from(muteList = muteList)
        if (list.isEmpty()) {
            internalDeleteList()
            return
        }

        // RunCatching bc we might change account
        runCatching {
            internalUpsert(entities = list)
            internalDeleteOutdated(newestCreatedAt = muteList.createdAt)
        }.onFailure {
            Log.w(TAG, "Failed to upsert mute list: ${it.message}")
        }
    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun internalUpsert(entities: Collection<MuteEntity>)

    @Query("SELECT MAX(createdAt) FROM mute")
    suspend fun internalGetNewestCreatedAt(): Long?

    @Query("DELETE FROM mute")
    suspend fun internalDeleteList()

    @Query("DELETE FROM mute WHERE createdAt < :newestCreatedAt")
    suspend fun internalDeleteOutdated(newestCreatedAt: Long)
}
