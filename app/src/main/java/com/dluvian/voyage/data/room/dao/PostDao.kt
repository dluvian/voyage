package com.dluvian.voyage.data.room.dao

import androidx.room.Dao
import androidx.room.Query
import com.dluvian.voyage.core.EventIdHex
import com.dluvian.voyage.core.PubkeyHex
import com.dluvian.voyage.data.room.entity.PostEntity
import com.dluvian.voyage.data.room.view.SimplePostView

@Dao
interface PostDao {
    @Query("SELECT * FROM post WHERE id = :id")
    suspend fun getPost(id: EventIdHex): PostEntity?

    @Query("SELECT pubkey FROM post WHERE id = :id")
    suspend fun getAuthor(id: EventIdHex): PubkeyHex?

    suspend fun getPostsByContent(content: String, limit: Int): List<SimplePostView> {
        if (limit <= 0) return emptyList()

        return internalGetPostsWithContentLike(somewhere = "%$content%", limit = limit)
    }

    @Query(
        "SELECT * FROM SimplePostView WHERE subject IS NOT NULL AND subject LIKE :somewhere " +
                "UNION " +
                "SELECT * FROM SimplePostView WHERE content LIKE :somewhere " +
                "LIMIT :limit"
    )
    suspend fun internalGetPostsWithContentLike(
        somewhere: String,
        limit: Int
    ): List<SimplePostView>
}
