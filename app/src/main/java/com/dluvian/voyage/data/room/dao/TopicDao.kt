package com.dluvian.voyage.data.room.dao

import androidx.room.Dao
import androidx.room.Query
import com.dluvian.voyage.core.Topic
import kotlinx.coroutines.flow.Flow

@Dao
interface TopicDao {
    @Query("SELECT topic FROM topic")
    fun getTopicsFlow(): Flow<List<Topic>>

    @Query("SELECT topic FROM topic UNION SELECT DISTINCT hashtag from hashtag")
    fun getAllTopicsFlow(): Flow<List<Topic>>
}
