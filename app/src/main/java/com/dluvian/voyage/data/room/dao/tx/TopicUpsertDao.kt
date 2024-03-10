package com.dluvian.voyage.data.room.dao.tx

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.dluvian.voyage.core.PubkeyHex
import com.dluvian.voyage.data.model.ValidatedTopicList
import com.dluvian.voyage.data.room.entity.TopicEntity

@Dao
interface TopicUpsertDao {
    @Transaction
    suspend fun upsertTopics(validatedTopicList: ValidatedTopicList) {
        val list = TopicEntity.from(validatedTopicList = validatedTopicList)
        val myPubkey = validatedTopicList.myPubkey.toHex()

        val newestCreatedAt = internalGetNewestCreatedAt(myPubkey = myPubkey) ?: 0L
        if (validatedTopicList.createdAt < newestCreatedAt) return

        if (list.isEmpty()) {
            internalDeleteList(myPubkey = myPubkey)
            return
        }

        internalUpsert(topicEntities = list)
        internalDeleteOutdated(newestCreatedAt = validatedTopicList.createdAt)
    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun internalUpsert(topicEntities: Collection<TopicEntity>)

    @Query("SELECT MAX(createdAt) FROM topic WHERE myPubkey = :myPubkey")
    suspend fun internalGetNewestCreatedAt(myPubkey: PubkeyHex): Long?

    @Query("DELETE FROM topic WHERE myPubkey = :myPubkey")
    suspend fun internalDeleteList(myPubkey: PubkeyHex)

    @Query("DELETE FROM topic WHERE createdAt < :newestCreatedAt")
    suspend fun internalDeleteOutdated(newestCreatedAt: Long)
}
