package com.dluvian.voyage.data.room.entity.lists

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import com.dluvian.voyage.core.PubkeyHex
import com.dluvian.voyage.data.event.ValidatedTopicList
import com.dluvian.voyage.data.room.entity.AccountEntity


@Entity(
    tableName = "topic",
    primaryKeys = ["topic"],
    foreignKeys = [ForeignKey(
        entity = AccountEntity::class,
        parentColumns = ["pubkey"],
        childColumns = ["myPubkey"],
        onDelete = ForeignKey.CASCADE,
        onUpdate = ForeignKey.NO_ACTION
    )],
    indices = [Index(value = ["myPubkey"], unique = false)], // ksp suggestion: "Highly advised"
)
data class TopicEntity(
    val myPubkey: PubkeyHex,
    val topic: String,
    val createdAt: Long,
) {
    companion object {
        fun from(validatedTopicList: ValidatedTopicList): List<TopicEntity> {
            return validatedTopicList.topics.map { topic ->
                TopicEntity(
                    myPubkey = validatedTopicList.myPubkey,
                    topic = topic,
                    createdAt = validatedTopicList.createdAt
                )
            }
        }
    }
}
