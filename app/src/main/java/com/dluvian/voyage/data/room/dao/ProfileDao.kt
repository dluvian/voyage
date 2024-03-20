package com.dluvian.voyage.data.room.dao

import androidx.room.Dao
import androidx.room.Query
import com.dluvian.voyage.core.PubkeyHex
import com.dluvian.voyage.data.room.view.AdvancedProfileView
import kotlinx.coroutines.flow.Flow

@Dao
interface ProfileDao {
    @Query("SELECT * FROM AdvancedProfileView WHERE pubkey = :pubkey")
    fun getAdvancedProfileFlow(pubkey: PubkeyHex): Flow<AdvancedProfileView?>
}
