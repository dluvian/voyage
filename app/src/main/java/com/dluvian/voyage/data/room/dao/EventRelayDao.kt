package com.dluvian.voyage.data.room.dao

import androidx.room.Dao
import androidx.room.Query
import com.dluvian.voyage.core.PubkeyHex
import com.dluvian.voyage.data.room.view.EventRelayAuthorView

@Dao
interface EventRelayDao {

    @Query("SELECT * FROM EventRelayAuthorView WHERE pubkey IN (:authors)")
    suspend fun getEventRelays(authors: Collection<PubkeyHex>): List<EventRelayAuthorView>
}
