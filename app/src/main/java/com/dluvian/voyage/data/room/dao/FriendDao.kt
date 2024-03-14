package com.dluvian.voyage.data.room.dao

import androidx.room.Dao
import androidx.room.Query
import com.dluvian.voyage.core.PubkeyHex
import kotlinx.coroutines.flow.Flow

@Dao
interface FriendDao {
    @Query("SELECT friendPubkey FROM friend")
    fun getFriendsFlow(): Flow<List<PubkeyHex>>

    @Query("SELECT friendPubkey FROM friend WHERE friendPubkey NOT IN (SELECT friendPubkey FROM weboftrust)")
    suspend fun getFriendsWithMissingContactList(): List<PubkeyHex>
}
