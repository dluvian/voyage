package com.dluvian.voyage.data.room.dao

import androidx.room.Dao
import androidx.room.Query
import com.dluvian.voyage.data.model.FriendPubkeysNoLock
import com.dluvian.voyage.data.model.Global
import com.dluvian.voyage.data.model.InboxFeedSetting
import com.dluvian.voyage.data.model.NoPubkeys
import com.dluvian.voyage.data.model.WebOfTrustPubkeys
import com.dluvian.voyage.data.room.view.CommentView
import com.dluvian.voyage.data.room.view.LegacyReplyView
import com.dluvian.voyage.data.room.view.PollOptionView
import com.dluvian.voyage.data.room.view.PollView
import com.dluvian.voyage.data.room.view.RootPostView
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf


private const val INBOX_CONDITION = "WHERE createdAt <= :until " +
        "AND isMentioningMe = 1 " +
        "AND authorIsOneself = 0 " +
        "AND authorIsLocked = 0 "

private const val INBOX_ORDER = "ORDER BY createdAt DESC LIMIT :size "

private const val FRIEND_CONDITION = "AND authorIsFriend = 1 "
private const val WOT_CONDITION = "AND authorIsTrusted = 1 "

private const val FRIEND_MAIN_QUERY = INBOX_CONDITION + FRIEND_CONDITION + INBOX_ORDER
private const val WOT_MAIN_QUERY = INBOX_CONDITION + WOT_CONDITION + INBOX_ORDER
private const val GLOBAL_MAIN_QUERY = INBOX_CONDITION + INBOX_ORDER

private const val SELECT_ROOT = "SELECT * FROM RootPostView "
private const val SELECT_REPLY = "SELECT * FROM LegacyReplyView "
private const val SELECT_COMMENT = "SELECT * FROM CommentView "
private const val SELECT_POLL = "SELECT * FROM PollView "
private const val SELECT_POLL_OPTION = "SELECT * FROM PollOptionView "

private const val SELECT_ROOT_ID = "SELECT id FROM RootPostView "
private const val SELECT_REPLY_ID = "SELECT id FROM LegacyReplyView "
private const val SELECT_COMMENT_ID = "SELECT id FROM CommentView "
private const val SELECT_POLL_ID = "SELECT id FROM PollView "

private const val SELECT_ROOT_CREATED_AT = "SELECT createdAt FROM RootPostView "
private const val SELECT_REPLY_CREATED_AT = "SELECT createdAt FROM LegacyReplyView "
private const val SELECT_COMMENT_CREATED_AT = "SELECT createdAt FROM CommentView "
private const val SELECT_POLL_CREATED_AT = "SELECT createdAt FROM PollView "

private const val FRIEND_ROOT_QUERY = SELECT_ROOT + FRIEND_MAIN_QUERY
private const val FRIEND_REPLY_QUERY = SELECT_REPLY + FRIEND_MAIN_QUERY
private const val FRIEND_COMMENT_QUERY = SELECT_COMMENT + FRIEND_MAIN_QUERY
private const val FRIEND_POLL_QUERY = SELECT_POLL + FRIEND_MAIN_QUERY
private const val FRIEND_POLL_OPTION_QUERY =
    "$SELECT_POLL_OPTION WHERE pollId IN (SELECT id FROM PollView $FRIEND_MAIN_QUERY)"

private const val WOT_ROOT_QUERY = SELECT_ROOT + WOT_MAIN_QUERY
private const val WOT_REPLY_QUERY = SELECT_REPLY + WOT_MAIN_QUERY
private const val WOT_COMMENT_QUERY = SELECT_COMMENT + WOT_MAIN_QUERY
private const val WOT_POLL_QUERY = SELECT_POLL + WOT_MAIN_QUERY
private const val WOT_POLL_OPTION_QUERY =
    "$SELECT_POLL_OPTION WHERE pollId IN (SELECT id FROM PollView $WOT_MAIN_QUERY)"

private const val GLOBAL_ROOT_QUERY = SELECT_ROOT + GLOBAL_MAIN_QUERY
private const val GLOBAL_REPLY_QUERY = SELECT_REPLY + GLOBAL_MAIN_QUERY
private const val GLOBAL_COMMENT_QUERY = SELECT_COMMENT + GLOBAL_MAIN_QUERY
private const val GLOBAL_POLL_QUERY = SELECT_POLL + GLOBAL_MAIN_QUERY
private const val GLOBAL_POLL_OPTION_QUERY =
    "$SELECT_POLL_OPTION WHERE pollId IN (SELECT id FROM PollView $GLOBAL_MAIN_QUERY)"

