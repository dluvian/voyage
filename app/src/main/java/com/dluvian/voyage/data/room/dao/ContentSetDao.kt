package com.dluvian.voyage.data.room.dao

import androidx.room.Dao
import androidx.room.Query

@Dao
interface ContentSetDao {
    @Query("SELECT MAX(createdAt) FROM profileSet")
    suspend fun getProfileSetMaxCreatedAt(): Long?

    @Query("SELECT MAX(createdAt) FROM topicSet")
    suspend fun getTopicSetMaxCreatedAt(): Long?
}
