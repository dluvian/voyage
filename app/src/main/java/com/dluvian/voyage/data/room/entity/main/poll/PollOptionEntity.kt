package com.dluvian.voyage.data.room.entity.main.poll

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import com.dluvian.voyage.core.EventIdHex

@Entity(
    tableName = "pollOption",
    primaryKeys = ["pollId", "optionId"],
    foreignKeys = [ForeignKey(
        entity = PollEntity::class,
        parentColumns = ["eventId"],
        childColumns = ["pollId"],
        onDelete = ForeignKey.CASCADE,
        onUpdate = ForeignKey.NO_ACTION
    )],
    indices = [
        Index(value = ["pollId"], unique = true), // pollId is apparently not a primary key
    ],
)
data class PollOptionEntity(
    val pollId: EventIdHex,
    val optionId: String,
    val label: String,
)
