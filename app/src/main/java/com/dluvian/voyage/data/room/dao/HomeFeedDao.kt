package com.dluvian.voyage.data.room.dao

import androidx.room.Dao
import com.dluvian.voyage.data.model.FriendPubkeys
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
        "AND crossPostedAuthorIsMuted = 0 " +
        "AND NOT EXISTS (SELECT * FROM hashtag WHERE postId = id AND hashtag IN (SELECT mutedItem FROM mute WHERE tag IS 't')) " +
        "ORDER BY createdAt DESC " +
        "LIMIT :size"

private const val TOPIC_ONLY_COND = "AND myTopic IS NOT NULL "
private const val FRIEND_ONLY_COND = "AND authorIsFriend "
private const val WOT_ONLY_COND = "AND (authorIsTrusted OR authorIsFriend)"
private const val FRIEND_OR_TOPIC_COND = "AND (authorIsFriend OR myTopic IS NOT NULL) "
private const val WOT_OR_TOPIC_COND =
    "AND (authorIsTrusted OR authorIsFriend OR myTopic IS NOT NULL) "

private const val TOPIC_ONLY_MAIN_QUERY = "$BASE_QUERY $TOPIC_ONLY_COND $BASE_CONDITION"
private const val TOPIC_ONLY_QUERY = "SELECT * $TOPIC_ONLY_MAIN_QUERY"
private const val TOPIC_ONLY_CREATED_AT_QUERY = "SELECT createdAt $TOPIC_ONLY_MAIN_QUERY"
private const val TOPIC_ONLY_EXISTS_QUERY = "SELECT EXISTS($TOPIC_ONLY_MAIN_QUERY)"

private const val FRIEND_ONLY_MAIN_QUERY = "$BASE_QUERY $FRIEND_ONLY_COND $BASE_CONDITION"
private const val FRIEND_ONLY_QUERY = "SELECT * $FRIEND_ONLY_MAIN_QUERY"
private const val FRIEND_ONLY_CREATED_AT_QUERY = "SELECT createdAt $FRIEND_ONLY_MAIN_QUERY"
private const val FRIEND_ONLY_EXISTS_QUERY = "SELECT EXISTS($FRIEND_ONLY_MAIN_QUERY)"

private const val WOT_ONLY_MAIN_QUERY = "$BASE_QUERY $WOT_ONLY_COND $BASE_CONDITION"
private const val WOT_ONLY_QUERY = "SELECT * $WOT_ONLY_MAIN_QUERY"
private const val WOT_ONLY_CREATED_AT_QUERY = "SELECT createdAt $WOT_ONLY_MAIN_QUERY"
private const val WOT_ONLY_EXISTS_QUERY = "SELECT EXISTS($WOT_ONLY_MAIN_QUERY)"

private const val FRIEND_OR_TOPIC_MAIN_QUERY = "$BASE_QUERY $FRIEND_OR_TOPIC_COND $BASE_CONDITION"
private const val FRIEND_OR_TOPIC_QUERY = "SELECT * $FRIEND_OR_TOPIC_MAIN_QUERY"
private const val FRIEND_OR_TOPIC_CREATED_AT_QUERY = "SELECT createdAt $FRIEND_OR_TOPIC_MAIN_QUERY"
private const val FRIEND_OR_TOPIC_EXISTS_QUERY = "SELECT EXISTS($FRIEND_OR_TOPIC_MAIN_QUERY)"

private const val WOT_OR_TOPIC_MAIN_QUERY = "$BASE_QUERY $WOT_OR_TOPIC_COND $BASE_CONDITION"
private const val WOT_OR_TOPIC_QUERY = "SELECT * $WOT_OR_TOPIC_MAIN_QUERY"
private const val WOT_OR_TOPIC_CREATED_AT_QUERY = "SELECT createdAt $WOT_OR_TOPIC_MAIN_QUERY"
private const val WOT_OR_TOPIC_EXISTS_QUERY = "SELECT EXISTS($WOT_OR_TOPIC_MAIN_QUERY)"

private const val GLOBAL_MAIN_QUERY = "$BASE_QUERY $BASE_CONDITION"
private const val GLOBAL_QUERY = "SELECT * $GLOBAL_MAIN_QUERY"
private const val GLOBAL_CREATED_AT_QUERY = "SELECT createdAt $GLOBAL_MAIN_QUERY"
private const val GLOBAL_EXISTS_QUERY = "SELECT EXISTS($GLOBAL_MAIN_QUERY)"

@Dao
interface HomeFeedDao {

    fun getHomeRootPostFlow(
        setting: HomeFeedSetting,
        until: Long,
        size: Int
    ): Flow<List<RootPostView>> {
        val withMyTopics = setting.topicSelection.isMyTopics()

        return when (setting.pubkeySelection) {
            NoPubkeys -> if (withMyTopics) {
                internalGetTopicFlow()
            } else {
                flowOf(emptyList())
            }

            FriendPubkeys -> if (withMyTopics) {
                internalGetFriendOrTopicFlow()
            } else {
                internalGetFriendFlow()
            }

            WebOfTrustPubkeys -> if (withMyTopics) {
                internalGetWotOrTopicFlow()
            } else {
                internalGetWotFlow()
            }

            Global -> internalGetGlobalFlow()
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
                internalGetTopic()
            } else {
                emptyList()
            }

            FriendPubkeys -> if (withMyTopics) {
                internalGetFriendOrTopic()
            } else {
                internalGetFriend()
            }

            WebOfTrustPubkeys -> if (withMyTopics) {
                internalGetWotOrTopic()
            } else {
                internalGetWot()
            }

            Global -> internalGetGlobal()
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
                internalHasTopic()
            } else {
                flowOf(false)
            }

            FriendPubkeys -> if (withMyTopics) {
                internalHasFriendOrTopic()
            } else {
                internalHasFriend()
            }

            WebOfTrustPubkeys -> if (withMyTopics) {
                internalHasWotOrTopic()
            } else {
                internalHasWot()
            }

            Global -> internalHasGlobal()
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
                internalGetTopicCreatedAt()
            } else {
                emptyList()
            }

            FriendPubkeys -> if (withMyTopics) {
                internalGetFriendOrTopicCreatedAt()
            } else {
                internalGetFriendCreatedAt()
            }

            WebOfTrustPubkeys -> if (withMyTopics) {
                internalGetWotOrTopicCreatedAt()
            } else {
                internalGetWotCreatedAt()
            }

            Global -> internalGetGlobalCreatedAt()
        }
    }
}
