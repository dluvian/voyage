package com.dluvian.voyage.data.room.dao.tx

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import com.dluvian.voyage.core.PubkeyHex
import com.dluvian.voyage.data.event.ValidatedLock
import com.dluvian.voyage.data.room.entity.LockEntity

@Dao
interface LockInsertDao {
    @Transaction
    suspend fun insertLocksTx(locks: Collection<ValidatedLock>) {
        if (locks.isEmpty()) return

        val lockedPubkeys = locks.map { it.pubkey }
        val alreadyLocked = internalFilterLockedPubkeys(pubkeys = lockedPubkeys)
        if (locks.size == alreadyLocked.size) return

        val filtered = locks.filter { !alreadyLocked.contains(it.pubkey) }
            .map { LockEntity.from(validatedLock = it) }

        internalDeleteContactList(lockedPubkeys = lockedPubkeys)
        internalInsertLocks(*filtered.toTypedArray())
    }

    @Insert
    suspend fun internalInsertLocks(vararg lock: LockEntity)

    @Query("SELECT pubkey FROM lock WHERE pubkey IN (:pubkeys)")
    suspend fun internalFilterLockedPubkeys(pubkeys: Collection<PubkeyHex>): List<PubkeyHex>

    @Query("DELETE FROM friend WHERE friendPubkey IN (:lockedPubkeys)")
    suspend fun internalDeleteContactList(lockedPubkeys: Collection<PubkeyHex>)
}
