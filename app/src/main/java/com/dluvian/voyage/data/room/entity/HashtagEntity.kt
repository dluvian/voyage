package com.dluvian.voyage.data.room.entity

import androidx.room.Entity
import com.dluvian.voyage.core.EventIdHex

@Entity(
    tableName = "hashtag",
    primaryKeys = ["eventId", "hashtag"],
)
data class HashtagEntity(
    val eventId: EventIdHex,
    val hashtag: String,
)
