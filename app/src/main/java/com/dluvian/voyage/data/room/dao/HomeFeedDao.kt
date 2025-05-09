package com.dluvian.voyage.data.room.dao

import androidx.room.Dao
import androidx.room.Query
import com.dluvian.voyage.data.model.FriendPubkeys
import com.dluvian.voyage.data.model.Global
import com.dluvian.voyage.data.model.HomeFeedSetting
import com.dluvian.voyage.data.model.NoPubkeys
import com.dluvian.voyage.data.model.WebOfTrustPubkeys
import com.dluvian.voyage.data.room.view.CrossPostView
import com.dluvian.voyage.data.room.view.RootPostView
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf

private const val CREATED_AT = "WHERE createdAt <= :until "
private const val ROOT = "FROM RootPostView $CREATED_AT"
private const val CROSS = "FROM CrossPostView $CREATED_AT"

private const val ROOT_COND = "AND authorIsOneself = 0 " +
        "ORDER BY createdAt DESC " +
        "LIMIT :size"

private const val CROSS_COND = "AND crossPostedAuthorIsOneself = 0 " +
        ROOT_COND


private const val TOPIC_ONLY_COND = "AND myTopic IS NOT NULL "
private const val FRIEND_ONLY_COND = "AND authorIsFriend "
private const val WOT_ONLY_COND = "AND (authorIsTrusted OR authorIsFriend) "
private const val FRIEND_OR_TOPIC_COND = "AND (authorIsFriend OR myTopic IS NOT NULL) "
private const val WOT_OR_TOPIC_COND =
    "AND (authorIsTrusted OR authorIsFriend OR myTopic IS NOT NULL) "

private const val TOPIC_ONLY_MAIN_ROOT = "$ROOT $TOPIC_ONLY_COND $ROOT_COND"
private const val TOPIC_ONLY_ROOT_QUERY = "SELECT * $TOPIC_ONLY_MAIN_ROOT"
private const val TOPIC_ONLY_CREATED_AT_ROOT_QUERY = "SELECT createdAt $TOPIC_ONLY_MAIN_ROOT"
private const val TOPIC_ONLY_EXISTS_ROOT_QUERY = "SELECT EXISTS($TOPIC_ONLY_ROOT_QUERY)"

private const val TOPIC_ONLY_MAIN_CROSS = "$CROSS $TOPIC_ONLY_COND $CROSS_COND"
private const val TOPIC_ONLY_CROSS_QUERY = "SELECT * $TOPIC_ONLY_MAIN_CROSS"
private const val TOPIC_ONLY_CREATED_AT_CROSS_QUERY = "SELECT createdAt $TOPIC_ONLY_MAIN_CROSS"
private const val TOPIC_ONLY_EXISTS_CROSS_QUERY = "SELECT EXISTS($TOPIC_ONLY_CROSS_QUERY)"

private const val FRIEND_ONLY_MAIN_ROOT = "$ROOT $FRIEND_ONLY_COND $ROOT_COND"
private const val FRIEND_ONLY_ROOT_QUERY = "SELECT * $FRIEND_ONLY_MAIN_ROOT"
private const val FRIEND_ONLY_CREATED_AT_ROOT_QUERY = "SELECT createdAt $FRIEND_ONLY_MAIN_ROOT"
private const val FRIEND_ONLY_EXISTS_ROOT_QUERY = "SELECT EXISTS($FRIEND_ONLY_ROOT_QUERY)"

private const val FRIEND_ONLY_MAIN_CROSS = "$CROSS $FRIEND_ONLY_COND $CROSS_COND"
private const val FRIEND_ONLY_CROSS_QUERY = "SELECT * $FRIEND_ONLY_MAIN_CROSS"
private const val FRIEND_ONLY_CREATED_AT_CROSS_QUERY = "SELECT createdAt $FRIEND_ONLY_MAIN_CROSS"
private const val FRIEND_ONLY_EXISTS_CROSS_QUERY = "SELECT EXISTS($FRIEND_ONLY_CROSS_QUERY)"

