package com.dluvian.voyage.data.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.dluvian.voyage.core.PubkeyHex
import com.dluvian.voyage.data.room.entity.LockEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LockDao {
    @Query("SELECT json FROM lock WHERE pubkey = :pubkey")
    suspend fun getLockJson(pubkey: PubkeyHex): String?

    @Query("SELECT EXISTS(SELECT pubkey FROM lock WHERE pubkey = :pubkey)")
    suspend fun isLocked(pubkey: PubkeyHex): Boolean

    @Query("SELECT EXISTS(SELECT pubkey FROM lock WHERE pubkey IN (SELECT pubkey FROM account))")
    fun getMyLockFlow(): Flow<Boolean>

    @Query("SELECT pubkey FROM lock")
    fun getLockedPubkeysFlow(): Flow<List<PubkeyHex>>

    @Insert
    suspend fun insertLock(vararg lock: LockEntity)
}