private const val FRIEND_ROOT_ID_QUERY = SELECT_ROOT_ID + INBOX_CONDITION + FRIEND_CONDITION
private const val FRIEND_REPLY_ID_QUERY = SELECT_REPLY_ID + INBOX_CONDITION + FRIEND_CONDITION
private const val FRIEND_COMMENT_ID_QUERY = SELECT_COMMENT_ID + INBOX_CONDITION + FRIEND_CONDITION
private const val FRIEND_POLL_ID_QUERY = SELECT_POLL_ID + INBOX_CONDITION + FRIEND_CONDITION

private const val WOT_ROOT_ID_QUERY = SELECT_ROOT_ID + INBOX_CONDITION + WOT_CONDITION
private const val WOT_REPLY_ID_QUERY = SELECT_REPLY_ID + INBOX_CONDITION + WOT_CONDITION
private const val WOT_COMMENT_ID_QUERY = SELECT_COMMENT_ID + INBOX_CONDITION + WOT_CONDITION
private const val WOT_POLL_ID_QUERY = SELECT_POLL_ID + INBOX_CONDITION + WOT_CONDITION

private const val GLOBAL_ROOT_ID_QUERY = SELECT_ROOT_ID + INBOX_CONDITION
private const val GLOBAL_REPLY_ID_QUERY = SELECT_REPLY_ID + INBOX_CONDITION
private const val GLOBAL_COMMENT_ID_QUERY = SELECT_COMMENT_ID + INBOX_CONDITION
private const val GLOBAL_POLL_ID_QUERY = SELECT_POLL_ID + INBOX_CONDITION

private const val FRIEND_INBOX_EXISTS_QUERY = "SELECT EXISTS(" +
        "$FRIEND_ROOT_ID_QUERY " +
        "UNION $FRIEND_REPLY_ID_QUERY " +
        "UNION $FRIEND_COMMENT_ID_QUERY " +
        "UNION $FRIEND_POLL_ID_QUERY)"
private const val WOT_INBOX_EXISTS_QUERY = "SELECT EXISTS(" +
        "$WOT_ROOT_ID_QUERY " +
        "UNION $WOT_REPLY_ID_QUERY " +
        "UNION $WOT_COMMENT_ID_QUERY " +
        "UNION $WOT_POLL_ID_QUERY)"
private const val GLOBAL_INBOX_EXISTS_QUERY = "SELECT EXISTS(" +
        "$GLOBAL_ROOT_ID_QUERY " +
        "UNION $GLOBAL_REPLY_ID_QUERY " +
        "UNION $GLOBAL_COMMENT_ID_QUERY " +
        "UNION $GLOBAL_POLL_ID_QUERY)"

private const val FRIEND_ROOT_INBOX_CREATED_AT_QUERY = "$SELECT_ROOT_CREATED_AT $FRIEND_MAIN_QUERY"
private const val FRIEND_REPLY_INBOX_CREATED_AT_QUERY =
    "$SELECT_REPLY_CREATED_AT $FRIEND_MAIN_QUERY"
private const val FRIEND_COMMENT_INBOX_CREATED_AT_QUERY =
    "$SELECT_COMMENT_CREATED_AT $FRIEND_MAIN_QUERY"
private const val FRIEND_POLL_INBOX_CREATED_AT_QUERY =
    "$SELECT_POLL_CREATED_AT $FRIEND_MAIN_QUERY"

private const val WOT_ROOT_INBOX_CREATED_AT_QUERY = "$SELECT_ROOT_CREATED_AT $WOT_MAIN_QUERY"
private const val WOT_REPLY_INBOX_CREATED_AT_QUERY = "$SELECT_REPLY_CREATED_AT $WOT_MAIN_QUERY"
private const val WOT_COMMENT_INBOX_CREATED_AT_QUERY = "$SELECT_COMMENT_CREATED_AT $WOT_MAIN_QUERY"
private const val WOT_POLL_INBOX_CREATED_AT_QUERY = "$SELECT_POLL_CREATED_AT $WOT_MAIN_QUERY"

private const val GLOBAL_ROOT_INBOX_CREATED_AT_QUERY = "$SELECT_ROOT_CREATED_AT $GLOBAL_MAIN_QUERY"
private const val GLOBAL_REPLY_INBOX_CREATED_AT_QUERY =
    "$SELECT_REPLY_CREATED_AT $GLOBAL_MAIN_QUERY"
