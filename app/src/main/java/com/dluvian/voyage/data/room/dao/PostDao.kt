package com.dluvian.voyage.data.room.dao

import androidx.room.Dao
import androidx.room.Query
import com.dluvian.voyage.core.EventIdHex
import com.dluvian.voyage.core.PubkeyHex
import com.dluvian.voyage.data.model.PostDetailsBase
import com.dluvian.voyage.data.room.entity.PostEntity
import com.dluvian.voyage.data.room.view.SimplePostView

@Dao
interface PostDao {
    @Query("SELECT * FROM post WHERE id = :id")
    suspend fun getPost(id: EventIdHex): PostEntity?

    @Query("SELECT pubkey FROM post WHERE id = :id")
    suspend fun getAuthor(id: EventIdHex): PubkeyHex?

    @Query("SELECT pubkey FROM post WHERE id = (SELECT parentId FROM post WHERE id = :id)")
    suspend fun getParentAuthor(id: EventIdHex): PubkeyHex?

    @Query("SELECT json FROM post WHERE id = :id")
    suspend fun getJson(id: EventIdHex): String?

    @Query("SELECT id, relayUrl AS firstSeenIn, json FROM post WHERE id = :id")
    suspend fun getPostDetails(id: EventIdHex): PostDetailsBase?

    suspend fun getPostsByContent(content: String, limit: Int): List<SimplePostView> {
        if (limit <= 0) return emptyList()

        return internalGetPostsWithContentLike(somewhere = "%$content%", limit = limit)
    }

    @Query(
        "SELECT * FROM SimplePostView " +
                "WHERE subject IS NOT NULL " +
                "AND subject LIKE :somewhere " +
                "AND pubkey NOT IN (SELECT mutedItem FROM mute WHERE mutedItem = pubkey AND tag = 'p')" +
                "UNION " +
                "SELECT * FROM SimplePostView " +
                "WHERE content LIKE :somewhere " +
                "AND pubkey NOT IN (SELECT mutedItem FROM mute WHERE mutedItem = pubkey AND tag = 'p')" +
                "LIMIT :limit"
    )
    suspend fun internalGetPostsWithContentLike(
        somewhere: String,
        limit: Int
    ): List<SimplePostView>

    @Query(
        "SELECT id " +
                "FROM post " +
                "WHERE (pubkey IN (SELECT pubkey FROM account) " +
                "OR id IN (SELECT postId FROM bookmark)) " +
                "AND json IS NOT NULL " +
                "ORDER BY createdAt ASC"
    )
    suspend fun getBookmarkedAndMyPostIds(): List<EventIdHex>
}
