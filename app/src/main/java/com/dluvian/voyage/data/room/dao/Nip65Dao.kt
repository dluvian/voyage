package com.dluvian.voyage.data.room.dao

import androidx.room.Dao
import androidx.room.Query
import com.dluvian.nostr_kt.RelayUrl
import com.dluvian.voyage.core.PubkeyHex
import com.dluvian.voyage.data.room.entity.Nip65Entity
import kotlinx.coroutines.flow.Flow

@Dao
interface Nip65Dao {
    @Query("SELECT * FROM nip65 WHERE pubkey = (SELECT pubkey FROM account LIMIT 1)")
    fun getMyNip65(): Flow<List<Nip65Entity>>

    @Query("SELECT DISTINCT url FROM nip65 WHERE pubkey = :pubkey AND isRead = 1")
    suspend fun getReadRelays(pubkey: PubkeyHex): List<RelayUrl>
}
