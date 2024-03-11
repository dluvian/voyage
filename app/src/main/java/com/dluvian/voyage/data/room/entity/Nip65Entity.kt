package com.dluvian.voyage.data.room.entity

import androidx.room.Embedded
import androidx.room.Entity
import com.dluvian.nostr_kt.Nip65Relay
import com.dluvian.voyage.core.PubkeyHex
import com.dluvian.voyage.data.model.ValidatedNip65

@Entity(tableName = "nip65", primaryKeys = ["pubkey", "url"])
data class Nip65Entity(
    val pubkey: PubkeyHex,
    @Embedded val nip65Relay: Nip65Relay,
    val createdAt: Long,
) {
    companion object {
        fun from(validatedNip65: ValidatedNip65): List<Nip65Entity> {
            val pubkey = validatedNip65.pubkey.toHex()
            return validatedNip65.relays.map { relay ->
                Nip65Entity(
                    pubkey = pubkey,
                    nip65Relay = relay,
                    createdAt = validatedNip65.createdAt
                )
            }
        }
    }
}
