package com.dluvian.voyage.data.room.dao

import androidx.room.Dao
import androidx.room.Query
import com.dluvian.voyage.data.model.FriendPubkeys
import com.dluvian.voyage.data.model.Global
import com.dluvian.voyage.data.model.HomeFeedSetting
import com.dluvian.voyage.data.model.NoPubkeys
import com.dluvian.voyage.data.model.WebOfTrustPubkeys
import com.dluvian.voyage.data.room.view.CrossPostView
import com.dluvian.voyage.data.room.view.PollOptionView
import com.dluvian.voyage.data.room.view.PollView
import com.dluvian.voyage.data.room.view.RootPostView
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf

private const val CREATED_AT = "WHERE createdAt <= :until "
private const val ROOT = "FROM RootPostView $CREATED_AT"
private const val CROSS = "FROM CrossPostView $CREATED_AT"
private const val POLL = "FROM PollView $CREATED_AT"
private const val POLL_OPTION = "FROM PollOptionView "

private const val ROOT_COND = "AND authorIsOneself = 0 " +
        "AND authorIsMuted = 0 " +
        "AND NOT EXISTS (SELECT * FROM hashtag WHERE eventId = id AND hashtag IN (SELECT mutedItem FROM mute WHERE tag IS 't')) " +
        "ORDER BY createdAt DESC " +
        "LIMIT :size"

private const val CROSS_COND = "AND crossPostedAuthorIsOneself = 0 " +
        "AND crossPostedAuthorIsMuted = 0 " +
        ROOT_COND

private const val POLL_COND = ROOT_COND

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

private const val TOPIC_ONLY_MAIN_POLL = "$POLL $TOPIC_ONLY_COND $POLL_COND"
private const val TOPIC_ONLY_POLL_QUERY = "SELECT * $TOPIC_ONLY_MAIN_POLL"
private const val TOPIC_ONLY_CREATED_AT_POLL_QUERY = "SELECT createdAt $TOPIC_ONLY_MAIN_POLL"
private const val TOPIC_ONLY_EXISTS_POLL_QUERY = "SELECT EXISTS($TOPIC_ONLY_POLL_QUERY)"

private const val TOPIC_ONLY_MAIN_POLL_OPTION =
    "$POLL_OPTION WHERE pollId IN (SELECT id $TOPIC_ONLY_MAIN_POLL)"
private const val TOPIC_ONLY_POLL_OPTION_QUERY = "SELECT * $TOPIC_ONLY_MAIN_POLL_OPTION"

private const val FRIEND_ONLY_MAIN_ROOT = "$ROOT $FRIEND_ONLY_COND $ROOT_COND"
private const val FRIEND_ONLY_ROOT_QUERY = "SELECT * $FRIEND_ONLY_MAIN_ROOT"
private const val FRIEND_ONLY_CREATED_AT_ROOT_QUERY = "SELECT createdAt $FRIEND_ONLY_MAIN_ROOT"
private const val FRIEND_ONLY_EXISTS_ROOT_QUERY = "SELECT EXISTS($FRIEND_ONLY_ROOT_QUERY)"

private const val FRIEND_ONLY_MAIN_CROSS = "$CROSS $FRIEND_ONLY_COND $CROSS_COND"
private const val FRIEND_ONLY_CROSS_QUERY = "SELECT * $FRIEND_ONLY_MAIN_CROSS"
private const val FRIEND_ONLY_CREATED_AT_CROSS_QUERY = "SELECT createdAt $FRIEND_ONLY_MAIN_CROSS"
private const val FRIEND_ONLY_EXISTS_CROSS_QUERY = "SELECT EXISTS($FRIEND_ONLY_CROSS_QUERY)"

private const val FRIEND_ONLY_MAIN_POLL = "$POLL $FRIEND_ONLY_COND $POLL_COND"
private const val FRIEND_ONLY_POLL_QUERY = "SELECT * $FRIEND_ONLY_MAIN_POLL"
private const val FRIEND_ONLY_CREATED_AT_POLL_QUERY = "SELECT createdAt $FRIEND_ONLY_MAIN_POLL"
private const val FRIEND_ONLY_EXISTS_POLL_QUERY = "SELECT EXISTS($FRIEND_ONLY_POLL_QUERY)"

