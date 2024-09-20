package com.dluvian.voyage.data.room.dao.util

import androidx.room.Dao
import androidx.room.Query
import com.dluvian.voyage.core.EventIdHex
import kotlinx.coroutines.flow.Flow

@Dao
interface ExistsDao {

    // TODO: What if id is crossPostId ?
    @Query("SELECT EXISTS (SELECT * FROM mainEvent WHERE id = :id)")
    suspend fun postExists(id: EventIdHex): Boolean

    // TODO: What if id is crossPostId ?
    @Query(
        "SELECT EXISTS" +
                "(SELECT id FROM mainEvent WHERE id = " +
                "(SELECT parentId FROM legacyReply WHERE id = :id))"
    )
    fun parentExistsFlow(id: EventIdHex): Flow<Boolean>
}
