package com.dluvian.voyage.data.room.dao

import androidx.room.Dao
import androidx.room.Query
import com.dluvian.voyage.core.EventIdHex

@Dao
interface ExistsDao {
    @Query("SELECT EXISTS (SELECT * FROM post WHERE id = :id)")
    suspend fun postExists(id: EventIdHex): Boolean
}