private const val FRIEND_ONLY_MAIN_POLL_OPTION =
    "$POLL_OPTION WHERE pollId IN (SELECT id $FRIEND_ONLY_MAIN_POLL)"
private const val FRIEND_ONLY_POLL_OPTION_QUERY = "SELECT * $FRIEND_ONLY_MAIN_POLL_OPTION"

private const val WOT_ONLY_MAIN_ROOT = "$ROOT $WOT_ONLY_COND $ROOT_COND"
private const val WOT_ONLY_ROOT_QUERY = "SELECT * $WOT_ONLY_MAIN_ROOT"
private const val WOT_ONLY_CREATED_AT_ROOT_QUERY = "SELECT createdAt $WOT_ONLY_MAIN_ROOT"
private const val WOT_ONLY_EXISTS_ROOT_QUERY = "SELECT EXISTS($WOT_ONLY_ROOT_QUERY)"

private const val WOT_ONLY_MAIN_CROSS = "$CROSS $WOT_ONLY_COND $CROSS_COND"
private const val WOT_ONLY_CROSS_QUERY = "SELECT * $WOT_ONLY_MAIN_CROSS"
private const val WOT_ONLY_CREATED_AT_CROSS_QUERY = "SELECT createdAt $WOT_ONLY_MAIN_CROSS"
private const val WOT_ONLY_EXISTS_CROSS_QUERY = "SELECT EXISTS($WOT_ONLY_CROSS_QUERY)"

private const val WOT_ONLY_MAIN_POLL = "$POLL $WOT_ONLY_COND $POLL_COND"
private const val WOT_ONLY_POLL_QUERY = "SELECT * $WOT_ONLY_MAIN_POLL"
private const val WOT_ONLY_CREATED_AT_POLL_QUERY = "SELECT createdAt $WOT_ONLY_MAIN_POLL"
private const val WOT_ONLY_EXISTS_POLL_QUERY = "SELECT EXISTS($WOT_ONLY_POLL_QUERY)"

private const val WOT_ONLY_MAIN_POLL_OPTION =
    "$POLL_OPTION WHERE pollId IN (SELECT id $WOT_ONLY_MAIN_POLL)"
private const val WOT_ONLY_POLL_OPTION_QUERY = "SELECT * $WOT_ONLY_MAIN_POLL_OPTION"

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

private const val FRIEND_OR_TOPIC_MAIN_POLL = "$POLL $FRIEND_OR_TOPIC_COND $POLL_COND"
private const val FRIEND_OR_TOPIC_POLL_QUERY = "SELECT * $FRIEND_OR_TOPIC_MAIN_POLL"
private const val FRIEND_OR_TOPIC_CREATED_AT_POLL_QUERY =
    "SELECT createdAt $FRIEND_OR_TOPIC_MAIN_POLL"
private const val FRIEND_OR_TOPIC_EXISTS_POLL_QUERY = "SELECT EXISTS($FRIEND_OR_TOPIC_POLL_QUERY)"

private const val FRIEND_OR_TOPIC_MAIN_POLL_OPTION =
    "$POLL_OPTION WHERE pollId IN (SELECT id $FRIEND_OR_TOPIC_MAIN_POLL)"
private const val FRIEND_OR_TOPIC_POLL_OPTION_QUERY = "SELECT * $FRIEND_OR_TOPIC_MAIN_POLL_OPTION"

private const val WOT_OR_TOPIC_MAIN_ROOT = "$ROOT $WOT_OR_TOPIC_COND $ROOT_COND"
private const val WOT_OR_TOPIC_ROOT_QUERY = "SELECT * $WOT_OR_TOPIC_MAIN_ROOT"
private const val WOT_OR_TOPIC_CREATED_AT_ROOT_QUERY = "SELECT createdAt $WOT_OR_TOPIC_MAIN_ROOT"
private const val WOT_OR_TOPIC_EXISTS_ROOT_QUERY = "SELECT EXISTS($WOT_OR_TOPIC_ROOT_QUERY)"

private const val WOT_OR_TOPIC_MAIN_CROSS = "$CROSS $WOT_OR_TOPIC_COND $CROSS_COND"
private const val WOT_OR_TOPIC_CROSS_QUERY = "SELECT * $WOT_OR_TOPIC_MAIN_CROSS"
private const val WOT_OR_TOPIC_CREATED_AT_CROSS_QUERY = "SELECT createdAt $WOT_OR_TOPIC_MAIN_CROSS"
private const val WOT_OR_TOPIC_EXISTS_CROSS_QUERY = "SELECT EXISTS($WOT_OR_TOPIC_CROSS_QUERY)"

