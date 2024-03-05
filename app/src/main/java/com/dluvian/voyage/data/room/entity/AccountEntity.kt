package com.dluvian.voyage.data.room.entity

import androidx.room.Entity
import com.dluvian.voyage.core.PubkeyHex

// Only one pubkey in table. CASCADE rules depend on it
@Entity(
    tableName = "account",
    primaryKeys = ["pubkey"],
)
data class AccountEntity(
    val pubkey: PubkeyHex
)
