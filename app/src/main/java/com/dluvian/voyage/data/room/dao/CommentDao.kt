package com.dluvian.voyage.data.room.dao

import androidx.room.Dao
import androidx.room.Query
import com.dluvian.voyage.core.EventIdHex
import com.dluvian.voyage.data.room.view.CommentView
import kotlinx.coroutines.flow.Flow

@Dao
interface CommentDao {
    @Query("SELECT * FROM CommentView WHERE parentId IN (:parentIds) ORDER BY createdAt ASC")
    fun getCommentsFlow(parentIds: Collection<EventIdHex>): Flow<List<CommentView>>
}
