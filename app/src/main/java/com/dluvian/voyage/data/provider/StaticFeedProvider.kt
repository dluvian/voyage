package com.dluvian.voyage.data.provider

import com.dluvian.voyage.core.model.LegacyReply
import com.dluvian.voyage.core.model.MainEvent
import com.dluvian.voyage.core.model.RootPost
import com.dluvian.voyage.core.utils.mergeToParentUIList
import com.dluvian.voyage.data.model.BookmarksFeedSetting
import com.dluvian.voyage.data.model.FeedSetting
import com.dluvian.voyage.data.model.HomeFeedSetting
import com.dluvian.voyage.data.model.InboxFeedSetting
import com.dluvian.voyage.data.model.ListFeedSetting
import com.dluvian.voyage.data.model.ProfileRootFeedSetting
import com.dluvian.voyage.data.model.ReplyFeedSetting
import com.dluvian.voyage.data.model.RootFeedSetting
import com.dluvian.voyage.data.model.TopicFeedSetting
import com.dluvian.voyage.data.room.AppDatabase

class StaticFeedProvider(
    private val room: AppDatabase,
    private val annotatedStringProvider: AnnotatedStringProvider
) {
    suspend fun getStaticFeed(
        until: Long,
        size: Int,
        setting: FeedSetting,
    ): List<MainEvent> {
        return when (setting) {
            is RootFeedSetting -> getStaticRootFeed(setting = setting, until = until, size = size)
            is ReplyFeedSetting -> getStaticReplyFeed(setting = setting, until = until, size = size)
            is InboxFeedSetting -> getStaticInboxFeed(setting = setting, until = until, size = size)
            BookmarksFeedSetting -> getStaticBooksmarksFeed(until = until, size = size)
        }
    }

    private suspend fun getStaticRootFeed(
        setting: RootFeedSetting,
        until: Long,
        size: Int,
    ): List<RootPost> {
        return when (setting) {
            is HomeFeedSetting -> room.homeFeedDao().getHomeRootPosts(
                setting = setting,
                until = until,
                size = size
            )

            is TopicFeedSetting -> room.rootPostDao().getTopicRootPosts(
                topic = setting.topic,
                until = until,
                size = size
            )

            is ProfileRootFeedSetting -> room.rootPostDao().getProfileRootPosts(
                pubkey = setting.nprofile.publicKey().toHex(),
                until = until,
                size = size
            )

            is ListFeedSetting -> room.rootPostDao().getListRootPosts(
                identifier = setting.identifier,
                until = until,
                size = size
            )
        }.map {
            it.mapToRootPostUI(
                forcedVotes = emptyMap(),
                forcedFollows = emptyMap(),
                forcedBookmarks = emptyMap(),
                annotatedStringProvider = annotatedStringProvider
            )
        }
    }

    private suspend fun getStaticReplyFeed(
        setting: ReplyFeedSetting,
        until: Long,
        size: Int,
    ): List<LegacyReply> {
        return room.replyDao().getProfileReplies(
            pubkey = setting.nprofile.publicKey().toHex(),
            until = until,
            size = size
        )
            .map {
                it.mapToLegacyReplyUI(
                    forcedVotes = emptyMap(),
                    forcedFollows = emptyMap(),
                    forcedBookmarks = emptyMap(),
                    annotatedStringProvider = annotatedStringProvider
                )
            }
    }

    private suspend fun getStaticInboxFeed(
        setting: InboxFeedSetting,
        until: Long,
        size: Int
    ): List<MainEvent> {
        return mergeToParentUIList(
            replies = room.inboxDao().getInboxReplies(
                setting = setting,
                until = until,
                size = size
            ),
            roots = room.inboxDao().getMentionRoots(
                setting = setting,
                until = until,
                size = size
            ),
            votes = emptyMap(),
            follows = emptyMap(),
            bookmarks = emptyMap(),
            size = size,
            annotatedStringProvider = annotatedStringProvider
        )
    }

    private suspend fun getStaticBooksmarksFeed(until: Long, size: Int): List<MainEvent> {
        return mergeToParentUIList(
            replies = room.bookmarkDao().getReplies(until = until, size = size),
            roots = room.bookmarkDao().getRootPosts(until = until, size = size),
            votes = emptyMap(),
            follows = emptyMap(),
            bookmarks = emptyMap(),
            size = size,
            annotatedStringProvider = annotatedStringProvider
        )
    }
}