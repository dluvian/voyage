package com.dluvian.voyage.data.room.dao.insert

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

        val unique = locks.distinctBy { it.pubkey }

        val lockedPubkeys = unique.map { it.pubkey }
        val alreadyLocked = internalFilterLockedPubkeys(pubkeys = lockedPubkeys)
        if (unique.size == alreadyLocked.size) return

        val filtered = unique.filter { !alreadyLocked.contains(it.pubkey) }
            .map { LockEntity.from(validatedLock = it) }

        internalDeleteContactList(lockedPubkeys = lockedPubkeys)
        insertLocks(*filtered.toTypedArray())
    }

    @Insert
    suspend fun insertLocks(vararg lock: LockEntity)

    @Query("SELECT pubkey FROM lock WHERE pubkey IN (:pubkeys)")
    suspend fun internalFilterLockedPubkeys(pubkeys: Collection<PubkeyHex>): List<PubkeyHex>

    @Query("DELETE FROM friend WHERE friendPubkey IN (:lockedPubkeys)")
    suspend fun internalDeleteContactList(lockedPubkeys: Collection<PubkeyHex>)
}
