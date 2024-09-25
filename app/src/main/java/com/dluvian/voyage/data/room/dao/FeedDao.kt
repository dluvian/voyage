package com.dluvian.voyage.data.room.dao

import androidx.room.Dao
import androidx.room.Query
import com.dluvian.voyage.core.PubkeyHex
import com.dluvian.voyage.core.Topic
import com.dluvian.voyage.data.room.view.CrossPostView
import com.dluvian.voyage.data.room.view.RootPostView
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

private const val CREATED_AT = "WHERE createdAt <= :until "
private const val ROOT = "FROM RootPostView $CREATED_AT "
private const val CROSS = "FROM CrossPostView $CREATED_AT "
private const val ORDER_AND_LIMIT = "ORDER BY createdAt DESC LIMIT :size"

private const val TOPIC_ROOT_COND = "AND authorIsMuted = 0 " +
        "AND authorIsLocked = 0 " +
        "AND id IN (SELECT eventId FROM hashtag WHERE hashtag = :topic) " +
        "AND NOT EXISTS (SELECT * FROM hashtag WHERE eventId = id AND hashtag IN (SELECT mutedItem FROM mute WHERE tag IS 't' AND mutedItem IS NOT :topic)) " +
        ORDER_AND_LIMIT

private const val TOPIC_CROSS_COND = "AND crossPostedAuthorIsOneself = 0 " +
        "AND crossPostedAuthorIsMuted = 0 " +
        TOPIC_ROOT_COND

private const val TOPIC_ROOT_QUERY = "SELECT * $ROOT $TOPIC_ROOT_COND"
private const val TOPIC_CREATED_AT_ROOT_QUERY = "SELECT createdAt $ROOT $TOPIC_ROOT_COND"
private const val TOPIC_EXISTS_ROOT_QUERY = "SELECT EXISTS($TOPIC_ROOT_QUERY)"

private const val TOPIC_CROSS_QUERY = "SELECT * $CROSS $TOPIC_CROSS_COND"
private const val TOPIC_CREATED_AT_CROSS_QUERY = "SELECT createdAt $CROSS $TOPIC_CROSS_COND"
private const val TOPIC_EXISTS_CROSS_QUERY = "SELECT EXISTS($TOPIC_CROSS_QUERY)"

private const val PROFILE_COND = "AND pubkey = :pubkey $ORDER_AND_LIMIT"

private const val PROFILE_ROOT_QUERY = "SELECT * $ROOT $PROFILE_COND"
private const val PROFILE_CREATED_AT_ROOT_QUERY = "SELECT createdAt $ROOT $PROFILE_COND"
private const val PROFILE_EXISTS_ROOT_QUERY = "SELECT EXISTS($PROFILE_ROOT_QUERY)"

private const val PROFILE_CROSS_QUERY = "SELECT * $CROSS $PROFILE_COND"
private const val PROFILE_CREATED_AT_CROSS_QUERY = "SELECT createdAt $CROSS $PROFILE_COND"
private const val PROFILE_EXISTS_CROSS_QUERY = "SELECT EXISTS($PROFILE_CROSS_QUERY)"


private const val LIST_ROOT = """
    AND (
        pubkey IN (SELECT pubkey FROM profileSetItem WHERE identifier = :identifier)
        OR id IN (SELECT eventId FROM hashtag WHERE hashtag IN (SELECT topic FROM topicSetItem WHERE identifier = :identifier))
    )
    AND authorIsMuted = 0 
    AND authorIsLocked = 0 
    AND NOT EXISTS (SELECT * FROM hashtag WHERE eventId = id AND hashtag IN (SELECT mutedItem FROM mute WHERE tag IS 't'))
""" + ORDER_AND_LIMIT

private const val LIST_CROSS = "AND crossPostedAuthorIsOneself = 0 " +
        "AND crossPostedAuthorIsMuted = 0 " +
        LIST_ROOT

private const val LIST_ROOT_QUERY = "SELECT * $ROOT $LIST_ROOT"
private const val LIST_CREATED_AT_ROOT_QUERY = "SELECT createdAt $ROOT $LIST_ROOT"
private const val LIST_EXISTS_ROOT_QUERY = "SELECT EXISTS($LIST_ROOT_QUERY)"

private const val LIST_CROSS_QUERY = "SELECT * $CROSS $LIST_CROSS"
private const val LIST_CREATED_AT_CROSS_QUERY = "SELECT createdAt $CROSS $LIST_CROSS"
private const val LIST_EXISTS_CROSS_QUERY = "SELECT EXISTS($LIST_CROSS_QUERY)"

@Dao
interface FeedDao {

    @Query(TOPIC_ROOT_QUERY)
    fun getTopicRootPostFlow(topic: Topic, until: Long, size: Int): Flow<List<RootPostView>>

    @Query(TOPIC_CROSS_QUERY)
    fun getTopicCrossPostFlow(topic: Topic, until: Long, size: Int): Flow<List<CrossPostView>>

    fun hasTopicFeedFlow(
        topic: Topic,
        until: Long = Long.MAX_VALUE,
        size: Int = 1
    ): Flow<Boolean> {
        return combine(
            internalTopicRootExistsFlow(topic = topic, until = until, size = size),
            internalTopicCrossExistsFlow(topic = topic, until = until, size = size),
        ) { root, cross -> root || cross }
    }

    @Query(TOPIC_ROOT_QUERY)
    suspend fun getTopicRootPosts(topic: Topic, until: Long, size: Int): List<RootPostView>

    @Query(TOPIC_CROSS_QUERY)
    suspend fun getTopicCrossPosts(topic: Topic, until: Long, size: Int): List<CrossPostView>

