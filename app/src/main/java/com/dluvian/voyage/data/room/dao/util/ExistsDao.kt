package com.dluvian.voyage.data.room.dao.util

import androidx.room.Dao
import androidx.room.Query
import com.dluvian.voyage.core.EventIdHex
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

@Dao
interface ExistsDao {

    suspend fun postExists(id: EventIdHex): Boolean {
        return internalRootPostExists(id = id) || internalLegacyReplyExists(id = id)
    }

    fun parentExistsFlow(id: EventIdHex): Flow<Boolean> {
        return combine(
            internalParentRootPostExistsFlow(id = id),
            internalParentLegacyReplyExistsFlow(id = id)
        ) { rootParentExists, replyParentExists ->
            rootParentExists || replyParentExists
        }
    }

    @Query("SELECT EXISTS (SELECT * FROM rootPost WHERE id = :id)")
    suspend fun internalRootPostExists(id: EventIdHex): Boolean

    @Query("SELECT EXISTS (SELECT * FROM legacyReply WHERE id = :id)")
    suspend fun internalLegacyReplyExists(id: EventIdHex): Boolean

    @Query(
        "SELECT EXISTS" +
                "(SELECT id FROM rootPost WHERE id = " +
                "(SELECT parentId FROM legacyReply WHERE id = :id))"
    )
    fun internalParentRootPostExistsFlow(id: EventIdHex): Flow<Boolean>

    @Query(
        "SELECT EXISTS" +
                "(SELECT id FROM legacyReply WHERE id = " +
                "(SELECT parentId FROM legacyReply WHERE id = :id))"
    )
    fun internalParentLegacyReplyExistsFlow(id: EventIdHex): Flow<Boolean>
}
