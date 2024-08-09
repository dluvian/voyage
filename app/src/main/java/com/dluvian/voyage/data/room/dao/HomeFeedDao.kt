package com.dluvian.voyage.data.room.dao

import android.util.Log
import androidx.room.Dao
import com.dluvian.voyage.data.model.CustomPubkeys
import com.dluvian.voyage.data.model.FriendPubkeys
import com.dluvian.voyage.data.model.Global
import com.dluvian.voyage.data.model.HomeFeedSetting
import com.dluvian.voyage.data.model.ListPubkeys
import com.dluvian.voyage.data.model.NoPubkeys
import com.dluvian.voyage.data.model.SingularPubkey
import com.dluvian.voyage.data.model.WebOfTrustPubkeys
import com.dluvian.voyage.data.room.view.RootPostView
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

private const val TAG = "HomeFeedDao"

private const val HOME_FEED_BASE_QUERY = "FROM RootPostView " +
        "WHERE createdAt <= :until " +
        "AND (authorIsFriend OR myTopic IS NOT NULL) " +
        "AND authorIsOneself = 0 " +
        "AND authorIsMuted = 0 " +
        "AND crossPostedAuthorIsMuted = 0 " +
        "AND NOT EXISTS (SELECT * FROM hashtag WHERE postId = id AND hashtag IN (SELECT mutedItem FROM mute WHERE tag IS 't')) " +
        "ORDER BY createdAt DESC " +
        "LIMIT :size"

private const val HOME_FEED_QUERY = "SELECT * $HOME_FEED_BASE_QUERY"
private const val HOME_FEED_CREATED_AT_QUERY = "SELECT createdAt $HOME_FEED_BASE_QUERY"
private const val HOME_FEED_EXISTS_QUERY = "SELECT EXISTS($HOME_FEED_QUERY)"

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
                internalGetFriendAndTopicFlow()
            } else {
                internalGetFriendFlow()
            }

            WebOfTrustPubkeys -> if (withMyTopics) {
                internalGetWotAndTopicFlow()
            } else {
                internalGetWotFlow()
            }

            Global -> if (withMyTopics) {
                internalGetGlobalAndTopicFlow()
            } else {
                internalGetGlobalFlow()
            }

            is CustomPubkeys, is ListPubkeys, is SingularPubkey -> {
                Log.w(TAG, "Selection ${setting.pubkeySelection} is not supported")
                flowOf(emptyList())
            }
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
                internalGetFriendAndTopic()
            } else {
                internalGetFriend()
            }

            WebOfTrustPubkeys -> if (withMyTopics) {
                internalGetWotAndTopic()
            } else {
                internalGetWot()
            }

            Global -> if (withMyTopics) {
                internalGetGlobalAndTopic()
            } else {
                internalGetGlobal()
            }

            is CustomPubkeys, is ListPubkeys, is SingularPubkey -> {
                Log.w(TAG, "Selection ${setting.pubkeySelection} is not supported")
                emptyList()
            }
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

            Global -> if (withMyTopics) {
                internalHasGlobalOrTopic()
            } else {
                internalHasGlobal()
            }

            is CustomPubkeys, is ListPubkeys, is SingularPubkey -> {
                Log.w(TAG, "Selection ${setting.pubkeySelection} is not supported")
                flowOf(false)
            }
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
                internalGetFriendAndTopicCreatedAt()
            } else {
                internalGetFriendCreatedAt()
            }

            WebOfTrustPubkeys -> if (withMyTopics) {
                internalGetWotAndTopicCreatedAt()
            } else {
                internalGetWotCreatedAt()
            }

            Global -> if (withMyTopics) {
                internalGetGlobalAndTopicCreatedAt()
            } else {
                internalGetGlobalCreatedAt()
            }

            is CustomPubkeys, is ListPubkeys, is SingularPubkey -> {
                Log.w(TAG, "Selection ${setting.pubkeySelection} is not supported")
                emptyList()
            }
        }
    }
}
