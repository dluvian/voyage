package com.dluvian.voyage.data.room.dao

import androidx.room.Dao
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CountDao {
    @Query("SELECT COUNT(*) FROM post WHERE parentId IS NULL")
    fun countRootPostsFlow(): Flow<Int>
}