private const val WOT_ONLY_MAIN_ROOT = "$ROOT $WOT_ONLY_COND $ROOT_COND"
private const val WOT_ONLY_ROOT_QUERY = "SELECT * $WOT_ONLY_MAIN_ROOT"
private const val WOT_ONLY_CREATED_AT_ROOT_QUERY = "SELECT createdAt $WOT_ONLY_MAIN_ROOT"
private const val WOT_ONLY_EXISTS_ROOT_QUERY = "SELECT EXISTS($WOT_ONLY_ROOT_QUERY)"

private const val WOT_ONLY_MAIN_CROSS = "$CROSS $WOT_ONLY_COND $CROSS_COND"
private const val WOT_ONLY_CROSS_QUERY = "SELECT * $WOT_ONLY_MAIN_CROSS"
private const val WOT_ONLY_CREATED_AT_CROSS_QUERY = "SELECT createdAt $WOT_ONLY_MAIN_CROSS"
private const val WOT_ONLY_EXISTS_CROSS_QUERY = "SELECT EXISTS($WOT_ONLY_CROSS_QUERY)"

private const val FRIEND_OR_TOPIC_MAIN_ROOT = "$ROOT $FRIEND_OR_TOPIC_COND $ROOT_COND"
private const val FRIEND_OR_TOPIC_ROOT_QUERY = "SELECT * $FRIEND_OR_TOPIC_MAIN_ROOT"
private const val FRIEND_OR_TOPIC_CREATED_AT_ROOT_QUERY =
    "SELECT createdAt $FRIEND_OR_TOPIC_MAIN_ROOT"
private const val FRIEND_OR_TOPIC_EXISTS_ROOT_QUERY = "SELECT EXISTS($FRIEND_OR_TOPIC_ROOT_QUERY)"

private const val FRIEND_OR_TOPIC_MAIN_CROSS = "$CROSS $FRIEND_OR_TOPIC_COND $CROSS_COND"
private const val FRIEND_OR_TOPIC_CROSS_QUERY = "SELECT * $FRIEND_OR_TOPIC_MAIN_CROSS"
private const val FRIEND_OR_TOPIC_CREATED_AT_CROSS_QUERY =
    "SELECT createdAt $FRIEND_OR_TOPIC_MAIN_CROSS"
private const val FRIEND_OR_TOPIC_EXISTS_CROSS_QUERY = "SELECT EXISTS($FRIEND_OR_TOPIC_CROSS_QUERY)"

private const val WOT_OR_TOPIC_MAIN_ROOT = "$ROOT $WOT_OR_TOPIC_COND $ROOT_COND"
private const val WOT_OR_TOPIC_ROOT_QUERY = "SELECT * $WOT_OR_TOPIC_MAIN_ROOT"
private const val WOT_OR_TOPIC_CREATED_AT_ROOT_QUERY = "SELECT createdAt $WOT_OR_TOPIC_MAIN_ROOT"
private const val WOT_OR_TOPIC_EXISTS_ROOT_QUERY = "SELECT EXISTS($WOT_OR_TOPIC_ROOT_QUERY)"

private const val WOT_OR_TOPIC_MAIN_CROSS = "$CROSS $WOT_OR_TOPIC_COND $CROSS_COND"
private const val WOT_OR_TOPIC_CROSS_QUERY = "SELECT * $WOT_OR_TOPIC_MAIN_CROSS"
private const val WOT_OR_TOPIC_CREATED_AT_CROSS_QUERY = "SELECT createdAt $WOT_OR_TOPIC_MAIN_CROSS"
private const val WOT_OR_TOPIC_EXISTS_CROSS_QUERY = "SELECT EXISTS($WOT_OR_TOPIC_CROSS_QUERY)"

private const val GLOBAL_MAIN_ROOT = "$ROOT $ROOT_COND"
private const val GLOBAL_ROOT_QUERY = "SELECT * $GLOBAL_MAIN_ROOT"
private const val GLOBAL_CREATED_AT_ROOT_QUERY = "SELECT createdAt $GLOBAL_MAIN_ROOT"
private const val GLOBAL_EXISTS_ROOT_QUERY = "SELECT EXISTS($GLOBAL_ROOT_QUERY)"

