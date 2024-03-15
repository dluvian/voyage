package com.dluvian.voyage.data.room.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.dluvian.voyage.core.PubkeyHex

@Entity(tableName = "profile")
data class ProfileEntity(
    @PrimaryKey val pubkey: PubkeyHex,
    val name: String,
    val picture: String?,
    val lud16: String?,
    val createdAt: Long,
)
