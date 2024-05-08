package com.dluvian.voyage.data.room.dao

import androidx.room.Dao
import androidx.room.Query
import com.dluvian.voyage.core.PubkeyHex
import kotlinx.coroutines.flow.Flow

@Dao
interface WebOfTrustDao {
    @Query("SELECT DISTINCT webOfTrustPubkey FROM weboftrust")
    fun getWebOfTrustFlow(): Flow<List<PubkeyHex>>

    @Query("SELECT webOfTrustPubkey FROM weboftrust WHERE webOfTrustPubkey NOT IN (SELECT pubkey FROM profile)")
    suspend fun getWotWithMissingProfile(): List<PubkeyHex>

    @Query("SELECT MAX(createdAt) FROM weboftrust")
    suspend fun getNewestCreatedAt(): Long?
}
