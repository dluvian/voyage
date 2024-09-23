package com.dluvian.voyage.data.room.dao.insert

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Transaction
import com.dluvian.voyage.data.event.ValidatedCrossPost
import com.dluvian.voyage.data.event.ValidatedLegacyReply
import com.dluvian.voyage.data.event.ValidatedMainEvent
import com.dluvian.voyage.data.event.ValidatedRootPost
import com.dluvian.voyage.data.room.entity.main.CrossPostMetaEntity
import com.dluvian.voyage.data.room.entity.main.HashtagEntity
import com.dluvian.voyage.data.room.entity.main.LegacyReplyMetaEntity
import com.dluvian.voyage.data.room.entity.main.MainEventEntity
import com.dluvian.voyage.data.room.entity.main.RootPostMetaEntity

@Dao
interface MainEventInsertDao {
    @Transaction
    suspend fun insertCrossPosts(crossPosts: Collection<ValidatedCrossPost>) {
        if (crossPosts.isEmpty()) return

        internalInsertMainEvents(mainEvents = crossPosts)
        internalInsertHashtags(mainEvents = crossPosts)

        val entities = crossPosts.map { CrossPostMetaEntity.from(crossPost = it) }
        internalInsertCrossPostMetaEntities(crossPosts = entities)
    }

    @Transaction
    suspend fun insertRootPosts(rootPosts: Collection<ValidatedRootPost>) {
        if (rootPosts.isEmpty()) return

        internalInsertMainEvents(mainEvents = rootPosts)
        internalInsertHashtags(mainEvents = rootPosts)

        val entities = rootPosts.map { RootPostMetaEntity.from(rootPost = it) }
        internalInsertRootPostMetaEntities(rootPosts = entities)
    }

    @Transaction
    suspend fun insertLegacyReplies(replies: Collection<ValidatedLegacyReply>) {
        if (replies.isEmpty()) return

        internalInsertMainEvents(mainEvents = replies)

        val entities = replies.map { LegacyReplyMetaEntity.from(legacyReply = it) }
        internalInsertLegacyReplyEntities(legacyReplies = entities)
    }

    suspend fun internalInsertMainEvents(mainEvents: Collection<ValidatedMainEvent>) {
        val mainEntities = mainEvents.map { MainEventEntity.from(mainEvent = it) }
        internalInsertMainEventsEntities(mainEvents = mainEntities)
    }

    suspend fun internalInsertHashtags(mainEvents: Collection<ValidatedMainEvent>) {
        val hashtags = mainEvents.flatMap { event ->
            when (event) {
                is ValidatedRootPost -> event.topics.map { topic ->
                    HashtagEntity(
                        eventId = event.id,
                        hashtag = topic
                    )
                }

                is ValidatedCrossPost -> event.topics.map { topic ->
                    HashtagEntity(
                        eventId = event.id,
                        hashtag = topic
                    )
                }

                is ValidatedLegacyReply -> emptyList()
            }
        }

        if (hashtags.isNotEmpty()) internalInsertHashtagsEntities(hashtags = hashtags)
    }

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun internalInsertHashtagsEntities(hashtags: Collection<HashtagEntity>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun internalInsertMainEventsEntities(mainEvents: Collection<MainEventEntity>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun internalInsertCrossPostMetaEntities(crossPosts: Collection<CrossPostMetaEntity>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun internalInsertRootPostMetaEntities(rootPosts: Collection<RootPostMetaEntity>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun internalInsertLegacyReplyEntities(legacyReplies: Collection<LegacyReplyMetaEntity>)
}
