package com.dluvian.voyage.data.room.dao.insert

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Transaction
import com.dluvian.voyage.data.event.ValidatedRootPost
import com.dluvian.voyage.data.room.entity.HashtagEntity
import com.dluvian.voyage.data.room.entity.RootPostEntity

@Dao
interface RootPostInsertDao {
    @Transaction
    suspend fun insertRootPosts(rootPosts: Collection<ValidatedRootPost>) {
        if (rootPosts.isEmpty()) return

        val entities = rootPosts.map { RootPostEntity.from(validatedRootPost = it) }
        internalInsertRootPosts(rootPosts = entities)

        val hashtags = rootPosts.flatMap { post ->
            post.topics.map { topic -> HashtagEntity(eventId = post.id, hashtag = topic) }
        }
        if (hashtags.isNotEmpty()) internalInsertHashtagsOrIgnore(hashtags = hashtags)
    }

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun internalInsertHashtagsOrIgnore(hashtags: Collection<HashtagEntity>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun internalInsertRootPosts(rootPosts: Collection<RootPostEntity>)
}
