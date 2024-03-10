package com.dluvian.voyage.data.room.dao

import androidx.room.Dao
import androidx.room.Query
import com.dluvian.voyage.core.PubkeyHex
import kotlinx.coroutines.flow.Flow

@Dao
interface FriendDao {
    @Query("SELECT DISTINCT friendPubkey FROM friend")
    fun getFriendsFlow(): Flow<List<PubkeyHex>>
}
