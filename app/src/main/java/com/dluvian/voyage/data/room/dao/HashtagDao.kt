package com.dluvian.voyage.data.room.dao

import androidx.room.Dao
import androidx.room.Query
import com.dluvian.voyage.core.EventIdHex
import com.dluvian.voyage.core.Topic

@Dao
interface HashtagDao {
    @Query("SELECT DISTINCT hashtag FROM hashtag WHERE eventId = :postId")
    suspend fun getHashtags(postId: EventIdHex): List<Topic>
}
