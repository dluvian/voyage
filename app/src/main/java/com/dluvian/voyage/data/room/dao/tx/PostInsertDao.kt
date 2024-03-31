package com.dluvian.voyage.data.room.dao.tx

import android.util.Log
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Transaction
import com.dluvian.voyage.data.event.ValidatedReply
import com.dluvian.voyage.data.event.ValidatedRootPost
import com.dluvian.voyage.data.model.RelayedItem
import com.dluvian.voyage.data.room.entity.EventRelayEntity
import com.dluvian.voyage.data.room.entity.HashtagEntity
import com.dluvian.voyage.data.room.entity.PostEntity

private const val TAG = "PostInsertDao"

@Dao
interface PostInsertDao {

    @Transaction
    suspend fun insertRelayedRootPosts(relayedPosts: Collection<RelayedItem<ValidatedRootPost>>) {
        if (relayedPosts.isEmpty()) return

        val entities = relayedPosts.map { relayedItem -> PostEntity.from(relayedItem.item) }

        internalInsertPostOrIgnore(posts = entities)
        val eventRelays = relayedPosts.map {
            EventRelayEntity(eventId = it.item.id, relayUrl = it.relayUrl)
        }
        internalInsertEventRelayOrIgnore(eventRelays = eventRelays)

        val hashtags = relayedPosts.flatMap { post ->
            post.item.topics.map { topic ->
                HashtagEntity(postId = post.item.id, hashtag = topic)
            }
        }
        if (hashtags.isNotEmpty()) internalInsertHashtagsOrIgnore(hashtags = hashtags)
    }

    @Transaction
    suspend fun insertRootPost(rootPost: ValidatedRootPost) {
        val entity = PostEntity.from(rootPost)
        internalInsertPostOrIgnore(posts = listOf(entity))

        val hashtags = rootPost.topics.map { topic ->
            HashtagEntity(postId = rootPost.id, hashtag = topic)
        }
        if (hashtags.isNotEmpty()) internalInsertHashtagsOrIgnore(hashtags = hashtags)
    }

    @Transaction
    suspend fun insertReplies(relayedReplies: Collection<RelayedItem<ValidatedReply>>) {
        if (relayedReplies.isEmpty()) return

        val entities = relayedReplies.map { relayedItem -> PostEntity.from(relayedItem.item) }
        val eventRelayEntities = relayedReplies
            .map { EventRelayEntity(eventId = it.item.id, relayUrl = it.relayUrl) }

        runCatching {
            internalInsertPostOrIgnore(posts = entities)
            internalInsertEventRelayOrIgnore(eventRelays = eventRelayEntities)
        }.onFailure {
            Log.w(TAG, "Failed to insert ${relayedReplies.size} comments: ${it.message}")
        }
    }

    suspend fun insertReply(reply: ValidatedReply) {
        val entity = PostEntity.from(reply)
        internalInsertPostOrIgnore(posts = listOf(entity))
    }

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun internalInsertPostOrIgnore(posts: Collection<PostEntity>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun internalInsertEventRelayOrIgnore(eventRelays: Collection<EventRelayEntity>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun internalInsertHashtagsOrIgnore(hashtags: Collection<HashtagEntity>)
}
