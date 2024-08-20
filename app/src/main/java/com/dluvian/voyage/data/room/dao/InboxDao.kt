package com.dluvian.voyage.data.room.dao

import androidx.room.Dao
import androidx.room.Query
import com.dluvian.voyage.data.model.FriendPubkeys
import com.dluvian.voyage.data.model.Global
import com.dluvian.voyage.data.model.InboxFeedSetting
import com.dluvian.voyage.data.model.NoPubkeys
import com.dluvian.voyage.data.model.WebOfTrustPubkeys
import com.dluvian.voyage.data.room.view.ReplyView
import com.dluvian.voyage.data.room.view.RootPostView
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf


private const val INBOX_CONDITION = "WHERE createdAt <= :until " +
        "AND isMentioningMe = 1 " +
        "AND authorIsOneself = 0 " +
        "AND authorIsMuted = 0 " +
        "AND authorIsLocked = 0 "

private const val INBOX_ORDER = "ORDER BY createdAt DESC LIMIT :size "

private const val FRIEND_CONDITION = "AND authorIsFriend = 1 "
private const val WOT_CONDITION = "AND authorIsTrusted = 1 "

private const val FRIEND_MAIN_QUERY = INBOX_CONDITION + FRIEND_CONDITION + INBOX_ORDER
private const val WOT_MAIN_QUERY = INBOX_CONDITION + WOT_CONDITION + INBOX_ORDER
private const val GLOBAL_MAIN_QUERY = INBOX_CONDITION + INBOX_ORDER

private const val SELECT_ROOT = "SELECT * FROM RootPostView "
private const val SELECT_REPLY = "SELECT * FROM ReplyView "
private const val SELECT_ROOT_ID = "SELECT id FROM RootPostView "
private const val SELECT_REPLY_ID = "SELECT id FROM ReplyView "
private const val SELECT_ROOT_CREATED_AT = "SELECT createdAt FROM RootPostView "
private const val SELECT_REPLY_CREATED_AT = "SELECT createdAt FROM ReplyView "

private const val FRIEND_ROOT_QUERY = SELECT_ROOT + FRIEND_MAIN_QUERY
private const val FRIEND_REPLY_QUERY = SELECT_REPLY + FRIEND_MAIN_QUERY

private const val WOT_ROOT_QUERY = SELECT_ROOT + WOT_MAIN_QUERY
private const val WOT_REPLY_QUERY = SELECT_REPLY + WOT_MAIN_QUERY

private const val GLOBAL_ROOT_QUERY = SELECT_ROOT + GLOBAL_MAIN_QUERY
private const val GLOBAL_REPLY_QUERY = SELECT_REPLY + GLOBAL_MAIN_QUERY

private const val FRIEND_REPLY_ID_QUERY = SELECT_REPLY_ID + INBOX_CONDITION + FRIEND_CONDITION
private const val FRIEND_ROOT_ID_QUERY = SELECT_ROOT_ID + INBOX_CONDITION + FRIEND_CONDITION

private const val WOT_REPLY_ID_QUERY = SELECT_REPLY_ID + INBOX_CONDITION + WOT_CONDITION
private const val WOT_ROOT_ID_QUERY = SELECT_ROOT_ID + INBOX_CONDITION + WOT_CONDITION

private const val GLOBAL_REPLY_ID_QUERY = SELECT_REPLY_ID + INBOX_CONDITION
private const val GLOBAL_ROOT_ID_QUERY = SELECT_ROOT_ID + INBOX_CONDITION

private const val FRIEND_INBOX_EXISTS_QUERY =
    "SELECT EXISTS($FRIEND_REPLY_ID_QUERY UNION $FRIEND_ROOT_ID_QUERY)"
private const val WOT_INBOX_EXISTS_QUERY =
    "SELECT EXISTS($WOT_REPLY_ID_QUERY UNION $WOT_ROOT_ID_QUERY)"
private const val GLOBAL_INBOX_EXISTS_QUERY =
    "SELECT EXISTS($GLOBAL_REPLY_ID_QUERY UNION $GLOBAL_ROOT_ID_QUERY)"

private const val FRIEND_ROOT_INBOX_CREATED_AT_QUERY = "$SELECT_ROOT_CREATED_AT $FRIEND_MAIN_QUERY"
private const val FRIEND_REPLY_INBOX_CREATED_AT_QUERY =
    "$SELECT_REPLY_CREATED_AT $FRIEND_MAIN_QUERY"

private const val WOT_ROOT_INBOX_CREATED_AT_QUERY = "$SELECT_ROOT_CREATED_AT $WOT_MAIN_QUERY"
private const val WOT_REPLY_INBOX_CREATED_AT_QUERY = "$SELECT_REPLY_CREATED_AT $WOT_MAIN_QUERY"