private const val WOT_OR_TOPIC_MAIN_POLL = "$POLL $WOT_OR_TOPIC_COND $POLL_COND"
private const val WOT_OR_TOPIC_POLL_QUERY = "SELECT * $WOT_OR_TOPIC_MAIN_POLL"
private const val WOT_OR_TOPIC_CREATED_AT_POLL_QUERY = "SELECT createdAt $WOT_OR_TOPIC_MAIN_POLL"
private const val WOT_OR_TOPIC_EXISTS_POLL_QUERY = "SELECT EXISTS($WOT_OR_TOPIC_POLL_QUERY)"

private const val WOT_OR_TOPIC_MAIN_POLL_OPTION =
    "$POLL_OPTION WHERE pollId IN (SELECT id $FRIEND_OR_TOPIC_MAIN_POLL)"
private const val WOT_OR_TOPIC_POLL_OPTION_QUERY = "SELECT * $WOT_OR_TOPIC_MAIN_POLL_OPTION"

private const val GLOBAL_MAIN_ROOT = "$ROOT $ROOT_COND"
private const val GLOBAL_ROOT_QUERY = "SELECT * $GLOBAL_MAIN_ROOT"
private const val GLOBAL_CREATED_AT_ROOT_QUERY = "SELECT createdAt $GLOBAL_MAIN_ROOT"
private const val GLOBAL_EXISTS_ROOT_QUERY = "SELECT EXISTS($GLOBAL_ROOT_QUERY)"

private const val GLOBAL_MAIN_CROSS = "$CROSS $CROSS_COND"
private const val GLOBAL_CROSS_QUERY = "SELECT * $GLOBAL_MAIN_CROSS"
private const val GLOBAL_CREATED_AT_CROSS_QUERY = "SELECT createdAt $GLOBAL_MAIN_CROSS"
private const val GLOBAL_EXISTS_CROSS_QUERY = "SELECT EXISTS($GLOBAL_CROSS_QUERY)"

private const val GLOBAL_MAIN_POLL = "$POLL $POLL_COND"
private const val GLOBAL_POLL_QUERY = "SELECT * $GLOBAL_MAIN_POLL"
private const val GLOBAL_CREATED_AT_POLL_QUERY = "SELECT createdAt $GLOBAL_MAIN_POLL"
private const val GLOBAL_EXISTS_POLL_QUERY = "SELECT EXISTS($GLOBAL_POLL_QUERY)"

private const val GLOBAL_MAIN_POLL_OPTION =
    "$POLL_OPTION WHERE pollId IN (SELECT id $GLOBAL_MAIN_POLL)"
private const val GLOBAL_POLL_OPTION_QUERY = "SELECT * $GLOBAL_MAIN_POLL_OPTION"

@Dao
interface HomeFeedDao {

