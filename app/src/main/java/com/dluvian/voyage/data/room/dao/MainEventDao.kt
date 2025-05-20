package com.dluvian.voyage.data.room.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import com.dluvian.voyage.core.EventIdHex
import com.dluvian.voyage.core.PubkeyHex
import com.dluvian.voyage.data.model.PostDetailsBase
import com.dluvian.voyage.data.room.entity.main.MainEventEntity
import com.dluvian.voyage.data.room.view.SimplePostView
import kotlinx.coroutines.flow.Flow
import rust.nostr.sdk.Event
import rust.nostr.sdk.PublicKey

@Dao
interface MainEventDao {
    @Query("SELECT * FROM mainEvent WHERE id = :id")
    suspend fun getPost(id: EventIdHex): MainEventEntity?

    @Query("SELECT pubkey FROM mainEvent WHERE id = :id")
    suspend fun getAuthor(id: EventIdHex): PubkeyHex?

    @Query("SELECT pubkey FROM mainEvent WHERE id = :id")
    fun getAuthorFlow(id: EventIdHex): Flow<PubkeyHex?>

    @Query(
        "SELECT pubkey " +
                "FROM mainEvent " +
                "WHERE id = (SELECT parentId FROM legacyReply WHERE eventId = :id) " +
                "OR id = (SELECT parentId FROM comment WHERE eventId = :id)"
    )
    suspend fun getParentAuthor(id: EventIdHex): PubkeyHex?

    @Query("SELECT json FROM mainEvent WHERE id = :id")
    suspend fun getJson(id: EventIdHex): String?

    @Query("SELECT id, relayUrl AS firstSeenIn, createdAt, json FROM mainEvent WHERE id = :id")
    suspend fun getPostDetails(id: EventIdHex): PostDetailsBase?

    suspend fun getPostsByContent(content: String, limit: Int): List<SimplePostView> {
        if (limit <= 0) return emptyList()

        return internalGetPostsWithContentLike(somewhere = "%$content%", limit = limit)
    }

    @Query(
        "SELECT id " +
                "FROM mainEvent " +
                "WHERE (pubkey IN (SELECT pubkey FROM account) " +
                "OR id IN (SELECT eventId FROM bookmark)) " +
                "AND json IS NOT NULL " +
                "ORDER BY createdAt ASC"
    )
    suspend fun getBookmarkedAndMyPostIds(): List<EventIdHex>

    @Transaction
    suspend fun reindexMentions(newPubkey: PublicKey) {
        internalResetAllMentions()

        val ids = internalGetIndexableIds()
        for (id in ids) {
            val json = getJson(id = id)
            if (json.isNullOrEmpty()) continue

            val isMentioningMe =
                Event.fromJson(json = json).tags().publicKeys().any { newPubkey == it }
            if (isMentioningMe) internalSetMentioningMe(id = id)
        }

    }

    @Query("UPDATE mainEvent SET isMentioningMe = 0")
    suspend fun internalResetAllMentions()

    @Query("UPDATE mainEvent SET isMentioningMe = 1 WHERE id = :id")
    suspend fun internalSetMentioningMe(id: EventIdHex)

    // Limit by 1500 or else it might take too long
    @Query("SELECT id FROM mainEvent WHERE json IS NOT NULL ORDER BY createdAt DESC LIMIT 1500")
    suspend fun internalGetIndexableIds(): List<EventIdHex>

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
}
