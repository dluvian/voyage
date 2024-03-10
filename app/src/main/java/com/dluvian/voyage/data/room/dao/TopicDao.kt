package com.dluvian.voyage.data.room.dao

import androidx.room.Dao
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TopicDao {
    @Query("SELECT topic FROM topic")
    fun getTopicsFlow(): Flow<List<String>>
}
