package com.dluvian.voyage.data.room.dao

import androidx.room.Dao
import androidx.room.Query
import com.dluvian.voyage.data.nostr.Nip65Relay
import com.dluvian.voyage.data.nostr.RelayUrl
import com.dluvian.voyage.core.PubkeyHex
import com.dluvian.voyage.data.room.entity.Nip65Entity
import kotlinx.coroutines.flow.Flow

@Dao
interface Nip65Dao {
    @Query("SELECT * FROM nip65 WHERE pubkey = (SELECT pubkey FROM account LIMIT 1)")
    fun getMyNip65Flow(): Flow<List<Nip65Entity>>

    @Query(
        "SELECT url, isRead, isWrite " +
                "FROM nip65 " +
                "WHERE pubkey = :pubkey"
    )
    fun getNip65Flow(pubkey: PubkeyHex): Flow<List<Nip65Relay>>

    @Query("SELECT * FROM nip65 WHERE pubkey IN (:pubkeys) AND isRead = 1")
    suspend fun getReadRelays(pubkeys: Collection<PubkeyHex>): List<Nip65Entity>

    @Query("SELECT DISTINCT * FROM nip65 WHERE pubkey IN (:pubkeys) AND isWrite = 1")
    suspend fun getWriteRelays(pubkeys: Collection<PubkeyHex>): List<Nip65Entity>

    @Query(
        "SELECT DISTINCT * " +
                "FROM nip65 " +
                "WHERE pubkey " +
                "IN (SELECT pubkey FROM profilesetitem WHERE identifier = :identifier) " +
                "AND isWrite = 1"
    )
    suspend fun getWriteRelaysFromList(identifier: String): List<Nip65Entity>

    @Query("SELECT DISTINCT * FROM nip65 WHERE pubkey IN (SELECT friendPubkey FROM friend) AND isWrite = 1")
    suspend fun getFriendsWriteRelays(): List<Nip65Entity>

    @Query("SELECT MAX(createdAt) FROM nip65")
    suspend fun getNewestCreatedAt(): Long?

    @Query("SELECT MAX(createdAt) FROM nip65 WHERE pubkey = :pubkey")
    suspend fun getNewestCreatedAt(pubkey: PubkeyHex): Long?

    @Query(
        "SELECT url " +
                "FROM nip65 " +
                "WHERE pubkey IN (SELECT pubkey FROM friend) " +
                "GROUP BY url " +
                "ORDER BY COUNT(url) DESC " +
                "LIMIT :limit"
    )
    suspend fun getPopularRelays(limit: Int): List<RelayUrl>
}