    fun getHomeRootPostFlow(
        setting: HomeFeedSetting,
        until: Long,
        size: Int,
    ): Flow<List<RootPostView>> {
        if (!setting.showRoots) return flowOf(emptyList())

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
        if (!setting.showCrossPosts) return flowOf(emptyList())

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

    fun getHomePollFlow(
        setting: HomeFeedSetting,
        until: Long,
        size: Int,
    ): Flow<List<PollView>> {
        if (!setting.showPolls) return flowOf(emptyList())

        val withMyTopics = setting.topicSelection.isMyTopics()

        return when (setting.pubkeySelection) {
            NoPubkeys -> if (withMyTopics) {
                internalGetTopicPollFlow(until = until, size = size)
            } else {
                flowOf(emptyList())
            }

            FriendPubkeys -> if (withMyTopics) {
                internalGetFriendOrTopicPollFlow(until = until, size = size)
            } else {
                internalGetFriendPollFlow(until = until, size = size)
            }

            WebOfTrustPubkeys -> if (withMyTopics) {
                internalGetWotOrTopicPollFlow(until = until, size = size)
            } else {
                internalGetWotPollFlow(until = until, size = size)
            }

            Global -> internalGetGlobalPollFlow(until = until, size = size)
        }
    }

    fun getHomePollOptionFlow(
        setting: HomeFeedSetting,
        until: Long,
        size: Int,
    ): Flow<List<PollOptionView>> {
        if (!setting.showPolls) return flowOf(emptyList())

        val withMyTopics = setting.topicSelection.isMyTopics()

        return when (setting.pubkeySelection) {
            NoPubkeys -> if (withMyTopics) {
                internalGetTopicPollOptionFlow(until = until, size = size)
            } else {
                flowOf(emptyList())
            }

            FriendPubkeys -> if (withMyTopics) {
                internalGetFriendOrTopicPollOptionFlow(until = until, size = size)
            } else {
                internalGetFriendPollOptionFlow(until = until, size = size)
            }

            WebOfTrustPubkeys -> if (withMyTopics) {
                internalGetWotOrTopicPollOptionFlow(until = until, size = size)
            } else {
                internalGetWotPollOptionFlow(until = until, size = size)
            }

            Global -> internalGetGlobalPollOptionFlow(until = until, size = size)
        }
    }

    suspend fun getHomeRootPosts(
        setting: HomeFeedSetting,
        until: Long,
        size: Int
    ): List<RootPostView> {
        if (!setting.showRoots) return emptyList()

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
        if (!setting.showCrossPosts) return emptyList()

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

    suspend fun getHomePolls(
        setting: HomeFeedSetting,
        until: Long,
        size: Int
    ): List<PollView> {
        if (!setting.showPolls) return emptyList()

        val withMyTopics = setting.topicSelection.isMyTopics()

        return when (setting.pubkeySelection) {
            NoPubkeys -> if (withMyTopics) {
                internalGetTopicPoll(until = until, size = size)
            } else {
                emptyList()
            }

            FriendPubkeys -> if (withMyTopics) {
                internalGetFriendOrTopicPoll(until = until, size = size)
            } else {
                internalGetFriendPoll(until = until, size = size)
            }

            WebOfTrustPubkeys -> if (withMyTopics) {
                internalGetWotOrTopicPoll(until = until, size = size)
            } else {
                internalGetWotPoll(until = until, size = size)
            }

            Global -> internalGetGlobalPoll(until = until, size = size)
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
                    if (setting.showRoots) internalHasTopicRootFlow(until = until, size = size)
                    else flowOf(false),
                    if (setting.showCrossPosts) internalHasTopicCrossFlow(
                        until = until,
                        size = size
                    ) else flowOf(false),
                    if (setting.showPolls) internalHasTopicPollFlow(until = until, size = size)
                    else flowOf(false),
                ) { root, cross, poll -> root || cross || poll }
            } else {
                flowOf(false)
            }

            FriendPubkeys -> if (withMyTopics) {
                combine(
                    if (setting.showRoots) internalHasFriendOrTopicRootFlow(
                        until = until,
                        size = size
                    ) else flowOf(false),
                    if (setting.showCrossPosts) internalHasFriendOrTopicCrossFlow(
                        until = until,
                        size = size
                    ) else flowOf(false),
                    if (setting.showPolls) internalHasFriendOrTopicPollFlow(
                        until = until,
                        size = size
                    ) else flowOf(false),
                ) { root, cross, poll -> root || cross || poll }
            } else {
                combine(
                    if (setting.showRoots) internalHasFriendRootFlow(until = until, size = size)
                    else flowOf(false),
                    if (setting.showCrossPosts) internalHasFriendCrossFlow(
                        until = until,
                        size = size
                    ) else flowOf(false),
                    if (setting.showPolls) internalHasFriendPollFlow(until = until, size = size)
                    else flowOf(false),
                ) { root, cross, poll -> root || cross || poll }
            }

            WebOfTrustPubkeys -> if (withMyTopics) {
                combine(
                    if (setting.showRoots) internalHasWotOrTopicRootFlow(until = until, size = size)
                    else flowOf(false),
                    if (setting.showCrossPosts) internalHasWotOrTopicCrossFlow(
                        until = until,
                        size = size
                    ) else flowOf(false),
                    if (setting.showPolls) internalHasWotOrTopicPollFlow(until = until, size = size)
                    else flowOf(false),
                ) { root, cross, poll -> root || cross || poll }
            } else {
                combine(
                    if (setting.showRoots) internalHasWotRootFlow(until = until, size = size)
                    else flowOf(false),
                    if (setting.showCrossPosts) internalHasWotCrossFlow(until = until, size = size)
                    else flowOf(false),
                    if (setting.showPolls) internalHasWotPollFlow(until = until, size = size)
                    else flowOf(false),
                ) { root, cross, poll -> root || cross || poll }
            }

            Global -> {
                combine(
                    if (setting.showRoots) internalHasGlobalRootFlow(until = until, size = size)
                    else flowOf(false),
                    if (setting.showCrossPosts) internalHasGlobalCrossFlow(
                        until = until,
                        size = size
                    ) else flowOf(false),
                    if (setting.showPolls) internalHasGlobalPollFlow(until = until, size = size)
                    else flowOf(false),
                ) { root, cross, poll -> root || cross || poll }
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
                internalGetTopicCreatedAtRoot(until = until, size = size)
                    .orNoRoots(setting) +
                        internalGetTopicCreatedAtCross(until = until, size = size)
                            .orNoCross(setting) +
                        internalGetTopicCreatedAtPoll(until = until, size = size)
                            .orNoPolls(setting)
            } else {
                emptyList()
            }

            FriendPubkeys -> if (withMyTopics) {
                internalGetFriendOrTopicCreatedAtRoot(until = until, size = size)
                    .orNoRoots(setting) +
                        internalGetFriendOrTopicCreatedAtCross(until = until, size = size)
                            .orNoCross(setting) +
                        internalGetFriendOrTopicCreatedAtPoll(until = until, size = size)
                            .orNoPolls(setting)
            } else {
                internalGetFriendCreatedAtRoot(until = until, size = size)
                    .orNoRoots(setting) +
                        internalGetFriendCreatedAtCross(until = until, size = size)
                            .orNoCross(setting) +
                        internalGetFriendCreatedAtPoll(until = until, size = size)
                            .orNoPolls(setting)
            }

            WebOfTrustPubkeys -> if (withMyTopics) {
                internalGetWotOrTopicCreatedAtRoot(until = until, size = size)
                    .orNoRoots(setting) +
                        internalGetWotOrTopicCreatedAtCross(until = until, size = size)
                            .orNoCross(setting) +
                        internalGetWotOrTopicCreatedAtPoll(until = until, size = size)
                            .orNoPolls(setting)
            } else {
                internalGetWotCreatedAtRoot(until = until, size = size)
                    .orNoRoots(setting) +
                        internalGetWotCreatedAtCross(until = until, size = size)
                            .orNoCross(setting) +
                        internalGetWotCreatedAtPoll(until = until, size = size)
                            .orNoPolls(setting)
            }

            Global -> internalGetGlobalCreatedAtRoot(until = until, size = size)
                .orNoRoots(setting) +
                    internalGetGlobalCreatedAtCross(until = until, size = size)
                        .orNoCross(setting) +
                    internalGetGlobalCreatedAtPoll(until = until, size = size)
                        .orNoPolls(setting)
        }
            .sortedDescending()
            .take(size)
    }

