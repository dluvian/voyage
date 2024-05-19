package com.dluvian.voyage.data.room.dao

import androidx.room.Dao
import androidx.room.Query
import com.dluvian.nostr_kt.RelayUrl
import com.dluvian.voyage.core.EventIdHex
import com.dluvian.voyage.core.PubkeyHex
import com.dluvian.voyage.data.room.view.EventRelayAuthorView
import kotlinx.coroutines.flow.Flow

@Dao
interface EventRelayDao {
    @Query("SELECT * FROM EventRelayAuthorView WHERE pubkey IN (:authors)")
    suspend fun getEventRelayAuthorView(authors: Collection<PubkeyHex>): List<EventRelayAuthorView>

    @Query("SELECT relayUrl FROM post WHERE id = :eventId")
    suspend fun getEventRelay(eventId: EventIdHex): RelayUrl?

    @Query("SELECT DISTINCT(relayUrl) FROM post")
    suspend fun getAllEventRelays(): List<RelayUrl>

    @Query("SELECT DISTINCT(relayUrl) FROM post WHERE pubkey = :pubkey")
    fun getEventRelays(pubkey: PubkeyHex): Flow<List<RelayUrl>>

    @Query("SELECT COUNT(*) FROM post WHERE relayUrl = :relayUrl")
    fun countByRelay(relayUrl: RelayUrl): Flow<Int>
}
