package com.dluvian.voyage.data.room.dao.tx

import android.util.Log
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Transaction
import com.dluvian.voyage.data.event.ValidatedComment
import com.dluvian.voyage.data.event.ValidatedRootPost
import com.dluvian.voyage.data.model.RelayedItem
import com.dluvian.voyage.data.room.entity.HashtagEntity
import com.dluvian.voyage.data.room.entity.PostEntity
import com.dluvian.voyage.data.room.entity.PostRelayEntity

private const val TAG = "PostInsertDao"

@Dao
interface PostInsertDao {

    @Transaction
    suspend fun insertRelayedRootPosts(relayedPosts: Collection<RelayedItem<ValidatedRootPost>>) {
        if (relayedPosts.isEmpty()) return

        val entities = relayedPosts.map { relayedItem -> PostEntity.from(relayedItem.item) }

        internalInsertPostOrIgnore(posts = entities)
        val postRelays = relayedPosts.map {
            PostRelayEntity(postId = it.item.id, relayUrl = it.relayUrl)
        }
        internalInsertPostRelayOrIgnore(postRelays = postRelays)

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
    suspend fun insertComments(relayedComments: Collection<RelayedItem<ValidatedComment>>) {
        if (relayedComments.isEmpty()) return

        val entities = relayedComments.map { relayedItem -> PostEntity.from(relayedItem.item) }
        val postRelayEntities = relayedComments
            .map { PostRelayEntity(postId = it.item.id, relayUrl = it.relayUrl) }

        runCatching {
            internalInsertPostOrIgnore(posts = entities)
            internalInsertPostRelayOrIgnore(postRelays = postRelayEntities)
        }.onFailure {
            Log.w(TAG, "Failed to insert ${relayedComments.size} comments: ${it.message}")
        }
    }

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun internalInsertPostOrIgnore(posts: Collection<PostEntity>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun internalInsertPostRelayOrIgnore(postRelays: Collection<PostRelayEntity>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun internalInsertHashtagsOrIgnore(hashtags: Collection<HashtagEntity>)
}