    @Query(TOPIC_ONLY_ROOT_QUERY)
    fun internalGetTopicRootFlow(until: Long, size: Int): Flow<List<RootPostView>>

    @Query(TOPIC_ONLY_CROSS_QUERY)
    fun internalGetTopicCrossFlow(until: Long, size: Int): Flow<List<CrossPostView>>

    @Query(TOPIC_ONLY_POLL_QUERY)
    fun internalGetTopicPollFlow(until: Long, size: Int): Flow<List<PollView>>

    @Query(TOPIC_ONLY_POLL_OPTION_QUERY)
    fun internalGetTopicPollOptionFlow(until: Long, size: Int): Flow<List<PollOptionView>>

    @Query(FRIEND_OR_TOPIC_ROOT_QUERY)
    fun internalGetFriendOrTopicRootFlow(until: Long, size: Int): Flow<List<RootPostView>>

    @Query(FRIEND_OR_TOPIC_CROSS_QUERY)
    fun internalGetFriendOrTopicCrossFlow(until: Long, size: Int): Flow<List<CrossPostView>>

    @Query(FRIEND_OR_TOPIC_POLL_QUERY)
    fun internalGetFriendOrTopicPollFlow(until: Long, size: Int): Flow<List<PollView>>

    @Query(FRIEND_OR_TOPIC_POLL_OPTION_QUERY)
    fun internalGetFriendOrTopicPollOptionFlow(until: Long, size: Int): Flow<List<PollOptionView>>

