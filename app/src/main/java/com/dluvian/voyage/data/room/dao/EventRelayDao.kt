package com.dluvian.voyage.data.room.dao

import androidx.room.Dao
import androidx.room.Query
import com.dluvian.nostr_kt.RelayUrl
import com.dluvian.voyage.core.EventIdHex
import com.dluvian.voyage.core.PubkeyHex
import com.dluvian.voyage.data.room.view.EventRelayAuthorView

@Dao
interface EventRelayDao {
    @Query("SELECT * FROM EventRelayAuthorView WHERE pubkey IN (:authors)")
    suspend fun getEventRelayAuthorView(authors: Collection<PubkeyHex>): List<EventRelayAuthorView>

    @Query("SELECT relayUrl FROM eventRelay WHERE eventId = :eventId")
    suspend fun getEventRelays(eventId: EventIdHex): List<RelayUrl>
}
