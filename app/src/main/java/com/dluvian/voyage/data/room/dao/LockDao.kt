package com.dluvian.voyage.data.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import com.dluvian.voyage.core.PubkeyHex
import com.dluvian.voyage.data.event.ValidatedLock
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

    @Insert
    suspend fun insertLock(vararg lock: LockEntity)

    @Transaction
    suspend fun insertLocksTx(locks: Collection<ValidatedLock>) {
        if (locks.isEmpty()) return

        val alreadyLocked = internalFilterLockedPubkeys(pubkeys = locks.map { it.pubkey })
        if (locks.size == alreadyLocked.size) return

        val filtered = locks.filter { !alreadyLocked.contains(it.pubkey) }
            .map { LockEntity.from(validatedLock = it) }

        insertLock(*filtered.toTypedArray())
    }

    @Query("SELECT pubkey FROM lock WHERE pubkey IN (:pubkeys)")
    suspend fun internalFilterLockedPubkeys(pubkeys: Collection<PubkeyHex>): List<PubkeyHex>
}
