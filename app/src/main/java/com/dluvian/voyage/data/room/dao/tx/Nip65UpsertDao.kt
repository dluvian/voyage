package com.dluvian.voyage.data.room.dao.tx

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.MapColumn
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.dluvian.voyage.core.PubkeyHex
import com.dluvian.voyage.data.event.ValidatedNip65
import com.dluvian.voyage.data.room.entity.Nip65Entity

@Dao
interface Nip65UpsertDao {
    @Transaction
    suspend fun upsertNip65s(validatedNip65s: Collection<ValidatedNip65>) {
        if (validatedNip65s.isEmpty()) return

        val newestCreatedAt = internalGetNewestCreatedAt(
            pubkeys = validatedNip65s.map { it.pubkey }
        )

        val toInsert = validatedNip65s
            .filter { it.createdAt > newestCreatedAt.getOrDefault(it.pubkey, 1L) }
        if (toInsert.isEmpty()) return

        internalUpsert(nip65Entities = toInsert.flatMap { Nip65Entity.from(validatedNip65 = it) })
        toInsert.forEach {
            internalDeleteOutdated(newestCreatedAt = it.createdAt, pubkey = it.pubkey)
        }
    }

    @Query(
        "SELECT MAX(createdAt) AS maxCreatedAt, pubkey " +
                "FROM nip65 " +
                "WHERE pubkey IN (:pubkeys)"
    )
    suspend fun internalGetNewestCreatedAt(pubkeys: Collection<PubkeyHex>):
            Map<@MapColumn("pubkey") PubkeyHex,
                    @MapColumn("maxCreatedAt") Long>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun internalUpsert(nip65Entities: Collection<Nip65Entity>)

    @Query("DELETE FROM nip65 WHERE createdAt < :newestCreatedAt AND pubkey = :pubkey")
    suspend fun internalDeleteOutdated(newestCreatedAt: Long, pubkey: PubkeyHex)
}
