package com.dluvian.voyage.data.room.dao.upsert

import android.util.Log
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.dluvian.voyage.core.Topic
import com.dluvian.voyage.data.event.ValidatedTopicSet
import com.dluvian.voyage.data.room.entity.sets.TopicSetEntity
import com.dluvian.voyage.data.room.entity.sets.TopicSetItemEntity

private const val TAG = "TopicSetUpsertDao"

@Dao
interface TopicSetUpsertDao {
    @Transaction
    suspend fun upsertSet(set: ValidatedTopicSet) {
        val newestCreatedAt = internalGetNewestCreatedAt(identifier = set.identifier) ?: 1L
        if (set.createdAt <= newestCreatedAt) return

        val setEntity = TopicSetEntity.from(set = set)
        // RunCatching bc we might change account
        val result = runCatching { internalUpsert(set = setEntity) }
        if (result.isFailure) {
            Log.w(TAG, "Failed to upsert topic set: ${result.exceptionOrNull()?.message}")
            return
        }

        val inDb = internalGetTopics(identifier = set.identifier).toSet()
        val toInsert = set.topics.minus(inDb).map { topic ->
            TopicSetItemEntity(identifier = set.identifier, topic = topic)
        }
        if (toInsert.isNotEmpty()) internalInsert(entries = toInsert)

        val toDelete = inDb.minus(set.topics).map { topic ->
            TopicSetItemEntity(identifier = set.identifier, topic = topic)
        }
        if (toDelete.isNotEmpty()) internalDelete(entries = toDelete)
    }


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun internalUpsert(set: TopicSetEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun internalInsert(entries: Collection<TopicSetItemEntity>)

    @Delete
    suspend fun internalDelete(entries: Collection<TopicSetItemEntity>)

    @Query("SELECT MAX(createdAt) FROM topicSet WHERE identifier = :identifier")
    suspend fun internalGetNewestCreatedAt(identifier: String): Long?

    @Query("SELECT topic FROM topicSetItem WHERE identifier = :identifier")
    suspend fun internalGetTopics(identifier: String): List<Topic>
}
