package com.dluvian.voyage.data.room.entity

import androidx.room.Entity
import androidx.room.Index
import com.dluvian.voyage.core.PubkeyHex

// Only one pubkey in table. CASCADE rules depend on it
@Entity(
    tableName = "account",
    primaryKeys = ["pubkey"],
    // ksp suggestion bc it's refrenced by topic.myPubkey and friend.myPubkey
    indices = [Index(value = ["pubkey"], unique = true)],
)
data class AccountEntity(
    val pubkey: PubkeyHex
)