    @Query(FRIEND_ONLY_ROOT_QUERY)
    fun internalGetFriendRootFlow(until: Long, size: Int): Flow<List<RootPostView>>

    @Query(FRIEND_ONLY_CROSS_QUERY)
    fun internalGetFriendCrossFlow(until: Long, size: Int): Flow<List<CrossPostView>>

    @Query(FRIEND_ONLY_POLL_QUERY)
    fun internalGetFriendPollFlow(until: Long, size: Int): Flow<List<PollView>>

    @Query(FRIEND_ONLY_POLL_OPTION_QUERY)
    fun internalGetFriendPollOptionFlow(until: Long, size: Int): Flow<List<PollOptionView>>

    @Query(WOT_OR_TOPIC_ROOT_QUERY)
    fun internalGetWotOrTopicRootFlow(until: Long, size: Int): Flow<List<RootPostView>>

    @Query(WOT_OR_TOPIC_CROSS_QUERY)
    fun internalGetWotOrTopicCrossFlow(until: Long, size: Int): Flow<List<CrossPostView>>

    @Query(WOT_OR_TOPIC_POLL_QUERY)
    fun internalGetWotOrTopicPollFlow(until: Long, size: Int): Flow<List<PollView>>

    @Query(WOT_OR_TOPIC_POLL_OPTION_QUERY)
    fun internalGetWotOrTopicPollOptionFlow(until: Long, size: Int): Flow<List<PollOptionView>>

    @Query(WOT_ONLY_ROOT_QUERY)
    fun internalGetWotRootFlow(until: Long, size: Int): Flow<List<RootPostView>>

    @Query(WOT_ONLY_CROSS_QUERY)
    fun internalGetWotCrossFlow(until: Long, size: Int): Flow<List<CrossPostView>>

    @Query(WOT_ONLY_POLL_QUERY)
    fun internalGetWotPollFlow(until: Long, size: Int): Flow<List<PollView>>

    @Query(WOT_ONLY_POLL_OPTION_QUERY)
    fun internalGetWotPollOptionFlow(until: Long, size: Int): Flow<List<PollOptionView>>

    @Query(GLOBAL_ROOT_QUERY)
    fun internalGetGlobalRootFlow(until: Long, size: Int): Flow<List<RootPostView>>

    @Query(GLOBAL_CROSS_QUERY)
    fun internalGetGlobalCrossFlow(until: Long, size: Int): Flow<List<CrossPostView>>

    @Query(GLOBAL_POLL_QUERY)
    fun internalGetGlobalPollFlow(until: Long, size: Int): Flow<List<PollView>>

    @Query(GLOBAL_POLL_OPTION_QUERY)
    fun internalGetGlobalPollOptionFlow(until: Long, size: Int): Flow<List<PollOptionView>>

    @Query(TOPIC_ONLY_ROOT_QUERY)
    suspend fun internalGetTopicRoot(until: Long, size: Int): List<RootPostView>

    @Query(TOPIC_ONLY_CROSS_QUERY)
    suspend fun internalGetTopicCross(until: Long, size: Int): List<CrossPostView>

    @Query(TOPIC_ONLY_POLL_QUERY)
    suspend fun internalGetTopicPoll(until: Long, size: Int): List<PollView>

    @Query(FRIEND_OR_TOPIC_ROOT_QUERY)
    suspend fun internalGetFriendOrTopicRoot(until: Long, size: Int): List<RootPostView>

    @Query(FRIEND_OR_TOPIC_CROSS_QUERY)
    suspend fun internalGetFriendOrTopicCross(until: Long, size: Int): List<CrossPostView>

    @Query(FRIEND_OR_TOPIC_POLL_QUERY)
    suspend fun internalGetFriendOrTopicPoll(until: Long, size: Int): List<PollView>

    @Query(FRIEND_ONLY_ROOT_QUERY)
    suspend fun internalGetFriendRoot(until: Long, size: Int): List<RootPostView>

    @Query(FRIEND_ONLY_CROSS_QUERY)
    suspend fun internalGetFriendCross(until: Long, size: Int): List<CrossPostView>