private const val GLOBAL_MAIN_CROSS = "$CROSS $CROSS_COND"
private const val GLOBAL_CROSS_QUERY = "SELECT * $GLOBAL_MAIN_CROSS"
private const val GLOBAL_CREATED_AT_CROSS_QUERY = "SELECT createdAt $GLOBAL_MAIN_CROSS"
private const val GLOBAL_EXISTS_CROSS_QUERY = "SELECT EXISTS($GLOBAL_CROSS_QUERY)"

@Dao
interface HomeFeedDao {

    fun getHomeRootPostFlow(
        setting: HomeFeedSetting,
        until: Long,
        size: Int,
    ): Flow<List<RootPostView>> {
        val withMyTopics = setting.topicSelection.isMyTopics()

        return when (setting.pubkeySelection) {
            NoPubkeys -> if (withMyTopics) {
                internalGetTopicRootFlow(until = until, size = size)
            } else {
                flowOf(emptyList())
            }

            FriendPubkeys -> if (withMyTopics) {
                internalGetFriendOrTopicRootFlow(until = until, size = size)
            } else {
                internalGetFriendRootFlow(until = until, size = size)
            }

            WebOfTrustPubkeys -> if (withMyTopics) {
                internalGetWotOrTopicRootFlow(until = until, size = size)
            } else {
                internalGetWotRootFlow(until = until, size = size)
            }

            Global -> internalGetGlobalRootFlow(until = until, size = size)
        }
    }

    fun getHomeCrossPostFlow(
        setting: HomeFeedSetting,
        until: Long,
        size: Int,
    ): Flow<List<CrossPostView>> {
        val withMyTopics = setting.topicSelection.isMyTopics()

        return when (setting.pubkeySelection) {
            NoPubkeys -> if (withMyTopics) {
                internalGetTopicCrossFlow(until = until, size = size)
            } else {
                flowOf(emptyList())
            }

            FriendPubkeys -> if (withMyTopics) {
                internalGetFriendOrTopicCrossFlow(until = until, size = size)
            } else {
                internalGetFriendCrossFlow(until = until, size = size)
            }

            WebOfTrustPubkeys -> if (withMyTopics) {
                internalGetWotOrTopicCrossFlow(until = until, size = size)
            } else {
                internalGetWotCrossFlow(until = until, size = size)
            }

            Global -> internalGetGlobalCrossFlow(until = until, size = size)
        }
    }

    suspend fun getHomeRootPosts(
        setting: HomeFeedSetting,
        until: Long,
        size: Int
    ): List<RootPostView> {
        val withMyTopics = setting.topicSelection.isMyTopics()

        return when (setting.pubkeySelection) {
            NoPubkeys -> if (withMyTopics) {
                internalGetTopicRoot(until = until, size = size)
            } else {
                emptyList()
            }

            FriendPubkeys -> if (withMyTopics) {
                internalGetFriendOrTopicRoot(until = until, size = size)
            } else {
                internalGetFriendRoot(until = until, size = size)
            }

            WebOfTrustPubkeys -> if (withMyTopics) {
                internalGetWotOrTopicRoot(until = until, size = size)
            } else {
                internalGetWotRoot(until = until, size = size)
            }

            Global -> internalGetGlobalRoot(until = until, size = size)
        }
    }

    suspend fun getHomeCrossPosts(
        setting: HomeFeedSetting,
        until: Long,
        size: Int
    ): List<CrossPostView> {
        val withMyTopics = setting.topicSelection.isMyTopics()

        return when (setting.pubkeySelection) {
            NoPubkeys -> if (withMyTopics) {
                internalGetTopicCross(until = until, size = size)
            } else {
                emptyList()
            }

            FriendPubkeys -> if (withMyTopics) {
                internalGetFriendOrTopicCross(until = until, size = size)
            } else {
                internalGetFriendCross(until = until, size = size)
            }

            WebOfTrustPubkeys -> if (withMyTopics) {
                internalGetWotOrTopicCross(until = until, size = size)
            } else {
                internalGetWotCross(until = until, size = size)
            }

            Global -> internalGetGlobalCross(until = until, size = size)
        }
    }