private const val GLOBAL_COMMENT_INBOX_CREATED_AT_QUERY =
    "$SELECT_COMMENT_CREATED_AT $GLOBAL_MAIN_QUERY"
private const val GLOBAL_POLL_INBOX_CREATED_AT_QUERY =
    "$SELECT_POLL_CREATED_AT $GLOBAL_MAIN_QUERY"


@Dao
interface InboxDao {

    fun getInboxRootFlow(
        setting: InboxFeedSetting,
        until: Long,
        size: Int
    ): Flow<List<RootPostView>> {
        return when (setting.pubkeySelection) {
            FriendPubkeysNoLock -> internalGetFriendRootFlow(until = until, size = size)
            WebOfTrustPubkeys -> internalGetWotRootFlow(until = until, size = size)
            Global -> internalGetGlobalRootFlow(until = until, size = size)
            NoPubkeys -> flowOf(emptyList())
        }
    }

    suspend fun getInboxRoots(
        setting: InboxFeedSetting,
        until: Long,
        size: Int
    ): List<RootPostView> {
        return when (setting.pubkeySelection) {
            FriendPubkeysNoLock -> internalGetFriendRoot(until = until, size = size)
            WebOfTrustPubkeys -> internalGetWotRoot(until = until, size = size)
            Global -> internalGetGlobalRoot(until = until, size = size)
            NoPubkeys -> emptyList()
        }
    }

    fun getInboxReplyFlow(
        setting: InboxFeedSetting,
        until: Long,
        size: Int
    ): Flow<List<LegacyReplyView>> {
        return when (setting.pubkeySelection) {
            FriendPubkeysNoLock -> internalGetFriendReplyFlow(until = until, size = size)
            WebOfTrustPubkeys -> internalGetWotReplyFlow(until = until, size = size)
            Global -> internalGetGlobalReplyFlow(until = until, size = size)
            NoPubkeys -> flowOf(emptyList())
        }
    }

    fun getInboxCommentFlow(
        setting: InboxFeedSetting,
        until: Long,
        size: Int
    ): Flow<List<CommentView>> {
        return when (setting.pubkeySelection) {
            FriendPubkeysNoLock -> internalGetFriendCommentFlow(until = until, size = size)
            WebOfTrustPubkeys -> internalGetWotCommentFlow(until = until, size = size)
            Global -> internalGetGlobalCommentFlow(until = until, size = size)
            NoPubkeys -> flowOf(emptyList())
        }
    }

    fun getInboxPollFlow(
        setting: InboxFeedSetting,
        until: Long,
        size: Int
    ): Flow<List<PollView>> {
        return when (setting.pubkeySelection) {
            FriendPubkeysNoLock -> internalGetFriendPollFlow(until = until, size = size)
            WebOfTrustPubkeys -> internalGetWotPollFlow(until = until, size = size)
            Global -> internalGetGlobalPollFlow(until = until, size = size)
            NoPubkeys -> flowOf(emptyList())
        }
    }

    fun getInboxPollOptionFlow(
        setting: InboxFeedSetting,
        until: Long,
        size: Int
    ): Flow<List<PollOptionView>> {
        return when (setting.pubkeySelection) {
            FriendPubkeysNoLock -> internalGetFriendPollOptionFlow(until = until, size = size)
            WebOfTrustPubkeys -> internalGetWotPollOptionFlow(until = until, size = size)
            Global -> internalGetGlobalPollOptionFlow(until = until, size = size)
            NoPubkeys -> flowOf(emptyList())
        }
    }

    suspend fun getInboxReplies(
        setting: InboxFeedSetting,
        until: Long,
        size: Int
    ): List<LegacyReplyView> {
        return when (setting.pubkeySelection) {
            FriendPubkeysNoLock -> internalGetFriendReply(until = until, size = size)
            WebOfTrustPubkeys -> internalGetWotReply(until = until, size = size)
            Global -> internalGetGlobalReply(until = until, size = size)
            NoPubkeys -> emptyList()
        }
    }

    suspend fun getInboxComments(
        setting: InboxFeedSetting,
        until: Long,
        size: Int
    ): List<CommentView> {
        return when (setting.pubkeySelection) {
            FriendPubkeysNoLock -> internalGetFriendComment(until = until, size = size)
            WebOfTrustPubkeys -> internalGetWotComment(until = until, size = size)
            Global -> internalGetGlobalComment(until = until, size = size)
            NoPubkeys -> emptyList()
        }
    }

