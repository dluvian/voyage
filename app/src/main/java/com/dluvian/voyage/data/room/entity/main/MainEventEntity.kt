package com.dluvian.voyage.data.room.entity.main

import androidx.room.Entity
import androidx.room.Index
import com.dluvian.voyage.core.EventIdHex
import com.dluvian.voyage.core.PubkeyHex
import com.dluvian.voyage.data.nostr.RelayUrl

@Entity(
    tableName = "mainEvent",
    primaryKeys = ["id"],
    indices = [
        Index(value = ["pubkey"], unique = false),
        Index(value = ["createdAt"], unique = false),
        Index(value = ["isMentioningMe"], unique = false),
    ],
)
data class MainEventEntity(
    val id: EventIdHex,
    val pubkey: PubkeyHex,
    val createdAt: Long,
    val content: String,
    val relayUrl: RelayUrl,
    val isMentioningMe: Boolean,
    val json: String?,
)