    fun hasHomeFeedFlow(
        setting: HomeFeedSetting,
        until: Long = Long.MAX_VALUE,
        size: Int = 1
    ): Flow<Boolean> {
        val withMyTopics = setting.topicSelection.isMyTopics()

        return when (setting.pubkeySelection) {
            NoPubkeys -> if (withMyTopics) {
                combine(
                    internalHasTopicRootFlow(until = until, size = size),
                    internalHasTopicCrossFlow(until = until, size = size),
                ) { root, cross -> root || cross }
            } else {
                flowOf(false)
            }

            FriendPubkeys -> if (withMyTopics) {
                combine(
                    internalHasFriendOrTopicRootFlow(
                        until = until,
                        size = size
                    ),
                    internalHasFriendOrTopicCrossFlow(
                        until = until,
                        size = size
                    ),
                ) { root, cross -> root || cross }
            } else {
                combine(
                    internalHasFriendRootFlow(until = until, size = size),
                    internalHasFriendCrossFlow(until = until, size = size),
                ) { root, cross -> root || cross }
            }

            WebOfTrustPubkeys -> if (withMyTopics) {
                combine(
                    internalHasWotOrTopicRootFlow(until = until, size = size),
                    internalHasWotOrTopicCrossFlow(until = until, size = size),
                ) { root, cross -> root || cross }
            } else {
                combine(
                    internalHasWotRootFlow(until = until, size = size),
                    internalHasWotCrossFlow(until = until, size = size)
                ) { root, cross -> root || cross }
            }

            Global -> {
                combine(
                    internalHasGlobalRootFlow(until = until, size = size),
                    internalHasGlobalCrossFlow(until = until, size = size)
                ) { root, cross -> root || cross }
            }
        }
    }

    suspend fun getHomeFeedCreatedAt(
        setting: HomeFeedSetting,
        until: Long,
        size: Int
    ): List<Long> {
        val withMyTopics = setting.topicSelection.isMyTopics()

        return when (setting.pubkeySelection) {
            NoPubkeys -> if (withMyTopics) {
                internalGetTopicCreatedAtRoot(until = until, size = size) +
                        internalGetTopicCreatedAtCross(until = until, size = size)
            } else {
                emptyList()
            }

            FriendPubkeys -> if (withMyTopics) {
                internalGetFriendOrTopicCreatedAtRoot(until = until, size = size) +
                        internalGetFriendOrTopicCreatedAtCross(until = until, size = size)
            } else {
                internalGetFriendCreatedAtRoot(until = until, size = size) +
                        internalGetFriendCreatedAtCross(until = until, size = size)
            }

            WebOfTrustPubkeys -> if (withMyTopics) {
                internalGetWotOrTopicCreatedAtRoot(until = until, size = size) +
                        internalGetWotOrTopicCreatedAtCross(until = until, size = size)
            } else {
                internalGetWotCreatedAtRoot(until = until, size = size) +
                        internalGetWotCreatedAtCross(until = until, size = size)
            }

            Global -> internalGetGlobalCreatedAtRoot(until = until, size = size) +
                    internalGetGlobalCreatedAtCross(until = until, size = size)
        }
            .sortedDescending()
            .take(size)
    }

    @Query(TOPIC_ONLY_ROOT_QUERY)
    fun internalGetTopicRootFlow(until: Long, size: Int): Flow<List<RootPostView>>

    @Query(TOPIC_ONLY_CROSS_QUERY)
    fun internalGetTopicCrossFlow(until: Long, size: Int): Flow<List<CrossPostView>>

    @Query(FRIEND_OR_TOPIC_ROOT_QUERY)
    fun internalGetFriendOrTopicRootFlow(until: Long, size: Int): Flow<List<RootPostView>>

