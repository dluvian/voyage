package com.dluvian.voyage.data.room.dao.insert

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.dluvian.voyage.core.EventIdHex
import com.dluvian.voyage.data.event.ValidatedCrossPost
import com.dluvian.voyage.data.event.ValidatedLegacyReply
import com.dluvian.voyage.data.event.ValidatedMainEvent
import com.dluvian.voyage.data.event.ValidatedRootPost
import com.dluvian.voyage.data.room.entity.HashtagEntity
import com.dluvian.voyage.data.room.entity.MainEventEntity

@Dao
interface MainEventInsertDao {

    @Transaction
    suspend fun insertRootPosts(rootPosts: Collection<ValidatedRootPost>) {
        internalInsertRootOrCrossPost(rootOrCrossPosts = rootPosts)
    }

    @Transaction
    suspend fun insertCrossPosts(crossPosts: Collection<ValidatedCrossPost>) {
        internalInsertRootOrCrossPost(rootOrCrossPosts = crossPosts)
    }

    @Transaction
    suspend fun insertLegacyReplies(replies: Collection<ValidatedLegacyReply>) {
        if (replies.isEmpty()) return

        val oldIds = filterAlreadyPresent(ids = replies.map { it.id })
        val newReplies = replies.filter { !oldIds.contains(it.id) }
        if (newReplies.isEmpty()) return

        val newEntities = newReplies.map { relayedItem -> MainEventEntity.from(relayedItem) }
        internalInsertMainEventOrIgnore(posts = newEntities)
    }

    @Transaction
    suspend fun internalInsertRootOrCrossPost(rootOrCrossPosts: Collection<ValidatedMainEvent>) {
        if (rootOrCrossPosts.isEmpty()) return

        val oldIds = filterAlreadyPresent(ids = rootOrCrossPosts.map { it.id })
        val newPosts = rootOrCrossPosts.filterNot { oldIds.contains(it.id) }
        if (newPosts.isEmpty()) return

        val newEntities = newPosts.map { post -> MainEventEntity.from(post) }
        internalInsertMainEventOrIgnore(posts = newEntities)

        val hashtags = newPosts.flatMap { post ->
            when (post) {
                is ValidatedRootPost -> post.topics.map { topic ->
                    HashtagEntity(eventId = post.id, hashtag = topic)
                }

                is ValidatedCrossPost -> post.topics.map { topic ->
                    HashtagEntity(eventId = post.id, hashtag = topic)
                }

                is ValidatedLegacyReply -> emptyList()
            }
        }
        if (hashtags.isNotEmpty()) internalInsertHashtagsOrIgnore(hashtags = hashtags)
    }

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun internalInsertMainEventOrIgnore(posts: Collection<MainEventEntity>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun internalInsertHashtagsOrIgnore(hashtags: Collection<HashtagEntity>)

    @Query("SELECT id FROM mainEvent WHERE id IN (:ids)")
    suspend fun filterAlreadyPresent(ids: Collection<EventIdHex>): List<EventIdHex>
}
