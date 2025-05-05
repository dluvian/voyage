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

private const val TOPIC_ROOT_COND =
    "AND id IN (SELECT eventId FROM hashtag WHERE hashtag = :topic) " +
        ORDER_AND_LIMIT

private const val TOPIC_CROSS_COND = "AND crossPostedAuthorIsOneself = 0 " +
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
""" + ORDER_AND_LIMIT

private const val LIST_CROSS = "AND crossPostedAuthorIsOneself = 0 " +
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
        return (internalGetTopicRootCreatedAt(topic = topic, until = until, size = size) +
                internalGetTopicCrossCreatedAt(topic = topic, until = until, size = size))
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
        return (internalGetProfileRootCreatedAt(pubkey = pubkey, until = until, size = size) +
                internalGetProfileCrossCreatedAt(pubkey = pubkey, until = until, size = size))
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
        return (internalGetListRootCreatedAt(identifier = identifier, until = until, size = size) +
                internalGetListCrossCreatedAt(identifier = identifier, until = until, size = size))
            .sortedDescending()
            .take(size)
    }

    @Query(TOPIC_EXISTS_ROOT_QUERY)
    fun internalTopicRootExistsFlow(topic: Topic, until: Long, size: Int): Flow<Boolean>

    @Query(TOPIC_EXISTS_CROSS_QUERY)
    fun internalTopicCrossExistsFlow(topic: Topic, until: Long, size: Int): Flow<Boolean>

    @Query(TOPIC_CREATED_AT_ROOT_QUERY)
    suspend fun internalGetTopicRootCreatedAt(topic: Topic, until: Long, size: Int): List<Long>

    @Query(TOPIC_CREATED_AT_CROSS_QUERY)
    suspend fun internalGetTopicCrossCreatedAt(topic: Topic, until: Long, size: Int): List<Long>

    @Query(PROFILE_EXISTS_ROOT_QUERY)
    fun internalProfileRootExistsFlow(pubkey: PubkeyHex, until: Long, size: Int): Flow<Boolean>

    @Query(PROFILE_EXISTS_CROSS_QUERY)
    fun internalProfileCrossExistsFlow(pubkey: PubkeyHex, until: Long, size: Int): Flow<Boolean>

    @Query(PROFILE_CREATED_AT_ROOT_QUERY)
    suspend fun internalGetProfileRootCreatedAt(
        pubkey: PubkeyHex,
        until: Long,
        size: Int
    ): List<Long>

    @Query(PROFILE_CREATED_AT_CROSS_QUERY)
    suspend fun internalGetProfileCrossCreatedAt(
        pubkey: PubkeyHex,
        until: Long,
        size: Int
    ): List<Long>

    @Query(LIST_EXISTS_ROOT_QUERY)
    fun internalListRootExistsFlow(identifier: String, until: Long, size: Int): Flow<Boolean>

    @Query(LIST_EXISTS_CROSS_QUERY)
    fun internalListCrossExistsFlow(identifier: String, until: Long, size: Int): Flow<Boolean>

    @Query(LIST_CREATED_AT_ROOT_QUERY)
    suspend fun internalGetListRootCreatedAt(
        identifier: String,
        until: Long,
        size: Int
    ): List<Long>

    @Query(LIST_CREATED_AT_CROSS_QUERY)
    suspend fun internalGetListCrossCreatedAt(
        identifier: String,
        until: Long,
        size: Int
    ): List<Long>
}
