package com.dluvian.voyage.data.room.dao

import androidx.room.Dao
import androidx.room.Query
import com.dluvian.voyage.core.EventIdHex
import com.dluvian.voyage.core.PubkeyHex
import com.dluvian.voyage.data.nostr.RelayUrl
import com.dluvian.voyage.data.room.view.EventRelayAuthorView
import kotlinx.coroutines.flow.Flow

@Dao
interface EventRelayDao {
    @Query("SELECT * FROM EventRelayAuthorView WHERE pubkey IN (:authors)")
    suspend fun getEventRelayAuthorView(authors: Collection<PubkeyHex>): List<EventRelayAuthorView>

    @Query(
        "SELECT * FROM EventRelayAuthorView " +
                "WHERE pubkey IN (SELECT friendPubkey FROM friend) "
    )
    suspend fun getFriendsEventRelayAuthorView(): List<EventRelayAuthorView>

    @Query(
        "SELECT * " +
                "FROM EventRelayAuthorView " +
                "WHERE pubkey " +
                "IN (SELECT pubkey FROM profileSetItem WHERE identifier = :identifier) "
    )
    suspend fun getEventRelayAuthorViewFromList(identifier: String): List<EventRelayAuthorView>

    @Query("SELECT relayUrl FROM mainEvent WHERE id = :id")
    suspend fun getEventRelay(id: EventIdHex): RelayUrl?

    @Query("SELECT DISTINCT(relayUrl) FROM mainEvent WHERE pubkey = :pubkey")
    fun getEventRelays(pubkey: PubkeyHex): Flow<List<RelayUrl>>
}