    @Query(FRIEND_OR_TOPIC_CROSS_QUERY)
    fun internalGetFriendOrTopicCrossFlow(until: Long, size: Int): Flow<List<CrossPostView>>

    @Query(FRIEND_ONLY_ROOT_QUERY)
    fun internalGetFriendRootFlow(until: Long, size: Int): Flow<List<RootPostView>>

    @Query(FRIEND_ONLY_CROSS_QUERY)
    fun internalGetFriendCrossFlow(until: Long, size: Int): Flow<List<CrossPostView>>

    @Query(WOT_OR_TOPIC_ROOT_QUERY)
    fun internalGetWotOrTopicRootFlow(until: Long, size: Int): Flow<List<RootPostView>>

    @Query(WOT_OR_TOPIC_CROSS_QUERY)
    fun internalGetWotOrTopicCrossFlow(until: Long, size: Int): Flow<List<CrossPostView>>

    @Query(WOT_ONLY_ROOT_QUERY)
    fun internalGetWotRootFlow(until: Long, size: Int): Flow<List<RootPostView>>

    @Query(WOT_ONLY_CROSS_QUERY)
    fun internalGetWotCrossFlow(until: Long, size: Int): Flow<List<CrossPostView>>

    @Query(GLOBAL_ROOT_QUERY)
    fun internalGetGlobalRootFlow(until: Long, size: Int): Flow<List<RootPostView>>

    @Query(GLOBAL_CROSS_QUERY)
    fun internalGetGlobalCrossFlow(until: Long, size: Int): Flow<List<CrossPostView>>

    @Query(TOPIC_ONLY_ROOT_QUERY)
    suspend fun internalGetTopicRoot(until: Long, size: Int): List<RootPostView>

    @Query(TOPIC_ONLY_CROSS_QUERY)
    suspend fun internalGetTopicCross(until: Long, size: Int): List<CrossPostView>

    @Query(FRIEND_OR_TOPIC_ROOT_QUERY)
    suspend fun internalGetFriendOrTopicRoot(until: Long, size: Int): List<RootPostView>

    @Query(FRIEND_OR_TOPIC_CROSS_QUERY)
    suspend fun internalGetFriendOrTopicCross(until: Long, size: Int): List<CrossPostView>

    @Query(FRIEND_ONLY_ROOT_QUERY)
    suspend fun internalGetFriendRoot(until: Long, size: Int): List<RootPostView>

    @Query(FRIEND_ONLY_CROSS_QUERY)
    suspend fun internalGetFriendCross(until: Long, size: Int): List<CrossPostView>

    @Query(WOT_OR_TOPIC_ROOT_QUERY)
    suspend fun internalGetWotOrTopicRoot(until: Long, size: Int): List<RootPostView>

    @Query(WOT_OR_TOPIC_CROSS_QUERY)
    suspend fun internalGetWotOrTopicCross(until: Long, size: Int): List<CrossPostView>

    @Query(WOT_ONLY_ROOT_QUERY)
    suspend fun internalGetWotRoot(until: Long, size: Int): List<RootPostView>

    @Query(WOT_ONLY_CROSS_QUERY)
    suspend fun internalGetWotCross(until: Long, size: Int): List<CrossPostView>

    @Query(GLOBAL_ROOT_QUERY)
    suspend fun internalGetGlobalRoot(until: Long, size: Int): List<RootPostView>

    @Query(GLOBAL_CROSS_QUERY)
    suspend fun internalGetGlobalCross(until: Long, size: Int): List<CrossPostView>

    @Query(TOPIC_ONLY_EXISTS_ROOT_QUERY)
    fun internalHasTopicRootFlow(until: Long, size: Int): Flow<Boolean>

    @Query(TOPIC_ONLY_EXISTS_CROSS_QUERY)
    fun internalHasTopicCrossFlow(until: Long, size: Int): Flow<Boolean>

