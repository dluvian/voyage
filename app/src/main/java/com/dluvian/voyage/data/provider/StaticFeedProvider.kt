package com.dluvian.voyage.data.provider

import com.dluvian.voyage.core.model.MainEvent
import com.dluvian.voyage.core.model.SomeReply
import com.dluvian.voyage.core.utils.mergeToMainEventUIList
import com.dluvian.voyage.core.utils.mergeToSomeReplyUIList
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
import com.dluvian.voyage.data.room.view.PollView
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
            BookmarksFeedSetting -> getStaticBookmarkFeed(until = until, size = size)
        }
            // Some query is buggy and returns duplicates.
            // TODO: Will be fixed once we move to in-memory view instead of room-view
            .distinctBy { it.id }
    }

    private suspend fun getStaticMainFeed(
        setting: MainFeedSetting,
        until: Long,
        size: Int,
    ): List<MainEvent> {
        return mergeToMainEventUIList(
            roots = getStaticRootPosts(setting = setting, until = until, size = size),
            crossPosts = getStaticCrossPosts(setting = setting, until = until, size = size),
            polls = getStaticPolls(setting = setting, until = until, size = size),
            pollOptions = emptyList(),
            legacyReplies = emptyList(),
            comments = emptyList(),
            votes = emptyMap(),
            follows = emptyMap(),
            bookmarks = emptyMap(),
            size = size,
            annotatedStringProvider = annotatedStringProvider
        )
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

    private suspend fun getStaticPolls(
        setting: MainFeedSetting,
        until: Long,
        size: Int,
    ): List<PollView> {
        return when (setting) {
            is HomeFeedSetting -> room.homeFeedDao().getHomePolls(
                setting = setting,
                until = until,
                size = size,
            )

            is TopicFeedSetting -> room.feedDao().getTopicPolls(
                topic = setting.topic,
                until = until,
                size = size,
            )

            is ProfileFeedSetting -> room.feedDao().getProfilePolls(
                pubkey = setting.nprofile.publicKey().toHex(),
                until = until,
                size = size,
            )

            is ListFeedSetting -> room.feedDao().getListPolls(
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
    ): List<SomeReply> {
        return mergeToSomeReplyUIList(
            legacyReplies = room.legacyReplyDao().getProfileReplies(
                pubkey = setting.nprofile.publicKey().toHex(),
                until = until,
                size = size
            ),
            comments = room.commentDao().getProfileComments(
                pubkey = setting.nprofile.publicKey().toHex(),
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

    private suspend fun getStaticInboxFeed(
        setting: InboxFeedSetting,
        until: Long,
        size: Int
    ): List<MainEvent> {
        return mergeToMainEventUIList(
            roots = room.inboxDao().getInboxRoots(
                setting = setting,
                until = until,
                size = size
            ),
            crossPosts = emptyList(),
            polls = room.inboxDao().getInboxPolls(
                setting = setting,
                until = until,
                size = size
            ),
            pollOptions = emptyList(),
            legacyReplies = room.inboxDao().getInboxReplies(
                setting = setting,
                until = until,
                size = size
            ),
            comments = room.inboxDao().getInboxComments(
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

    private suspend fun getStaticBookmarkFeed(until: Long, size: Int): List<MainEvent> {
        return mergeToMainEventUIList(
            roots = room.bookmarkDao().getRootPosts(until = until, size = size),
            crossPosts = emptyList(),
            polls = room.bookmarkDao().getPolls(until = until, size = size),
            pollOptions = emptyList(),
            legacyReplies = room.bookmarkDao().getReplies(until = until, size = size),
            comments = room.bookmarkDao().getComments(until = until, size = size),
            votes = emptyMap(),
            follows = emptyMap(),
            bookmarks = emptyMap(),
            size = size,
            annotatedStringProvider = annotatedStringProvider
        )
    }
}