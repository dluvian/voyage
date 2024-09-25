package com.dluvian.voyage.data.provider

import com.dluvian.voyage.core.model.LegacyReply
import com.dluvian.voyage.core.model.MainEvent
import com.dluvian.voyage.core.utils.mergeToParentUIList
import com.dluvian.voyage.data.model.BookmarksFeedSetting
import com.dluvian.voyage.data.model.FeedSetting
import com.dluvian.voyage.data.model.HomeFeedSetting
import com.dluvian.voyage.data.model.InboxFeedSetting
import com.dluvian.voyage.data.model.ListFeedSetting
import com.dluvian.voyage.data.model.MainFeedSetting
import com.dluvian.voyage.data.model.ProfileFeedSetting
import com.dluvian.voyage.data.model.ReplyFeedSetting
import com.dluvian.voyage.data.model.TopicFeedSetting
import com.dluvian.voyage.data.room.AppDatabase
import com.dluvian.voyage.data.room.view.CrossPostView
import com.dluvian.voyage.data.room.view.RootPostView

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
            is MainFeedSetting -> getStaticMainFeed(setting = setting, until = until, size = size)
            is ReplyFeedSetting -> getStaticReplyFeed(setting = setting, until = until, size = size)
            is InboxFeedSetting -> getStaticInboxFeed(setting = setting, until = until, size = size)
            BookmarksFeedSetting -> getStaticBooksmarksFeed(until = until, size = size)
        }
    }

    private suspend fun getStaticMainFeed(
        setting: MainFeedSetting,
        until: Long,
        size: Int,
    ): List<MainEvent> {
        val rootPosts = getStaticRootPosts(setting = setting, until = until, size = size)
        val crossPosts = getStaticCrossPosts(setting = setting, until = until, size = size)

        val allCreatedAt = rootPosts.map { it.createdAt } + crossPosts.map { it.createdAt }
        val validCreatedAt = allCreatedAt.sortedDescending().take(size).toSet()

        val result = mutableListOf<MainEvent>()
        rootPosts.filter { validCreatedAt.contains(it.createdAt) }
            .forEach {
                result.add(
                    it.mapToRootPostUI(
                        forcedVotes = emptyMap(),
                        forcedFollows = emptyMap(),
                        forcedBookmarks = emptyMap(),
                        annotatedStringProvider = annotatedStringProvider
                    )
                )
            }
        crossPosts.filter { validCreatedAt.contains(it.createdAt) }
            .forEach {
                result.add(
                    it.mapToCrossPostUI(
                        forcedVotes = emptyMap(),
                        forcedFollows = emptyMap(),
                        forcedBookmarks = emptyMap(),
                        annotatedStringProvider = annotatedStringProvider
                    )
                )
            }

        return result.sortedByDescending { it.createdAt }
    }

    private suspend fun getStaticRootPosts(
        setting: MainFeedSetting,
        until: Long,
        size: Int,
    ): List<RootPostView> {
        return when (setting) {
            is HomeFeedSetting -> room.homeFeedDao().getHomeRootPosts(
                setting = setting,
                until = until,
                size = size
            )

            is TopicFeedSetting -> room.feedDao().getTopicRootPosts(
                topic = setting.topic,
                until = until,
                size = size
            )

            is ProfileFeedSetting -> room.feedDao().getProfileRootPosts(
                pubkey = setting.nprofile.publicKey().toHex(),
                until = until,
                size = size
            )

            is ListFeedSetting -> room.feedDao().getListRootPosts(
                identifier = setting.identifier,
                until = until,
                size = size
            )
        }
    }

    private suspend fun getStaticCrossPosts(
        setting: MainFeedSetting,
        until: Long,
        size: Int,
    ): List<CrossPostView> {
        return when (setting) {
            is HomeFeedSetting -> room.homeFeedDao().getHomeCrossPosts(
                setting = setting,
                until = until,
                size = size,
            )

            is TopicFeedSetting -> room.feedDao().getTopicCrossPosts(
                topic = setting.topic,
                until = until,
                size = size,
            )

            is ProfileFeedSetting -> room.feedDao().getProfileCrossPosts(
                pubkey = setting.nprofile.publicKey().toHex(),
                until = until,
                size = size,
            )

            is ListFeedSetting -> room.feedDao().getListCrossPosts(
                identifier = setting.identifier,
                until = until,
                size = size,
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