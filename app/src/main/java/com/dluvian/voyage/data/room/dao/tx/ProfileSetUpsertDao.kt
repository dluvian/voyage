package com.dluvian.voyage.data.room.dao.tx

import android.util.Log
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.dluvian.voyage.core.PubkeyHex
import com.dluvian.voyage.data.event.ValidatedProfileSet
import com.dluvian.voyage.data.room.entity.sets.ProfileSetEntity
import com.dluvian.voyage.data.room.entity.sets.ProfileSetItemEntity

private const val TAG = "ProfileSetUpsertDao"

@Dao
interface ProfileSetUpsertDao {
    @Transaction
    suspend fun upsertSet(set: ValidatedProfileSet) {
        val newestCreatedAt = internalGetNewestCreatedAt(identifier = set.identifier) ?: 1L
        if (set.createdAt <= newestCreatedAt) return

        val setEntity = ProfileSetEntity.from(set = set)
        // RunCatching bc we might change account
        val result = runCatching { internalUpsert(set = setEntity) }
        if (result.isFailure) {
            Log.w(TAG, "Failed to upsert profile set: ${result.exceptionOrNull()?.message}")
            return
        }

        val inDb = internalGetPubkeys(identifier = set.identifier).toSet()
        val toInsert = set.pubkeys.minus(inDb).map { pubkey ->
            ProfileSetItemEntity(identifier = set.identifier, pubkey = pubkey)
        }
        if (toInsert.isNotEmpty()) internalInsert(entries = toInsert)

        val toDelete = inDb.minus(set.pubkeys).map { pubkey ->
            ProfileSetItemEntity(identifier = set.identifier, pubkey = pubkey)
        }
        if (toDelete.isNotEmpty()) internalDelete(entries = toDelete)
    }


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun internalUpsert(set: ProfileSetEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun internalInsert(entries: Collection<ProfileSetItemEntity>)

    @Delete
    suspend fun internalDelete(entries: Collection<ProfileSetItemEntity>)

    @Query("SELECT MAX(createdAt) FROM profileSet WHERE identifier = :identifier")
    suspend fun internalGetNewestCreatedAt(identifier: String): Long?

    @Query("SELECT pubkey FROM profileSetItem WHERE identifier = :identifier")
    suspend fun internalGetPubkeys(identifier: String): List<PubkeyHex>
}
