package com.dluvian.voyage.data.room.dao.insert

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Transaction
import com.dluvian.voyage.data.event.ValidatedCrossPost
import com.dluvian.voyage.data.room.entity.main.CrossPostEntity
import com.dluvian.voyage.data.room.entity.main.HashtagEntity

@Dao
interface CrossPostInsertDao {
    @Transaction
    suspend fun insertCrossPosts(crossPosts: Collection<ValidatedCrossPost>) {
        if (crossPosts.isEmpty()) return

        val entities = crossPosts.map { CrossPostEntity.from(validatedCrossPost = it) }
        internalInsertCrossPosts(crossPosts = entities)

        val hashtags = crossPosts.flatMap { post ->
            post.topics.map { topic -> HashtagEntity(eventId = post.id, hashtag = topic) }
        }
        if (hashtags.isNotEmpty()) internalInsertHashtagsOrIgnore(hashtags = hashtags)
    }

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun internalInsertHashtagsOrIgnore(hashtags: Collection<HashtagEntity>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun internalInsertCrossPosts(crossPosts: Collection<CrossPostEntity>)
}