    @Query(FRIEND_ONLY_POLL_QUERY)
    suspend fun internalGetFriendPoll(until: Long, size: Int): List<PollView>

    @Query(WOT_OR_TOPIC_ROOT_QUERY)
    suspend fun internalGetWotOrTopicRoot(until: Long, size: Int): List<RootPostView>

    @Query(WOT_OR_TOPIC_CROSS_QUERY)
    suspend fun internalGetWotOrTopicCross(until: Long, size: Int): List<CrossPostView>

    @Query(WOT_OR_TOPIC_POLL_QUERY)
    suspend fun internalGetWotOrTopicPoll(until: Long, size: Int): List<PollView>

    @Query(WOT_ONLY_ROOT_QUERY)
    suspend fun internalGetWotRoot(until: Long, size: Int): List<RootPostView>

    @Query(WOT_ONLY_CROSS_QUERY)
    suspend fun internalGetWotCross(until: Long, size: Int): List<CrossPostView>

    @Query(WOT_ONLY_POLL_QUERY)
    suspend fun internalGetWotPoll(until: Long, size: Int): List<PollView>

    @Query(GLOBAL_ROOT_QUERY)
    suspend fun internalGetGlobalRoot(until: Long, size: Int): List<RootPostView>

    @Query(GLOBAL_CROSS_QUERY)
    suspend fun internalGetGlobalCross(until: Long, size: Int): List<CrossPostView>

    @Query(GLOBAL_POLL_QUERY)
    suspend fun internalGetGlobalPoll(until: Long, size: Int): List<PollView>

    @Query(TOPIC_ONLY_EXISTS_ROOT_QUERY)
    fun internalHasTopicRootFlow(until: Long, size: Int): Flow<Boolean>

    @Query(TOPIC_ONLY_EXISTS_CROSS_QUERY)
    fun internalHasTopicCrossFlow(until: Long, size: Int): Flow<Boolean>

    @Query(TOPIC_ONLY_EXISTS_POLL_QUERY)
    fun internalHasTopicPollFlow(until: Long, size: Int): Flow<Boolean>

    @Query(FRIEND_OR_TOPIC_EXISTS_ROOT_QUERY)
    fun internalHasFriendOrTopicRootFlow(until: Long, size: Int): Flow<Boolean>

    @Query(FRIEND_OR_TOPIC_EXISTS_CROSS_QUERY)
    fun internalHasFriendOrTopicCrossFlow(until: Long, size: Int): Flow<Boolean>

    @Query(FRIEND_OR_TOPIC_EXISTS_POLL_QUERY)
    fun internalHasFriendOrTopicPollFlow(until: Long, size: Int): Flow<Boolean>

    @Query(FRIEND_ONLY_EXISTS_ROOT_QUERY)
    fun internalHasFriendRootFlow(until: Long, size: Int): Flow<Boolean>

    @Query(FRIEND_ONLY_EXISTS_CROSS_QUERY)
    fun internalHasFriendCrossFlow(until: Long, size: Int): Flow<Boolean>

    @Query(FRIEND_ONLY_EXISTS_POLL_QUERY)
    fun internalHasFriendPollFlow(until: Long, size: Int): Flow<Boolean>

    @Query(WOT_OR_TOPIC_EXISTS_ROOT_QUERY)
    fun internalHasWotOrTopicRootFlow(until: Long, size: Int): Flow<Boolean>

    @Query(WOT_OR_TOPIC_EXISTS_CROSS_QUERY)
    fun internalHasWotOrTopicCrossFlow(until: Long, size: Int): Flow<Boolean>

    @Query(WOT_OR_TOPIC_EXISTS_POLL_QUERY)
    fun internalHasWotOrTopicPollFlow(until: Long, size: Int): Flow<Boolean>

    @Query(WOT_ONLY_EXISTS_ROOT_QUERY)
    fun internalHasWotRootFlow(until: Long, size: Int): Flow<Boolean>

    @Query(WOT_ONLY_EXISTS_CROSS_QUERY)
    fun internalHasWotCrossFlow(until: Long, size: Int): Flow<Boolean>

    @Query(WOT_ONLY_EXISTS_POLL_QUERY)
    fun internalHasWotPollFlow(until: Long, size: Int): Flow<Boolean>

