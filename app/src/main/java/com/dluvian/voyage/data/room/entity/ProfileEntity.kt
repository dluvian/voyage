package com.dluvian.voyage.data.room.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.dluvian.voyage.core.MAX_NAME_LEN
import com.dluvian.voyage.core.PubkeyHex
import com.dluvian.voyage.data.event.ValidatedProfile

@Entity(
    tableName = "profile",
    indices = [
        Index(value = ["name"])
    ]
)
data class ProfileEntity(
    @PrimaryKey val pubkey: PubkeyHex,
    val name: String,
    val createdAt: Long,
) {
    companion object {
        fun from(validatedProfile: ValidatedProfile): ProfileEntity {
            return ProfileEntity(
                pubkey = validatedProfile.pubkey,
                name = validatedProfile.metadata.getName()?.take(MAX_NAME_LEN).orEmpty(),
                createdAt = validatedProfile.createdAt
            )
        }

        fun from(fullProfileEntity: FullProfileEntity): ProfileEntity {
            return ProfileEntity(
                pubkey = fullProfileEntity.pubkey,
                name = fullProfileEntity.name,
                createdAt = fullProfileEntity.createdAt
            )
        }
    }
}
