package com.dluvian.voyage.data.room.dao.upsert

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.MapColumn
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.dluvian.voyage.core.PubkeyHex
import com.dluvian.voyage.data.room.entity.ProfileEntity


@Dao
interface ProfileUpsertDao {
    @Transaction
    suspend fun upsertProfiles(profiles: Collection<ProfileEntity>) {
        if (profiles.isEmpty()) return

        val newestCreatedAt = internalGetNewestCreatedAt(pubkeys = profiles.map { it.pubkey })
        val toInsert = profiles.filter {
            it.createdAt > newestCreatedAt.getOrDefault(it.pubkey, 1L)
        }

        if (toInsert.isEmpty()) return

        internalUpsertProfiles(profileEntities = toInsert)
    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun internalUpsertProfiles(profileEntities: Collection<ProfileEntity>)

    @Query(
        "SELECT MAX(createdAt) AS maxCreatedAt, pubkey " +
                "FROM profile " +
                "WHERE pubkey IN (:pubkeys)"
    )
    suspend fun internalGetNewestCreatedAt(pubkeys: Collection<PubkeyHex>):
            Map<@MapColumn("pubkey") PubkeyHex,
                    @MapColumn("maxCreatedAt") Long>
}
