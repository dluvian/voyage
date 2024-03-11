package com.dluvian.voyage.data.room.dao

import androidx.room.Dao
import androidx.room.Query
import com.dluvian.voyage.data.room.view.RootPostView
import kotlinx.coroutines.flow.Flow

@Dao
interface RootPostDao {
    @Query("SELECT * FROM RootPostView WHERE createdAt < :until ORDER BY createdAt DESC LIMIT :size")
    fun getRootPostFlow(until: Long, size: Int): Flow<List<RootPostView>>
}
