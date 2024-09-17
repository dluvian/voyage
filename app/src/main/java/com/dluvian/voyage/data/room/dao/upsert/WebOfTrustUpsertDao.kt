package com.dluvian.voyage.data.room.dao.upsert

import android.util.Log
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.MapColumn
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.dluvian.voyage.core.PubkeyHex
import com.dluvian.voyage.data.event.ValidatedContactList
import com.dluvian.voyage.data.room.entity.WebOfTrustEntity

private const val TAG = "WebOfTrustUpsertDao"

@Dao
interface WebOfTrustUpsertDao {

    @Transaction
    suspend fun upsertWebOfTrust(validatedWoTs: Collection<ValidatedContactList>) {
        if (validatedWoTs.isEmpty()) return

        val newestCreatedAt = internalGetNewestCreatedAt(
            friendPubkeys = validatedWoTs.map { it.pubkey }
        )

        val toInsert = validatedWoTs.filter { list ->
            list.createdAt > newestCreatedAt.getOrDefault(list.pubkey, 1L)
        }
        if (toInsert.isEmpty()) return

        val emptyLists = toInsert.filter { it.friendPubkeys.isEmpty() }
        if (emptyLists.isNotEmpty()) {
            internalDeleteLists(friendPubkeys = emptyLists.map { it.pubkey })
        }

        val rest = (toInsert - emptyLists.toSet())
        if (rest.isEmpty()) return

        val restEntities = rest.flatMap { WebOfTrustEntity.from(it) }

        // RunCatching bc we might change accounts or update friendList
        runCatching {
            internalUpsert(webOfTrustEntities = restEntities)
            rest.forEach {
                internalDeleteOutdated(
                    newestCreatedAt = it.createdAt,
                    friendPubkey = it.pubkey
                )
            }
        }.onFailure {
            Log.w(TAG, "Failed to upsert wot: ${it.message}")
        }
    }

    @Query(
        "SELECT MAX(createdAt) AS maxCreatedAt, friendPubkey " +
                "FROM weboftrust " +
                "WHERE friendPubkey IN (:friendPubkeys) " +
                "GROUP BY friendPubkey"
    )
    suspend fun internalGetNewestCreatedAt(friendPubkeys: Collection<PubkeyHex>):
            Map<@MapColumn("friendPubkey") PubkeyHex,
                    @MapColumn("maxCreatedAt") Long>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun internalUpsert(webOfTrustEntities: Collection<WebOfTrustEntity>)

    @Query("DELETE FROM weboftrust WHERE friendPubkey IN (:friendPubkeys)")
    suspend fun internalDeleteLists(friendPubkeys: Collection<PubkeyHex>)

    @Query(
        "DELETE FROM weboftrust " +
                "WHERE createdAt < :newestCreatedAt " +
                "AND friendPubkey = :friendPubkey"
    )
    suspend fun internalDeleteOutdated(newestCreatedAt: Long, friendPubkey: PubkeyHex)
}