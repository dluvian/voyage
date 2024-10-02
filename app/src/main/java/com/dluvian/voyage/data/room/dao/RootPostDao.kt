package com.dluvian.voyage.data.room.dao

import androidx.room.Dao
import androidx.room.Query
import com.dluvian.voyage.core.EventIdHex
import com.dluvian.voyage.data.room.view.RootPostView
import kotlinx.coroutines.flow.Flow

// TODO: Move to SomeReplyDao
@Dao
interface RootPostDao {
    @Query("SELECT * FROM RootPostView WHERE id = :id")
    fun getRootPostFlow(id: EventIdHex): Flow<RootPostView?>
}
