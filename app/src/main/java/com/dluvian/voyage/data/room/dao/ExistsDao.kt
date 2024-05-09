package com.dluvian.voyage.data.room.dao

import androidx.room.Dao
import androidx.room.Query
import com.dluvian.voyage.core.EventIdHex
import kotlinx.coroutines.flow.Flow

@Dao
interface ExistsDao {
    @Query("SELECT EXISTS (SELECT * FROM post WHERE id = :id)")
    suspend fun postExists(id: EventIdHex): Boolean

    @Query(
        "SELECT EXISTS" +
                "(SELECT id FROM post WHERE id = " +
                "(SELECT parentId FROM post WHERE id = :replyId))"
    )
    fun parentExistsFlow(replyId: EventIdHex): Flow<Boolean>
}
