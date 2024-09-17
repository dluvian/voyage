package com.dluvian.voyage.data.room.dao.upsert

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
        val myPubkey = validatedContactList.pubkey
        val newestCreatedAt = internalGetNewestCreatedAt(myPubkey = myPubkey) ?: 1L
        if (validatedContactList.createdAt <= newestCreatedAt) return

        val list = FriendEntity.from(validatedContactList = validatedContactList)
        if (list.isEmpty()) {
            internalDeleteList(myPubkey = myPubkey)
            return
        }

        // RunCatching bc we might switch accounts
        runCatching {
            // REPLACE seems to cascade delete wot pubkeys, so we have to update manually
            internalUpdateCreatedAt(
                friendPubkeys = list.map { it.friendPubkey },
                newCreatedAt = validatedContactList.createdAt
            )
            internalInsertOrIgnore(friendEntities = list)
            internalDeleteOutdated(newestCreatedAt = validatedContactList.createdAt)
        }.onFailure {
            Log.w(TAG, "Failed to upsert friends: ${it.message}")
        }
    }

    @Query("SELECT MAX(createdAt) FROM friend WHERE myPubkey = :myPubkey")
    suspend fun internalGetNewestCreatedAt(myPubkey: PubkeyHex): Long?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun internalInsertOrIgnore(friendEntities: Collection<FriendEntity>)

    @Query("UPDATE friend SET createdAt = :newCreatedAt WHERE friendPubkey IN (:friendPubkeys)")
    suspend fun internalUpdateCreatedAt(friendPubkeys: Collection<PubkeyHex>, newCreatedAt: Long)

    @Query("DELETE FROM friend WHERE myPubkey = :myPubkey")
    suspend fun internalDeleteList(myPubkey: PubkeyHex)

    @Query("DELETE FROM friend WHERE createdAt < :newestCreatedAt")
    suspend fun internalDeleteOutdated(newestCreatedAt: Long)
}