    @Query(FRIEND_OR_TOPIC_EXISTS_ROOT_QUERY)
    fun internalHasFriendOrTopicRootFlow(until: Long, size: Int): Flow<Boolean>

    @Query(FRIEND_OR_TOPIC_EXISTS_CROSS_QUERY)
    fun internalHasFriendOrTopicCrossFlow(until: Long, size: Int): Flow<Boolean>

    @Query(FRIEND_ONLY_EXISTS_ROOT_QUERY)
    fun internalHasFriendRootFlow(until: Long, size: Int): Flow<Boolean>

    @Query(FRIEND_ONLY_EXISTS_CROSS_QUERY)
    fun internalHasFriendCrossFlow(until: Long, size: Int): Flow<Boolean>

    @Query(WOT_OR_TOPIC_EXISTS_ROOT_QUERY)
    fun internalHasWotOrTopicRootFlow(until: Long, size: Int): Flow<Boolean>

    @Query(WOT_OR_TOPIC_EXISTS_CROSS_QUERY)
    fun internalHasWotOrTopicCrossFlow(until: Long, size: Int): Flow<Boolean>

    @Query(WOT_ONLY_EXISTS_ROOT_QUERY)
    fun internalHasWotRootFlow(until: Long, size: Int): Flow<Boolean>

    @Query(WOT_ONLY_EXISTS_CROSS_QUERY)
    fun internalHasWotCrossFlow(until: Long, size: Int): Flow<Boolean>

    @Query(GLOBAL_EXISTS_ROOT_QUERY)
    fun internalHasGlobalRootFlow(until: Long, size: Int): Flow<Boolean>

    @Query(GLOBAL_EXISTS_CROSS_QUERY)
    fun internalHasGlobalCrossFlow(until: Long, size: Int): Flow<Boolean>

    @Query(TOPIC_ONLY_CREATED_AT_ROOT_QUERY)
    suspend fun internalGetTopicCreatedAtRoot(until: Long, size: Int): List<Long>

    @Query(TOPIC_ONLY_CREATED_AT_CROSS_QUERY)
    suspend fun internalGetTopicCreatedAtCross(until: Long, size: Int): List<Long>

    @Query(FRIEND_OR_TOPIC_CREATED_AT_ROOT_QUERY)
    suspend fun internalGetFriendOrTopicCreatedAtRoot(until: Long, size: Int): List<Long>

    @Query(FRIEND_OR_TOPIC_CREATED_AT_CROSS_QUERY)
    suspend fun internalGetFriendOrTopicCreatedAtCross(until: Long, size: Int): List<Long>

    @Query(FRIEND_ONLY_CREATED_AT_ROOT_QUERY)
    suspend fun internalGetFriendCreatedAtRoot(until: Long, size: Int): List<Long>

    @Query(FRIEND_ONLY_CREATED_AT_CROSS_QUERY)
    suspend fun internalGetFriendCreatedAtCross(until: Long, size: Int): List<Long>

    @Query(WOT_OR_TOPIC_CREATED_AT_ROOT_QUERY)
    suspend fun internalGetWotOrTopicCreatedAtRoot(until: Long, size: Int): List<Long>

    @Query(WOT_OR_TOPIC_CREATED_AT_CROSS_QUERY)
    suspend fun internalGetWotOrTopicCreatedAtCross(until: Long, size: Int): List<Long>

    @Query(WOT_ONLY_CREATED_AT_ROOT_QUERY)
    suspend fun internalGetWotCreatedAtRoot(until: Long, size: Int): List<Long>

    @Query(WOT_ONLY_CREATED_AT_CROSS_QUERY)
    suspend fun internalGetWotCreatedAtCross(until: Long, size: Int): List<Long>

    @Query(GLOBAL_CREATED_AT_ROOT_QUERY)
    suspend fun internalGetGlobalCreatedAtRoot(until: Long, size: Int): List<Long>

    @Query(GLOBAL_CREATED_AT_CROSS_QUERY)
    suspend fun internalGetGlobalCreatedAtCross(until: Long, size: Int): List<Long>
}
