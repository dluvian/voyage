package com.dluvian.voyage.data.room.dao

import androidx.room.Dao
import androidx.room.Query
import com.dluvian.voyage.core.EventIdHex
import com.dluvian.voyage.data.room.entity.PostEntity

@Dao
interface PostDao {
    @Query("SELECT * FROM post WHERE id = :id")
    suspend fun getPost(id: EventIdHex): PostEntity?
}
