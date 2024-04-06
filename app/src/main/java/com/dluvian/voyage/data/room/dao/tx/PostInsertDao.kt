package com.dluvian.voyage.data.room.dao.tx

import android.util.Log
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Transaction
import com.dluvian.voyage.data.event.ValidatedReply
import com.dluvian.voyage.data.event.ValidatedRootPost
import com.dluvian.voyage.data.room.entity.HashtagEntity
import com.dluvian.voyage.data.room.entity.PostEntity

private const val TAG = "PostInsertDao"

@Dao
interface PostInsertDao {

    @Transaction
    suspend fun insertRelayedRootPosts(posts: Collection<ValidatedRootPost>) {
        if (posts.isEmpty()) return

        val entities = posts.map { post -> PostEntity.from(post) }
        internalInsertPostOrReplace(posts = entities)

        val hashtags = posts.flatMap { post ->
            post.topics.map { topic -> HashtagEntity(postId = post.id, hashtag = topic) }
        }
        if (hashtags.isNotEmpty()) internalInsertHashtagsOrIgnore(hashtags = hashtags)
    }

    @Transaction
    suspend fun insertRootPost(rootPost: ValidatedRootPost) {
        val entity = PostEntity.from(rootPost)
        internalInsertPostOrReplace(posts = listOf(entity))

        val hashtags = rootPost.topics.map { topic ->
            HashtagEntity(postId = rootPost.id, hashtag = topic)
        }
        if (hashtags.isNotEmpty()) internalInsertHashtagsOrIgnore(hashtags = hashtags)
    }

    @Transaction
    suspend fun insertReplies(replies: Collection<ValidatedReply>) {
        if (replies.isEmpty()) return

        val entities = replies.map { relayedItem -> PostEntity.from(relayedItem) }

        runCatching {
            internalInsertPostOrReplace(posts = entities)
        }.onFailure {
            Log.w(TAG, "Failed to insert ${replies.size} comments: ${it.message}")
        }
    }

    suspend fun insertReply(reply: ValidatedReply) {
        val entity = PostEntity.from(reply)
        internalInsertPostOrReplace(posts = listOf(entity))
    }

    // Replace bc after writing post/reply we set a relayUrl that might not be valid.
    // It needs to be updated
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun internalInsertPostOrReplace(posts: Collection<PostEntity>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun internalInsertHashtagsOrIgnore(hashtags: Collection<HashtagEntity>)
}
