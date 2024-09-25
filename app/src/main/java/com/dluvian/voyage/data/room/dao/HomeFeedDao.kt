package com.dluvian.voyage.data.room.dao

import androidx.room.Dao
import androidx.room.Query
import com.dluvian.voyage.data.model.FriendPubkeysNoLock
import com.dluvian.voyage.data.model.Global
import com.dluvian.voyage.data.model.HomeFeedSetting
import com.dluvian.voyage.data.model.NoPubkeys
import com.dluvian.voyage.data.model.WebOfTrustPubkeys
import com.dluvian.voyage.data.room.view.RootPostView
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

private const val BASE_QUERY = "FROM RootPostView " +
        "WHERE createdAt <= :until "

private const val BASE_CONDITION = "AND authorIsOneself = 0 " +
        "AND authorIsMuted = 0 " +
        "AND authorIsLocked = 0 " +
        "AND NOT EXISTS (SELECT * FROM hashtag WHERE eventId = id AND hashtag IN (SELECT mutedItem FROM mute WHERE tag IS 't')) " +
        "ORDER BY createdAt DESC " +
        "LIMIT :size"

private const val TOPIC_ONLY_COND = "AND myTopic IS NOT NULL "
private const val FRIEND_ONLY_COND = "AND authorIsFriend "
private const val WOT_ONLY_COND = "AND (authorIsTrusted OR authorIsFriend) "
private const val FRIEND_OR_TOPIC_COND = "AND (authorIsFriend OR myTopic IS NOT NULL) "
private const val WOT_OR_TOPIC_COND =
    "AND (authorIsTrusted OR authorIsFriend OR myTopic IS NOT NULL) "

private const val TOPIC_ONLY_MAIN_QUERY = "$BASE_QUERY $TOPIC_ONLY_COND $BASE_CONDITION"
private const val TOPIC_ONLY_QUERY = "SELECT * $TOPIC_ONLY_MAIN_QUERY"
private const val TOPIC_ONLY_CREATED_AT_QUERY = "SELECT createdAt $TOPIC_ONLY_MAIN_QUERY"
private const val TOPIC_ONLY_EXISTS_QUERY = "SELECT EXISTS($TOPIC_ONLY_QUERY)"

private const val FRIEND_ONLY_MAIN_QUERY = "$BASE_QUERY $FRIEND_ONLY_COND $BASE_CONDITION"
private const val FRIEND_ONLY_QUERY = "SELECT * $FRIEND_ONLY_MAIN_QUERY"
private const val FRIEND_ONLY_CREATED_AT_QUERY = "SELECT createdAt $FRIEND_ONLY_MAIN_QUERY"
private const val FRIEND_ONLY_EXISTS_QUERY = "SELECT EXISTS($FRIEND_ONLY_QUERY)"

private const val WOT_ONLY_MAIN_QUERY = "$BASE_QUERY $WOT_ONLY_COND $BASE_CONDITION"
private const val WOT_ONLY_QUERY = "SELECT * $WOT_ONLY_MAIN_QUERY"
private const val WOT_ONLY_CREATED_AT_QUERY = "SELECT createdAt $WOT_ONLY_MAIN_QUERY"
private const val WOT_ONLY_EXISTS_QUERY = "SELECT EXISTS($WOT_ONLY_QUERY)"

private const val FRIEND_OR_TOPIC_MAIN_QUERY = "$BASE_QUERY $FRIEND_OR_TOPIC_COND $BASE_CONDITION"
private const val FRIEND_OR_TOPIC_QUERY = "SELECT * $FRIEND_OR_TOPIC_MAIN_QUERY"
private const val FRIEND_OR_TOPIC_CREATED_AT_QUERY = "SELECT createdAt $FRIEND_OR_TOPIC_MAIN_QUERY"
private const val FRIEND_OR_TOPIC_EXISTS_QUERY = "SELECT EXISTS($FRIEND_OR_TOPIC_QUERY)"