    suspend fun getTopicFeedCreatedAt(topic: Topic, until: Long, size: Int): List<Long> {
        return (internalGetTopicRootPostsCreatedAt(topic = topic, until = until, size = size) +
                internalGetTopicCrossPostsCreatedAt(topic = topic, until = until, size = size))
            .sortedDescending()
            .take(size)
    }

    @Query(PROFILE_ROOT_QUERY)
    fun getProfileRootPostFlow(pubkey: PubkeyHex, until: Long, size: Int): Flow<List<RootPostView>>

    @Query(PROFILE_CROSS_QUERY)
    fun getProfileCrossPostFlow(
        pubkey: PubkeyHex,
        until: Long,
        size: Int
    ): Flow<List<CrossPostView>>

    fun hasProfileFeedFlow(
        pubkey: PubkeyHex,
        until: Long = Long.MAX_VALUE,
        size: Int = 1
    ): Flow<Boolean> {
        return combine(
            internalProfileRootExistsFlow(pubkey = pubkey, until = until, size = size),
            internalProfileCrossExistsFlow(pubkey = pubkey, until = until, size = size),
        ) { root, cross -> root || cross }
    }

    @Query(PROFILE_ROOT_QUERY)
    suspend fun getProfileRootPosts(pubkey: PubkeyHex, until: Long, size: Int): List<RootPostView>

    @Query(PROFILE_CROSS_QUERY)
    suspend fun getProfileCrossPosts(pubkey: PubkeyHex, until: Long, size: Int): List<CrossPostView>

    suspend fun getProfileFeedCreatedAt(pubkey: PubkeyHex, until: Long, size: Int): List<Long> {
        return (internalGetProfileRootPostsCreatedAt(pubkey = pubkey, until = until, size = size) +
                internalGetProfileCrossPostsCreatedAt(pubkey = pubkey, until = until, size = size))
            .sortedDescending()
            .take(size)
    }

    @Query(LIST_ROOT_QUERY)
    fun getListRootPostFlow(identifier: String, until: Long, size: Int): Flow<List<RootPostView>>

    @Query(LIST_CROSS_QUERY)
    fun getListCrossPostFlow(identifier: String, until: Long, size: Int): Flow<List<CrossPostView>>

    fun hasListFeedFlow(
        identifier: String,
        until: Long = Long.MAX_VALUE,
        size: Int = 1
    ): Flow<Boolean> {
        return combine(
            internalListRootExistsFlow(identifier = identifier, until = until, size = size),
            internalListCrossExistsFlow(identifier = identifier, until = until, size = size),
        ) { root, cross -> root || cross }
    }

    @Query(LIST_ROOT_QUERY)
    suspend fun getListRootPosts(identifier: String, until: Long, size: Int): List<RootPostView>

    @Query(LIST_CROSS_QUERY)
    suspend fun getListCrossPosts(identifier: String, until: Long, size: Int): List<CrossPostView>

    suspend fun getListFeedCreatedAt(identifier: String, until: Long, size: Int): List<Long> {
        val root = internalGetListRootPostsCreatedAt(
            identifier = identifier,
            until = until,
            size = size
        )
        val cross = internalGetListCrossPostsCreatedAt(
            identifier = identifier,
            until = until,
            size = size
        )
        return (root + cross)
            .sortedDescending()
            .take(size)
    }


    @Query(TOPIC_EXISTS_ROOT_QUERY)
    fun internalTopicRootExistsFlow(topic: Topic, until: Long, size: Int): Flow<Boolean>

    @Query(TOPIC_EXISTS_CROSS_QUERY)
    fun internalTopicCrossExistsFlow(topic: Topic, until: Long, size: Int): Flow<Boolean>

    @Query(TOPIC_CREATED_AT_ROOT_QUERY)
    suspend fun internalGetTopicRootPostsCreatedAt(topic: Topic, until: Long, size: Int): List<Long>

    @Query(TOPIC_CREATED_AT_CROSS_QUERY)
    suspend fun internalGetTopicCrossPostsCreatedAt(
        topic: Topic,
        until: Long,
        size: Int
    ): List<Long>

    @Query(PROFILE_EXISTS_ROOT_QUERY)
    fun internalProfileRootExistsFlow(pubkey: PubkeyHex, until: Long, size: Int): Flow<Boolean>

    @Query(PROFILE_EXISTS_CROSS_QUERY)
    fun internalProfileCrossExistsFlow(pubkey: PubkeyHex, until: Long, size: Int): Flow<Boolean>

    @Query(PROFILE_CREATED_AT_ROOT_QUERY)
    suspend fun internalGetProfileRootPostsCreatedAt(
        pubkey: PubkeyHex,
        until: Long,
        size: Int
    ): List<Long>

    @Query(PROFILE_CREATED_AT_CROSS_QUERY)
    suspend fun internalGetProfileCrossPostsCreatedAt(
        pubkey: PubkeyHex,
        until: Long,
        size: Int
    ): List<Long>

    @Query(LIST_EXISTS_ROOT_QUERY)
    fun internalListRootExistsFlow(identifier: String, until: Long, size: Int): Flow<Boolean>

    @Query(LIST_EXISTS_CROSS_QUERY)
    fun internalListCrossExistsFlow(identifier: String, until: Long, size: Int): Flow<Boolean>

    @Query(LIST_CREATED_AT_ROOT_QUERY)
    suspend fun internalGetListRootPostsCreatedAt(
        identifier: String,
        until: Long,
        size: Int
    ): List<Long>

    @Query(LIST_CREATED_AT_CROSS_QUERY)
    suspend fun internalGetListCrossPostsCreatedAt(
        identifier: String,
        until: Long,
        size: Int
    ): List<Long>
}
