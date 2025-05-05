package com.dluvian.voyage.data.room.dao.insert

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Transaction
import com.dluvian.voyage.data.event.ValidatedComment
import com.dluvian.voyage.data.event.ValidatedCrossPost
import com.dluvian.voyage.data.event.ValidatedLegacyReply
import com.dluvian.voyage.data.event.ValidatedMainEvent
import com.dluvian.voyage.data.event.ValidatedRootPost
import com.dluvian.voyage.data.room.entity.main.CommentEntity
import com.dluvian.voyage.data.room.entity.main.CrossPostEntity
import com.dluvian.voyage.data.room.entity.main.HashtagEntity
import com.dluvian.voyage.data.room.entity.main.LegacyReplyEntity
import com.dluvian.voyage.data.room.entity.main.MainEventEntity
import com.dluvian.voyage.data.room.entity.main.RootPostEntity

@Dao
interface MainEventInsertDao {
    @Transaction
    suspend fun insertCrossPosts(crossPosts: Collection<ValidatedCrossPost>) {
        if (crossPosts.isEmpty()) return

        internalInsertMainEvents(mainEvents = crossPosts)
        internalInsertHashtags(mainEvents = crossPosts)

        val entities = crossPosts.map { CrossPostEntity.from(crossPost = it) }
        internalInsertCrossPostEntities(crossPosts = entities)
    }

    @Transaction
    suspend fun insertRootPosts(roots: Collection<ValidatedRootPost>) {
        if (roots.isEmpty()) return

        internalInsertMainEvents(mainEvents = roots)
        internalInsertHashtags(mainEvents = roots)
        internalInsertRootPostEntities(roots = roots.map { RootPostEntity.from(rootPost = it) })
    }

    @Transaction
    suspend fun insertLegacyReplies(replies: Collection<ValidatedLegacyReply>) {
        if (replies.isEmpty()) return

        internalInsertMainEvents(mainEvents = replies)

        val entities = replies.map { LegacyReplyEntity.from(legacyReply = it) }
        internalInsertLegacyReplyEntities(legacyReplies = entities)
    }

    @Transaction
    suspend fun insertComments(comments: Collection<ValidatedComment>) {
        if (comments.isEmpty()) return

        internalInsertMainEvents(mainEvents = comments)
        internalInsertCommentEntities(comments = comments.map { CommentEntity.from(comment = it) })
    }

    suspend fun internalInsertMainEvents(mainEvents: Collection<ValidatedMainEvent>) {
        val mainEntities = mainEvents.map { MainEventEntity.from(mainEvent = it) }
        internalInsertMainEventEntities(mainEvents = mainEntities)
    }

    suspend fun internalInsertHashtags(mainEvents: Collection<ValidatedMainEvent>) {
        val hashtags = mainEvents.flatMap { event ->
            when (event) {
                is ValidatedRootPost -> event.topics.map { topic ->
                    HashtagEntity(eventId = event.id, hashtag = topic)
                }

                is ValidatedCrossPost -> event.topics.map { topic ->
                    HashtagEntity(eventId = event.id, hashtag = topic)
                }

                // We don't index hashtags of replies
                is ValidatedLegacyReply, is ValidatedComment -> emptyList()
            }
        }

        if (hashtags.isNotEmpty()) internalInsertHashtagEntities(hashtags = hashtags)
    }

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun internalInsertHashtagEntities(hashtags: Collection<HashtagEntity>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun internalInsertMainEventEntities(mainEvents: Collection<MainEventEntity>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun internalInsertCrossPostEntities(crossPosts: Collection<CrossPostEntity>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun internalInsertRootPostEntities(roots: Collection<RootPostEntity>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun internalInsertLegacyReplyEntities(legacyReplies: Collection<LegacyReplyEntity>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun internalInsertCommentEntities(comments: Collection<CommentEntity>)
}
