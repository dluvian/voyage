package com.dluvian.voyage.data.room.dao

import androidx.room.Dao
import androidx.room.Query
import com.dluvian.voyage.core.PubkeyHex
import com.dluvian.voyage.core.Topic
import com.dluvian.voyage.data.room.view.RootPostView
import kotlinx.coroutines.flow.Flow

@Dao
interface RootPostDao {
    @Query(
        "SELECT * " +
                "FROM RootPostView " +
                "WHERE createdAt <= :until " +
                "AND (authorIsFriend OR myTopic IS NOT NULL) " +
                "ORDER BY createdAt DESC " +
                "LIMIT :size"
    )
    fun getHomeRootPostFlow(until: Long, size: Int): Flow<List<RootPostView>>

    @Query(
        "SELECT * " +
                "FROM RootPostView " +
                "WHERE createdAt <= :until " +
                "AND id IN (SELECT postId FROM hashtag WHERE hashtag = :topic) " +
                "ORDER BY createdAt DESC " +
                "LIMIT :size"
    )
    fun getTopicRootPostFlow(topic: Topic, until: Long, size: Int): Flow<List<RootPostView>>

    @Query(
        "SELECT * " +
                "FROM RootPostView " +
                "WHERE createdAt <= :until " +
                "AND pubkey = :pubkey " +
                "ORDER BY createdAt DESC " +
                "LIMIT :size"
    )
    fun getProfileRootPostFlow(pubkey: PubkeyHex, until: Long, size: Int): Flow<List<RootPostView>>
}