    suspend fun getInboxPolls(
        setting: InboxFeedSetting,
        until: Long,
        size: Int
    ): List<PollView> {
        return when (setting.pubkeySelection) {
            FriendPubkeysNoLock -> internalGetFriendPoll(until = until, size = size)
            WebOfTrustPubkeys -> internalGetWotPoll(until = until, size = size)
            Global -> internalGetGlobalPoll(until = until, size = size)
            NoPubkeys -> emptyList()
        }
    }

    fun hasInboxFlow(setting: InboxFeedSetting, until: Long = Long.MAX_VALUE): Flow<Boolean> {
        return when (setting.pubkeySelection) {
            FriendPubkeysNoLock -> internalHasFriendInboxFlow(until = until)
            WebOfTrustPubkeys -> internalHasWotInboxFlow(until = until)
            Global -> internalHasGlobalInboxFlow(until = until)
            NoPubkeys -> flowOf(false)
        }
    }

    suspend fun getInboxCreatedAt(
        setting: InboxFeedSetting,
        until: Long,
        size: Int
    ): List<Long> {
        return when (setting.pubkeySelection) {
            FriendPubkeysNoLock -> internalGetFriendRootCreatedAt(until = until, size = size)
                .plus(internalGetFriendReplyCreatedAt(until = until, size = size))
                .plus(internalGetFriendCommentCreatedAt(until = until, size = size))
                .plus(internalGetFriendPollCreatedAt(until = until, size = size))

            WebOfTrustPubkeys -> internalGetWotRootCreatedAt(until = until, size = size)
                .plus(internalGetWotReplyCreatedAt(until = until, size = size))
                .plus(internalGetWotCommentCreatedAt(until = until, size = size))
                .plus(internalGetWotPollCreatedAt(until = until, size = size))

            Global, NoPubkeys -> internalGetGlobalRootCreatedAt(until = until, size = size)
                .plus(internalGetGlobalReplyCreatedAt(until = until, size = size))
                .plus(internalGetGlobalCommentCreatedAt(until = until, size = size))
                .plus(internalGetGlobalPollCreatedAt(until = until, size = size))
        }
            .sortedDescending()
            .take(size)
    }

    @Query(FRIEND_ROOT_QUERY)
    fun internalGetFriendRootFlow(until: Long, size: Int): Flow<List<RootPostView>>

    @Query(FRIEND_REPLY_QUERY)
    fun internalGetFriendReplyFlow(until: Long, size: Int): Flow<List<LegacyReplyView>>

    @Query(FRIEND_COMMENT_QUERY)
    fun internalGetFriendCommentFlow(until: Long, size: Int): Flow<List<CommentView>>

    @Query(FRIEND_POLL_QUERY)
    fun internalGetFriendPollFlow(until: Long, size: Int): Flow<List<PollView>>

    @Query(FRIEND_POLL_OPTION_QUERY)
    fun internalGetFriendPollOptionFlow(until: Long, size: Int): Flow<List<PollOptionView>>

    @Query(WOT_ROOT_QUERY)
    fun internalGetWotRootFlow(until: Long, size: Int): Flow<List<RootPostView>>

    @Query(WOT_REPLY_QUERY)
    fun internalGetWotReplyFlow(until: Long, size: Int): Flow<List<LegacyReplyView>>

    @Query(WOT_COMMENT_QUERY)
    fun internalGetWotCommentFlow(until: Long, size: Int): Flow<List<CommentView>>

    @Query(WOT_POLL_QUERY)
    fun internalGetWotPollFlow(until: Long, size: Int): Flow<List<PollView>>

    @Query(WOT_POLL_OPTION_QUERY)
    fun internalGetWotPollOptionFlow(until: Long, size: Int): Flow<List<PollOptionView>>

    @Query(GLOBAL_ROOT_QUERY)
    fun internalGetGlobalRootFlow(until: Long, size: Int): Flow<List<RootPostView>>

    @Query(GLOBAL_REPLY_QUERY)
    fun internalGetGlobalReplyFlow(until: Long, size: Int): Flow<List<LegacyReplyView>>

    @Query(GLOBAL_COMMENT_QUERY)
    fun internalGetGlobalCommentFlow(until: Long, size: Int): Flow<List<CommentView>>

    @Query(GLOBAL_POLL_QUERY)
    fun internalGetGlobalPollFlow(until: Long, size: Int): Flow<List<PollView>>