private const val GLOBAL_ROOT_INBOX_CREATED_AT_QUERY = "$SELECT_ROOT_CREATED_AT $GLOBAL_MAIN_QUERY"
private const val GLOBAL_REPLY_INBOX_CREATED_AT_QUERY =
    "$SELECT_REPLY_CREATED_AT $GLOBAL_MAIN_QUERY"


@Dao
interface InboxDao {
    fun getInboxReplyFlow(
        setting: InboxFeedSetting,
        until: Long,
        size: Int
    ): Flow<List<ReplyView>> {
        return when (setting.pubkeySelection) {
            FriendPubkeys -> internalGetFriendReplyFlow(until = until, size = size)
            WebOfTrustPubkeys -> internalGetWotReplyFlow(until = until, size = size)
            Global -> internalGetGlobalReplyFlow(until = until, size = size)
            NoPubkeys -> flowOf(emptyList())
        }
    }

    suspend fun getInboxReplies(
        setting: InboxFeedSetting,
        until: Long,
        size: Int
    ): List<ReplyView> {
        return when (setting.pubkeySelection) {
            FriendPubkeys -> internalGetFriendReply(until = until, size = size)
            WebOfTrustPubkeys -> internalGetWotReply(until = until, size = size)
            Global -> internalGetGlobalReply(until = until, size = size)
            NoPubkeys -> emptyList()
        }
    }

    fun getMentionRootFlow(
        setting: InboxFeedSetting,
        until: Long,
        size: Int
    ): Flow<List<RootPostView>> {
        return when (setting.pubkeySelection) {
            FriendPubkeys -> internalGetFriendRootFlow(until = until, size = size)
            WebOfTrustPubkeys -> internalGetWotRootFlow(until = until, size = size)
            Global -> internalGetGlobalRootFlow(until = until, size = size)
            NoPubkeys -> flowOf(emptyList())
        }
    }

    suspend fun getMentionRoots(
        setting: InboxFeedSetting,
        until: Long,
        size: Int
    ): List<RootPostView> {
        return when (setting.pubkeySelection) {
            FriendPubkeys -> internalGetFriendRoot(until = until, size = size)
            WebOfTrustPubkeys -> internalGetWotRoot(until = until, size = size)
            Global -> internalGetGlobalRoot(until = until, size = size)
            NoPubkeys -> emptyList()
        }
    }

    fun hasInboxFlow(setting: InboxFeedSetting, until: Long = Long.MAX_VALUE): Flow<Boolean> {
        return when (setting.pubkeySelection) {
            FriendPubkeys -> internalHasFriendInboxFlow(until = until)
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
            FriendPubkeys -> internalGetFriendRootCreatedAt(until = until, size = size)
                .plus(internalGetFriendReplyCreatedAt(until = until, size = size))

            WebOfTrustPubkeys -> internalGetWotRootCreatedAt(until = until, size = size)
                .plus(internalGetWotReplyCreatedAt(until = until, size = size))

            Global, NoPubkeys -> internalGetGlobalRootCreatedAt(until = until, size = size)
                .plus(internalGetGlobalReplyCreatedAt(until = until, size = size))
        }
            .sortedDescending()
            .take(size)
    }

    @Query(FRIEND_REPLY_QUERY)
    fun internalGetFriendReplyFlow(until: Long, size: Int): Flow<List<ReplyView>>

    @Query(FRIEND_ROOT_QUERY)
    fun internalGetFriendRootFlow(until: Long, size: Int): Flow<List<RootPostView>>

    @Query(WOT_REPLY_QUERY)
    fun internalGetWotReplyFlow(until: Long, size: Int): Flow<List<ReplyView>>

    @Query(WOT_ROOT_QUERY)
    fun internalGetWotRootFlow(until: Long, size: Int): Flow<List<RootPostView>>

    @Query(GLOBAL_REPLY_QUERY)
    fun internalGetGlobalReplyFlow(until: Long, size: Int): Flow<List<ReplyView>>

    @Query(GLOBAL_ROOT_QUERY)
    fun internalGetGlobalRootFlow(until: Long, size: Int): Flow<List<RootPostView>>

    @Query(FRIEND_REPLY_QUERY)
    suspend fun internalGetFriendReply(until: Long, size: Int): List<ReplyView>

    @Query(FRIEND_ROOT_QUERY)
    suspend fun internalGetFriendRoot(until: Long, size: Int): List<RootPostView>

    @Query(WOT_REPLY_QUERY)
    suspend fun internalGetWotReply(until: Long, size: Int): List<ReplyView>

    @Query(WOT_ROOT_QUERY)
    suspend fun internalGetWotRoot(until: Long, size: Int): List<RootPostView>

    @Query(GLOBAL_REPLY_QUERY)
    suspend fun internalGetGlobalReply(until: Long, size: Int): List<ReplyView>

    @Query(GLOBAL_ROOT_QUERY)
    suspend fun internalGetGlobalRoot(until: Long, size: Int): List<RootPostView>

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
}
