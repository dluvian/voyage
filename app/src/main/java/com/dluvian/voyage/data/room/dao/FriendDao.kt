package com.dluvian.voyage.data.room.dao

import androidx.room.Dao
import androidx.room.Query
import com.dluvian.voyage.core.PubkeyHex
import kotlinx.coroutines.flow.Flow

@Dao
interface FriendDao {
    @Query("SELECT DISTINCT friendPubkey FROM friend WHERE myPubkey = (SELECT pubkey FROM account LIMIT 1)")
    fun getFriendFlow(): Flow<List<PubkeyHex>>
}