    @Query(GLOBAL_EXISTS_ROOT_QUERY)
    fun internalHasGlobalRootFlow(until: Long, size: Int): Flow<Boolean>

    @Query(GLOBAL_EXISTS_CROSS_QUERY)
    fun internalHasGlobalCrossFlow(until: Long, size: Int): Flow<Boolean>

    @Query(GLOBAL_EXISTS_POLL_QUERY)
    fun internalHasGlobalPollFlow(until: Long, size: Int): Flow<Boolean>

    @Query(TOPIC_ONLY_CREATED_AT_ROOT_QUERY)
    suspend fun internalGetTopicCreatedAtRoot(until: Long, size: Int): List<Long>

    @Query(TOPIC_ONLY_CREATED_AT_CROSS_QUERY)
    suspend fun internalGetTopicCreatedAtCross(until: Long, size: Int): List<Long>

    @Query(TOPIC_ONLY_CREATED_AT_POLL_QUERY)
    suspend fun internalGetTopicCreatedAtPoll(until: Long, size: Int): List<Long>

    @Query(FRIEND_OR_TOPIC_CREATED_AT_ROOT_QUERY)
    suspend fun internalGetFriendOrTopicCreatedAtRoot(until: Long, size: Int): List<Long>

    @Query(FRIEND_OR_TOPIC_CREATED_AT_CROSS_QUERY)
    suspend fun internalGetFriendOrTopicCreatedAtCross(until: Long, size: Int): List<Long>

    @Query(FRIEND_OR_TOPIC_CREATED_AT_POLL_QUERY)
    suspend fun internalGetFriendOrTopicCreatedAtPoll(until: Long, size: Int): List<Long>

    @Query(FRIEND_ONLY_CREATED_AT_ROOT_QUERY)
    suspend fun internalGetFriendCreatedAtRoot(until: Long, size: Int): List<Long>

    @Query(FRIEND_ONLY_CREATED_AT_CROSS_QUERY)
    suspend fun internalGetFriendCreatedAtCross(until: Long, size: Int): List<Long>

    @Query(FRIEND_ONLY_CREATED_AT_POLL_QUERY)
    suspend fun internalGetFriendCreatedAtPoll(until: Long, size: Int): List<Long>

    @Query(WOT_OR_TOPIC_CREATED_AT_ROOT_QUERY)
    suspend fun internalGetWotOrTopicCreatedAtRoot(until: Long, size: Int): List<Long>

    @Query(WOT_OR_TOPIC_CREATED_AT_CROSS_QUERY)
    suspend fun internalGetWotOrTopicCreatedAtCross(until: Long, size: Int): List<Long>

    @Query(WOT_OR_TOPIC_CREATED_AT_POLL_QUERY)
    suspend fun internalGetWotOrTopicCreatedAtPoll(until: Long, size: Int): List<Long>

    @Query(WOT_ONLY_CREATED_AT_ROOT_QUERY)
    suspend fun internalGetWotCreatedAtRoot(until: Long, size: Int): List<Long>

    @Query(WOT_ONLY_CREATED_AT_CROSS_QUERY)
    suspend fun internalGetWotCreatedAtCross(until: Long, size: Int): List<Long>

    @Query(WOT_ONLY_CREATED_AT_POLL_QUERY)
    suspend fun internalGetWotCreatedAtPoll(until: Long, size: Int): List<Long>

    @Query(GLOBAL_CREATED_AT_ROOT_QUERY)
    suspend fun internalGetGlobalCreatedAtRoot(until: Long, size: Int): List<Long>

    @Query(GLOBAL_CREATED_AT_CROSS_QUERY)
    suspend fun internalGetGlobalCreatedAtCross(until: Long, size: Int): List<Long>

    @Query(GLOBAL_CREATED_AT_POLL_QUERY)
    suspend fun internalGetGlobalCreatedAtPoll(until: Long, size: Int): List<Long>
}

private fun List<Long>.orNoRoots(setting: HomeFeedSetting): List<Long> {
    return if (setting.showRoots) this else emptyList()
}

private fun List<Long>.orNoCross(setting: HomeFeedSetting): List<Long> {
    return if (setting.showCrossPosts) this else emptyList()
}

private fun List<Long>.orNoPolls(setting: HomeFeedSetting): List<Long> {
    return if (setting.showPolls) this else emptyList()
}
