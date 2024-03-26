package com.dluvian.voyage.data.room.dao

import androidx.room.Dao
import androidx.room.Query
import com.dluvian.voyage.core.EventIdHex
import com.dluvian.voyage.data.room.view.CommentView
import kotlinx.coroutines.flow.Flow

@Dao
interface CommentDao {
    @Query("SELECT * from CommentView WHERE parentId = :parentId")
    fun getCommentsFlow(parentId: EventIdHex): Flow<List<CommentView>>
}
