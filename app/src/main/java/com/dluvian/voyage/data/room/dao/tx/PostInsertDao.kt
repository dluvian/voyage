package com.dluvian.voyage.data.room.dao.tx

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.dluvian.voyage.core.EventIdHex
import com.dluvian.voyage.data.event.ValidatedCrossPost
import com.dluvian.voyage.data.event.ValidatedMainEvent
import com.dluvian.voyage.data.event.ValidatedReply
import com.dluvian.voyage.data.event.ValidatedRootPost
import com.dluvian.voyage.data.room.entity.HashtagEntity
import com.dluvian.voyage.data.room.entity.PostEntity

@Dao
interface PostInsertDao {

    @Transaction
    suspend fun insertRootPosts(posts: Collection<ValidatedRootPost>) {
        internalInsertRootOrCrossPosts(rootOrCross = posts)
    }

    @Transaction
    suspend fun insertCrossPosts(crossPosts: Collection<ValidatedCrossPost>) {
        internalInsertRootOrCrossPosts(rootOrCross = crossPosts)
    }

    @Transaction
    suspend fun insertReplies(replies: Collection<ValidatedReply>) {
        if (replies.isEmpty()) return

        val oldIds = filterOld(ids = replies.map { it.id })
        val newReplies = replies.filter { !oldIds.contains(it.id) }
        if (newReplies.isEmpty()) return

        val newEntities = newReplies.map { relayedItem -> PostEntity.from(relayedItem) }
        internalInsertPostOrIgnore(posts = newEntities)
    }

    suspend fun internalInsertRootOrCrossPosts(rootOrCross: Collection<ValidatedMainEvent>) {
        if (rootOrCross.isEmpty()) return

        val oldIds = filterOld(ids = rootOrCross.map { it.id })
        val newPosts = rootOrCross.filter { !oldIds.contains(it.id) }
        if (newPosts.isEmpty()) return

        val newEntities = newPosts.map { post -> PostEntity.from(post) }
        internalInsertPostOrIgnore(posts = newEntities)

        val hashtags = newPosts.flatMap { post ->
            post.topics.map { topic -> HashtagEntity(postId = post.id, hashtag = topic) }
        }
        if (hashtags.isNotEmpty()) internalInsertHashtagsOrIgnore(hashtags = hashtags)
    }

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun internalInsertPostOrIgnore(posts: Collection<PostEntity>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun internalInsertHashtagsOrIgnore(hashtags: Collection<HashtagEntity>)

    @Query("SELECT id FROM post WHERE id IN (:ids)")
    suspend fun filterOld(ids: Collection<EventIdHex>): List<EventIdHex>
}
