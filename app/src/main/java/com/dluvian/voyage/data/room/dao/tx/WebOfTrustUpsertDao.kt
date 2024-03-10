package com.dluvian.voyage.data.room.dao.tx

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.dluvian.voyage.core.PubkeyHex
import com.dluvian.voyage.data.model.ValidatedContactList
import com.dluvian.voyage.data.room.entity.WebOfTrustEntity

@Dao
interface WebOfTrustUpsertDao {
    @Transaction
    suspend fun upsertWebOfTrust(validatedWebOfTrust: ValidatedContactList) {
        val list = WebOfTrustEntity.from(validatedContactList = validatedWebOfTrust)
        val friendPubkey = validatedWebOfTrust.pubkey.toHex()

        val newestCreatedAt = internalGetNewestCreatedAt(friendPubkey = friendPubkey) ?: 0L
        if (validatedWebOfTrust.createdAt < newestCreatedAt) return

        if (list.isEmpty()) {
            internalDeleteList(friendPubkey = friendPubkey)
            return
        }

        internalUpsert(webOfTrustEntities = list)
        internalDeleteOutdated(newestCreatedAt = validatedWebOfTrust.createdAt)
    }

    @Query("SELECT MAX(createdAt) FROM weboftrust WHERE friendPubkey = :friendPubkey")
    suspend fun internalGetNewestCreatedAt(friendPubkey: PubkeyHex): Long?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun internalUpsert(webOfTrustEntities: Collection<WebOfTrustEntity>)

    @Query("DELETE FROM weboftrust WHERE friendPubkey = :friendPubkey")
    suspend fun internalDeleteList(friendPubkey: PubkeyHex)

    @Query("DELETE FROM weboftrust WHERE createdAt < :newestCreatedAt")
    suspend fun internalDeleteOutdated(newestCreatedAt: Long)
}