    @Query(GLOBAL_POLL_OPTION_QUERY)
    fun internalGetGlobalPollOptionFlow(until: Long, size: Int): Flow<List<PollOptionView>>

    @Query(FRIEND_ROOT_QUERY)
    suspend fun internalGetFriendRoot(until: Long, size: Int): List<RootPostView>

    @Query(FRIEND_REPLY_QUERY)
    suspend fun internalGetFriendReply(until: Long, size: Int): List<LegacyReplyView>

    @Query(FRIEND_COMMENT_QUERY)
    suspend fun internalGetFriendComment(until: Long, size: Int): List<CommentView>

    @Query(FRIEND_POLL_QUERY)
    suspend fun internalGetFriendPoll(until: Long, size: Int): List<PollView>

    @Query(WOT_ROOT_QUERY)
    suspend fun internalGetWotRoot(until: Long, size: Int): List<RootPostView>

    @Query(WOT_REPLY_QUERY)
    suspend fun internalGetWotReply(until: Long, size: Int): List<LegacyReplyView>

    @Query(WOT_COMMENT_QUERY)
    suspend fun internalGetWotComment(until: Long, size: Int): List<CommentView>

    @Query(WOT_POLL_QUERY)
    suspend fun internalGetWotPoll(until: Long, size: Int): List<PollView>

    @Query(GLOBAL_ROOT_QUERY)
    suspend fun internalGetGlobalRoot(until: Long, size: Int): List<RootPostView>

    @Query(GLOBAL_REPLY_QUERY)
    suspend fun internalGetGlobalReply(until: Long, size: Int): List<LegacyReplyView>

    @Query(GLOBAL_COMMENT_QUERY)
    suspend fun internalGetGlobalComment(until: Long, size: Int): List<CommentView>

    @Query(GLOBAL_POLL_QUERY)
    suspend fun internalGetGlobalPoll(until: Long, size: Int): List<PollView>

    @Query(FRIEND_INBOX_EXISTS_QUERY)
    fun internalHasFriendInboxFlow(until: Long): Flow<Boolean>

    @Query(WOT_INBOX_EXISTS_QUERY)
    fun internalHasWotInboxFlow(until: Long): Flow<Boolean>

    @Query(GLOBAL_INBOX_EXISTS_QUERY)
    fun internalHasGlobalInboxFlow(until: Long): Flow<Boolean>

    @Query(FRIEND_ROOT_INBOX_CREATED_AT_QUERY)
    suspend fun internalGetFriendRootCreatedAt(until: Long, size: Int): List<Long>

    @Query(WOT_ROOT_INBOX_CREATED_AT_QUERY)
    suspend fun internalGetWotRootCreatedAt(until: Long, size: Int): List<Long>

    @Query(GLOBAL_ROOT_INBOX_CREATED_AT_QUERY)
    suspend fun internalGetGlobalRootCreatedAt(until: Long, size: Int): List<Long>

    @Query(FRIEND_REPLY_INBOX_CREATED_AT_QUERY)
    suspend fun internalGetFriendReplyCreatedAt(until: Long, size: Int): List<Long>

    @Query(WOT_REPLY_INBOX_CREATED_AT_QUERY)
    suspend fun internalGetWotReplyCreatedAt(until: Long, size: Int): List<Long>

    @Query(GLOBAL_REPLY_INBOX_CREATED_AT_QUERY)
    suspend fun internalGetGlobalReplyCreatedAt(until: Long, size: Int): List<Long>

    @Query(FRIEND_COMMENT_INBOX_CREATED_AT_QUERY)
    suspend fun internalGetFriendCommentCreatedAt(until: Long, size: Int): List<Long>

    @Query(WOT_COMMENT_INBOX_CREATED_AT_QUERY)
    suspend fun internalGetWotCommentCreatedAt(until: Long, size: Int): List<Long>

    @Query(GLOBAL_COMMENT_INBOX_CREATED_AT_QUERY)
    suspend fun internalGetGlobalCommentCreatedAt(until: Long, size: Int): List<Long>

    @Query(FRIEND_POLL_INBOX_CREATED_AT_QUERY)
    suspend fun internalGetFriendPollCreatedAt(until: Long, size: Int): List<Long>

    @Query(WOT_POLL_INBOX_CREATED_AT_QUERY)
    suspend fun internalGetWotPollCreatedAt(until: Long, size: Int): List<Long>

    @Query(GLOBAL_POLL_INBOX_CREATED_AT_QUERY)
    suspend fun internalGetGlobalPollCreatedAt(until: Long, size: Int): List<Long>
}
