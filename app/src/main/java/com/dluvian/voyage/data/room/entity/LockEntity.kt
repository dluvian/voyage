package com.dluvian.voyage.data.room.entity

import androidx.room.Entity
import com.dluvian.voyage.core.PubkeyHex
import com.dluvian.voyage.data.event.ValidatedLock

@Entity(
    tableName = "lock",
    primaryKeys = ["pubkey"],
)
data class LockEntity(
    val pubkey: PubkeyHex,
    val json: String,
) {
    companion object {
        fun from(validatedLock: ValidatedLock): LockEntity {
            return LockEntity(pubkey = validatedLock.pubkey, json = validatedLock.json)
        }
    }
}
