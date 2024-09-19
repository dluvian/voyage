package com.dluvian.voyage.data.room.dao

import androidx.room.Dao
import androidx.room.Query
import com.dluvian.voyage.core.EventIdHex
import com.dluvian.voyage.core.PubkeyHex
import com.dluvian.voyage.data.nostr.RelayUrl
import com.dluvian.voyage.data.room.view.EventRelayAuthorView
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

@Dao
interface EventRelayDao {
    @Query("SELECT * FROM EventRelayAuthorView WHERE pubkey IN (:authors)")
    suspend fun getEventRelayAuthorView(authors: Collection<PubkeyHex>): List<EventRelayAuthorView>

    @Query(
        "SELECT * FROM EventRelayAuthorView " +
                "WHERE pubkey IN (SELECT friendPubkey FROM friend) " +
                "AND pubkey NOT IN (SELECT pubkey FROM lock)"
    )
    suspend fun getFriendsEventRelayAuthorView(): List<EventRelayAuthorView>

    @Query(
        "SELECT * " +
                "FROM EventRelayAuthorView " +
                "WHERE pubkey " +
                "IN (SELECT pubkey FROM profileSetItem WHERE identifier = :identifier) " +
                "AND pubkey NOT IN (SELECT pubkey FROM lock)"
    )
    suspend fun getEventRelayAuthorViewFromList(identifier: String): List<EventRelayAuthorView>

    suspend fun getEventRelay(id: EventIdHex): RelayUrl? {
        return internalGetRootPostRelay(id = id) ?: internalGetLegacyReplyRelay(id = id)
    }

    fun getEventRelays(pubkey: PubkeyHex): Flow<List<RelayUrl>> {
        return combine(
            internalGetRootPostRelayFlow(pubkey = pubkey),
            internalGetLegacyReplyRelayFlow(pubkey = pubkey),
        ) { rootRelays, replyRelays ->
            (rootRelays + replyRelays).distinct()
        }
    }

    @Query("SELECT relayUrl FROM rootPost WHERE id = :id")
    suspend fun internalGetRootPostRelay(id: EventIdHex): RelayUrl?

    @Query("SELECT relayUrl FROM legacyReply WHERE id = :id")
    suspend fun internalGetLegacyReplyRelay(id: EventIdHex): RelayUrl?

    @Query("SELECT relayUrl FROM rootPost WHERE pubkey = :pubkey")
    fun internalGetRootPostRelayFlow(pubkey: PubkeyHex): Flow<List<RelayUrl>>

    @Query("SELECT relayUrl FROM legacyReply WHERE pubkey = :pubkey")
    fun internalGetLegacyReplyRelayFlow(pubkey: PubkeyHex): Flow<List<RelayUrl>>
}
