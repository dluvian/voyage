package com.dluvian.voyage.data.room.dao

import androidx.room.Dao
import androidx.room.Query
import com.dluvian.voyage.core.PubkeyHex
import kotlinx.coroutines.flow.Flow

private const val EXCLUDE_LOCKED_PUBKEYS = "friendPubkey NOT IN (SELECT pubkey FROM lock) " +
        "AND webOfTrustPubkey NOT IN (SELECT pubkey FROM lock)"

@Dao
interface WebOfTrustDao {
    @Query(
        "SELECT DISTINCT webOfTrustPubkey " +
                "FROM weboftrust " +
                "WHERE $EXCLUDE_LOCKED_PUBKEYS"
    )
    fun getWebOfTrustFlow(): Flow<List<PubkeyHex>>

    @Query(
        "SELECT webOfTrustPubkey " +
                "FROM weboftrust " +
                "WHERE webOfTrustPubkey NOT IN (SELECT pubkey FROM profile) " +
                "AND $EXCLUDE_LOCKED_PUBKEYS"
    )
    suspend fun getWotWithMissingProfile(): List<PubkeyHex>

    @Query("SELECT MAX(createdAt) FROM weboftrust WHERE $EXCLUDE_LOCKED_PUBKEYS")
    suspend fun getNewestCreatedAt(): Long?

    @Query(
        "SELECT friendPubkey " +
                "FROM weboftrust " +
                "WHERE webOfTrustPubkey = :pubkey"
    )
    suspend fun getTrustedByPubkey(pubkey: PubkeyHex): PubkeyHex?
}
