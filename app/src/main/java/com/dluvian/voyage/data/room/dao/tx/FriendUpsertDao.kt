package com.dluvian.voyage.data.room.dao.tx

import android.util.Log
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.dluvian.voyage.core.PubkeyHex
import com.dluvian.voyage.data.event.ValidatedContactList
import com.dluvian.voyage.data.room.entity.FriendEntity


private const val TAG = "FriendUpsertDao"

@Dao
interface FriendUpsertDao {
    @Transaction
    suspend fun upsertFriends(validatedContactList: ValidatedContactList) {
        val list = FriendEntity.from(validatedContactList = validatedContactList)
        val myPubkey = validatedContactList.pubkey.toHex()

        val newestCreatedAt = internalGetNewestCreatedAt(myPubkey = myPubkey) ?: 0L
        if (validatedContactList.createdAt < newestCreatedAt) return

        if (list.isEmpty()) {
            internalDeleteList(myPubkey = myPubkey)
            return
        }

        runCatching {
            internalUpsert(friendEntities = list)
            internalDeleteOutdated(newestCreatedAt = validatedContactList.createdAt)
        }.onFailure {
            Log.w(TAG, "Failed to upsert friends: ${it.message}")
        }
    }

    @Query("SELECT MAX(createdAt) FROM friend WHERE myPubkey = :myPubkey")
    suspend fun internalGetNewestCreatedAt(myPubkey: PubkeyHex): Long?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun internalUpsert(friendEntities: Collection<FriendEntity>)

    @Query("DELETE FROM friend WHERE myPubkey = :myPubkey")
    suspend fun internalDeleteList(myPubkey: PubkeyHex)

    @Query("DELETE FROM friend WHERE createdAt < :newestCreatedAt")
    suspend fun internalDeleteOutdated(newestCreatedAt: Long)
}
