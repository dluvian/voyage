package com.dluvian.voyage.data.room.dao

import androidx.room.Dao
import androidx.room.Query
import com.dluvian.voyage.core.EventIdHex
import com.dluvian.voyage.data.room.view.ReplyView
import kotlinx.coroutines.flow.Flow

@Dao
interface ReplyDao {
    @Query("SELECT * FROM ReplyView WHERE parentId IN (:parentIds) ORDER BY createdAt ASC")
    fun getReplyFlow(parentIds: Collection<EventIdHex>): Flow<List<ReplyView>>
}
