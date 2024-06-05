package com.dluvian.voyage.data.provider

import com.dluvian.voyage.core.mergeToParentUIList
import com.dluvian.voyage.core.model.ParentUI
import com.dluvian.voyage.core.model.ReplyUI
import com.dluvian.voyage.core.model.RootPostUI
import com.dluvian.voyage.data.model.BookmarksFeedSetting
import com.dluvian.voyage.data.model.FeedSetting
import com.dluvian.voyage.data.model.HomeFeedSetting
import com.dluvian.voyage.data.model.InboxFeedSetting
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
    ): List<ParentUI> {
        return when (setting) {
            is RootFeedSetting -> getStaticRootFeed(until = until, size = size, setting = setting)
            is ReplyFeedSetting -> getStaticReplyFeed(until = until, size = size, setting = setting)
            InboxFeedSetting -> getStaticInboxFeed(until = until, size = size)
            BookmarksFeedSetting -> getStaticBooksmarksFeed(until = until, size = size)
        }
    }

    private suspend fun getStaticRootFeed(
        until: Long,
        size: Int,
        setting: RootFeedSetting,
    ): List<RootPostUI> {
        return when (setting) {
            is HomeFeedSetting -> room.rootPostDao().getHomeRootPosts(until = until, size = size)
            is TopicFeedSetting -> room.rootPostDao().getTopicRootPosts(
                topic = setting.topic,
                until = until,
                size = size
            )

            is ProfileRootFeedSetting -> room.rootPostDao().getProfileRootPosts(
                pubkey = setting.pubkey,
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
        until: Long,
        size: Int,
        setting: ReplyFeedSetting,
    ): List<ReplyUI> {
        return room.replyDao().getProfileReplies(
            pubkey = setting.pubkey,
            until = until,
            size = size
        )
            .map {
                it.mapToReplyUI(
                    forcedVotes = emptyMap(),
                    forcedFollows = emptyMap(),
                    forcedBookmarks = emptyMap(),
                    annotatedStringProvider = annotatedStringProvider
                )
            }
    }

    private suspend fun getStaticInboxFeed(until: Long, size: Int): List<ParentUI> {
        val replies = room.directReplyDao().getDirectReplies(until = until, size = size)
        val crossPosts = room.directCrossPostDao().getDirectCrossPosts(until = until, size = size)

        return mergeToParentUIList(
            replies = replies,
            roots = crossPosts,
            votes = emptyMap(),
            follows = emptyMap(),
            bookmarks = emptyMap(),
            size = size,
            annotatedStringProvider = annotatedStringProvider
        )
    }

    private suspend fun getStaticBooksmarksFeed(until: Long, size: Int): List<ParentUI> {
        val roots = room.bookmarkDao().getRootPosts(until = until, size = size)
        val replies = room.bookmarkDao().getReplies(until = until, size = size)

        return mergeToParentUIList(
            replies = replies,
            roots = roots,
            votes = emptyMap(),
            follows = emptyMap(),
            bookmarks = emptyMap(),
            size = size,
            annotatedStringProvider = annotatedStringProvider
        )
    }
}