package com.dluvian.voyage.data.room.dao

import androidx.room.Dao
import androidx.room.Query
import com.dluvian.voyage.core.PubkeyHex
import kotlinx.coroutines.flow.Flow

@Dao
interface WebOfTrustDao {
    @Query(
        "SELECT DISTINCT webOfTrustPubkey FROM weboftrust " +
                "UNION SELECT DISTINCT friendPubkey FROM friend"
    )
    fun getWebOfTrustFlow(): Flow<List<PubkeyHex>>
}