private const val WOT_OR_TOPIC_MAIN_QUERY = "$BASE_QUERY $WOT_OR_TOPIC_COND $BASE_CONDITION"
private const val WOT_OR_TOPIC_QUERY = "SELECT * $WOT_OR_TOPIC_MAIN_QUERY"
private const val WOT_OR_TOPIC_CREATED_AT_QUERY = "SELECT createdAt $WOT_OR_TOPIC_MAIN_QUERY"
private const val WOT_OR_TOPIC_EXISTS_QUERY = "SELECT EXISTS($WOT_OR_TOPIC_QUERY)"

private const val GLOBAL_MAIN_QUERY = "$BASE_QUERY $BASE_CONDITION"
private const val GLOBAL_QUERY = "SELECT * $GLOBAL_MAIN_QUERY"
private const val GLOBAL_CREATED_AT_QUERY = "SELECT createdAt $GLOBAL_MAIN_QUERY"
private const val GLOBAL_EXISTS_QUERY = "SELECT EXISTS($GLOBAL_QUERY)"

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
                internalGetTopicFlow(until = until, size = size)
            } else {
                flowOf(emptyList())
            }

            FriendPubkeysNoLock -> if (withMyTopics) {
                internalGetFriendOrTopicFlow(until = until, size = size)
            } else {
                internalGetFriendFlow(until = until, size = size)
            }

            WebOfTrustPubkeys -> if (withMyTopics) {
                internalGetWotOrTopicFlow(until = until, size = size)
            } else {
                internalGetWotFlow(until = until, size = size)
            }

            Global -> internalGetGlobalFlow(until = until, size = size)
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
                internalGetTopic(until = until, size = size)
            } else {
                emptyList()
            }

            FriendPubkeysNoLock -> if (withMyTopics) {
                internalGetFriendOrTopic(until = until, size = size)
            } else {
                internalGetFriend(until = until, size = size)
            }

            WebOfTrustPubkeys -> if (withMyTopics) {
                internalGetWotOrTopic(until = until, size = size)
            } else {
                internalGetWot(until = until, size = size)
            }

            Global -> internalGetGlobal(until = until, size = size)
        }
    }

    fun hasHomeRootPostsFlow(
        setting: HomeFeedSetting,
        until: Long = Long.MAX_VALUE,
        size: Int = 1
    ): Flow<Boolean> {
        val withMyTopics = setting.topicSelection.isMyTopics()

        return when (setting.pubkeySelection) {
            NoPubkeys -> if (withMyTopics) {
                internalHasTopicFlow(until = until, size = size)
            } else {
                flowOf(false)
            }

            FriendPubkeysNoLock -> if (withMyTopics) {
                internalHasFriendOrTopicFlow(until = until, size = size)
            } else {
                internalHasFriendFlow(until = until, size = size)
            }

            WebOfTrustPubkeys -> if (withMyTopics) {
                internalHasWotOrTopicFlow(until = until, size = size)
            } else {
                internalHasWotFlow(until = until, size = size)
            }

            Global -> internalHasGlobalFlow(until = until, size = size)
        }
    }

    suspend fun getHomeRootPostsCreatedAt(
        setting: HomeFeedSetting,
        until: Long,
        size: Int
    ): List<Long> {
        val withMyTopics = setting.topicSelection.isMyTopics()

        return when (setting.pubkeySelection) {
            NoPubkeys -> if (withMyTopics) {
                internalGetTopicCreatedAt(until = until, size = size)
            } else {
                emptyList()
            }

            FriendPubkeysNoLock -> if (withMyTopics) {
                internalGetFriendOrTopicCreatedAt(until = until, size = size)
            } else {
                internalGetFriendCreatedAt(until = until, size = size)
            }

            WebOfTrustPubkeys -> if (withMyTopics) {
                internalGetWotOrTopicCreatedAt(until = until, size = size)
            } else {
                internalGetWotCreatedAt(until = until, size = size)
            }

            Global -> internalGetGlobalCreatedAt(until = until, size = size)
        }
    }

    @Query(TOPIC_ONLY_QUERY)
    fun internalGetTopicFlow(until: Long, size: Int): Flow<List<RootPostView>>

    @Query(FRIEND_OR_TOPIC_QUERY)
    fun internalGetFriendOrTopicFlow(until: Long, size: Int): Flow<List<RootPostView>>

    @Query(FRIEND_ONLY_QUERY)
    fun internalGetFriendFlow(until: Long, size: Int): Flow<List<RootPostView>>

    @Query(WOT_OR_TOPIC_QUERY)
    fun internalGetWotOrTopicFlow(until: Long, size: Int): Flow<List<RootPostView>>

    @Query(WOT_ONLY_QUERY)
    fun internalGetWotFlow(until: Long, size: Int): Flow<List<RootPostView>>

    @Query(GLOBAL_QUERY)
    fun internalGetGlobalFlow(until: Long, size: Int): Flow<List<RootPostView>>

    @Query(TOPIC_ONLY_QUERY)
    suspend fun internalGetTopic(until: Long, size: Int): List<RootPostView>

    @Query(FRIEND_OR_TOPIC_QUERY)
    suspend fun internalGetFriendOrTopic(until: Long, size: Int): List<RootPostView>

    @Query(FRIEND_ONLY_QUERY)
    suspend fun internalGetFriend(until: Long, size: Int): List<RootPostView>

    @Query(WOT_OR_TOPIC_QUERY)
    suspend fun internalGetWotOrTopic(until: Long, size: Int): List<RootPostView>

    @Query(WOT_ONLY_QUERY)
    suspend fun internalGetWot(until: Long, size: Int): List<RootPostView>

    @Query(GLOBAL_QUERY)
    suspend fun internalGetGlobal(until: Long, size: Int): List<RootPostView>

    @Query(TOPIC_ONLY_EXISTS_QUERY)
    fun internalHasTopicFlow(until: Long, size: Int): Flow<Boolean>

    @Query(FRIEND_OR_TOPIC_EXISTS_QUERY)
    fun internalHasFriendOrTopicFlow(until: Long, size: Int): Flow<Boolean>

    @Query(FRIEND_ONLY_EXISTS_QUERY)
    fun internalHasFriendFlow(until: Long, size: Int): Flow<Boolean>

    @Query(WOT_OR_TOPIC_EXISTS_QUERY)
    fun internalHasWotOrTopicFlow(until: Long, size: Int): Flow<Boolean>

    @Query(WOT_ONLY_EXISTS_QUERY)
    fun internalHasWotFlow(until: Long, size: Int): Flow<Boolean>

    @Query(GLOBAL_EXISTS_QUERY)
    fun internalHasGlobalFlow(until: Long, size: Int): Flow<Boolean>

    @Query(TOPIC_ONLY_CREATED_AT_QUERY)
    suspend fun internalGetTopicCreatedAt(until: Long, size: Int): List<Long>

    @Query(FRIEND_OR_TOPIC_CREATED_AT_QUERY)
    suspend fun internalGetFriendOrTopicCreatedAt(until: Long, size: Int): List<Long>

    @Query(FRIEND_ONLY_CREATED_AT_QUERY)
    suspend fun internalGetFriendCreatedAt(until: Long, size: Int): List<Long>

    @Query(WOT_OR_TOPIC_CREATED_AT_QUERY)
    suspend fun internalGetWotOrTopicCreatedAt(until: Long, size: Int): List<Long>

    @Query(WOT_ONLY_CREATED_AT_QUERY)
    suspend fun internalGetWotCreatedAt(until: Long, size: Int): List<Long>

    @Query(GLOBAL_CREATED_AT_QUERY)
    suspend fun internalGetGlobalCreatedAt(until: Long, size: Int): List<Long>
}
