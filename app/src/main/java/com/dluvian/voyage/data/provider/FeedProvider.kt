package com.dluvian.voyage.data.provider

import androidx.compose.runtime.State
import com.dluvian.voyage.core.EventIdHex
import com.dluvian.voyage.core.PubkeyHex
import com.dluvian.voyage.core.SHORT_DEBOUNCE
import com.dluvian.voyage.core.model.LegacyReply
import com.dluvian.voyage.core.model.MainEvent
import com.dluvian.voyage.core.model.RootPost
import com.dluvian.voyage.core.utils.containsNoneIgnoreCase
import com.dluvian.voyage.core.utils.firstThenDistinctDebounce
import com.dluvian.voyage.core.utils.mergeToParentUIList
import com.dluvian.voyage.data.event.OldestUsedEvent
import com.dluvian.voyage.data.model.BookmarksFeedSetting
import com.dluvian.voyage.data.model.FeedSetting
import com.dluvian.voyage.data.model.HomeFeedSetting
import com.dluvian.voyage.data.model.InboxFeedSetting
import com.dluvian.voyage.data.model.ListFeedSetting
import com.dluvian.voyage.data.model.ProfileRootFeedSetting
import com.dluvian.voyage.data.model.ReplyFeedSetting
import com.dluvian.voyage.data.model.RootFeedSetting
import com.dluvian.voyage.data.model.TopicFeedSetting
import com.dluvian.voyage.data.nostr.NostrSubscriber
import com.dluvian.voyage.data.room.AppDatabase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onEach

class FeedProvider(
    private val nostrSubscriber: NostrSubscriber,
    private val room: AppDatabase,
    private val oldestUsedEvent: OldestUsedEvent,
    private val annotatedStringProvider: AnnotatedStringProvider,
    private val forcedVotes: Flow<Map<EventIdHex, Boolean>>,
    private val forcedFollows: Flow<Map<PubkeyHex, Boolean>>,
    private val forcedBookmarks: Flow<Map<EventIdHex, Boolean>>,
    private val muteProvider: MuteProvider,
    private val showAuthorName: State<Boolean>,
) {
    private val staticFeedProvider = StaticFeedProvider(
        room = room,
        annotatedStringProvider = annotatedStringProvider
    )

    suspend fun getStaticFeed(
        until: Long,
        size: Int,
        setting: FeedSetting,
    ): List<MainEvent> {
        return staticFeedProvider.getStaticFeed(
            until = until,
            size = size,
            setting = setting
        )
    }

    suspend fun getFeedFlow(
        until: Long,
        subUntil: Long,
        size: Int,
        setting: FeedSetting,
        forceSubscription: Boolean,
        subscribe: Boolean = true,
    ): Flow<List<MainEvent>> {
        if (subscribe) {
            nostrSubscriber.subFeed(
                until = subUntil,
                limit = size,
                setting = setting,
                forceSubscription = forceSubscription
            )
        }
        val mutedWords = muteProvider.getMutedWords()

        return when (setting) {
            is RootFeedSetting -> getRootFeedFlow(
                until = until,
                size = size,
                setting = setting
            )

            is ReplyFeedSetting -> getReplyFeedFlow(setting = setting, until = until, size = size)

            is InboxFeedSetting -> getInboxFeedFlow(setting = setting, until = until, size = size)

            BookmarksFeedSetting -> getBookmarksFeedFlow(until = until, size = size)
        }
            .firstThenDistinctDebounce(SHORT_DEBOUNCE)
            .onEach { posts ->
                oldestUsedEvent.updateOldestCreatedAt(posts.minOfOrNull { it.createdAt })
                nostrSubscriber.subVotesAndReplies(
                    parentIds = posts.filter { it.replyCount == 0 && it.upvoteCount == 0 }
                        .filter { it.content.text.containsNoneIgnoreCase(strs = mutedWords) }
                        .map { it.getRelevantId() }
                )
                if (showAuthorName.value) {
                    nostrSubscriber.subProfiles(
                        pubkeys = posts.filter { it.authorName.isNullOrEmpty() }.map { it.pubkey }
                    )
                }
            }
    }

    private fun getRootFeedFlow(
        until: Long,
        size: Int,
        setting: RootFeedSetting,
    ): Flow<List<RootPost>> {
        val flow = when (setting) {
            is HomeFeedSetting -> room.homeFeedDao().getHomeRootPostFlow(
                setting = setting,
                until = until,
                size = size
            )

            // TODO: crossPosts
            is TopicFeedSetting -> room.rootPostDao().getTopicRootPostFlow(
                topic = setting.topic,
                until = until,
                size = size
            )

            // TODO: crossPosts
            is ProfileRootFeedSetting -> room.rootPostDao().getProfileRootPostFlow(
                pubkey = setting.nprofile.publicKey().toHex(),
                until = until,
                size = size
            )

            // TODO: crossPosts
            is ListFeedSetting -> room.rootPostDao().getListRootPostFlow(
                identifier = setting.identifier,
                until = until,
                size = size
            )
        }

        return combine(
            flow.firstThenDistinctDebounce(SHORT_DEBOUNCE),
            forcedVotes,
            forcedFollows,
            forcedBookmarks,
        ) { posts, votes, follows, bookmarks ->
            posts.map {
                it.mapToRootPostUI(
                    forcedVotes = votes,
                    forcedFollows = follows,
                    forcedBookmarks = bookmarks,
                    annotatedStringProvider = annotatedStringProvider
                )
            }
        }
    }

    private fun getReplyFeedFlow(
        setting: ReplyFeedSetting,
        until: Long,
        size: Int,
    ): Flow<List<LegacyReply>> {
        val flow = room.replyDao().getProfileReplyFlow(
            pubkey = setting.nprofile.publicKey().toHex(),
            until = until,
            size = size
        )

        return combine(
            flow.firstThenDistinctDebounce(SHORT_DEBOUNCE),
            forcedVotes,
            forcedFollows,
            forcedBookmarks,
        ) { posts, votes, follows, bookmarks ->
            posts.map {
                it.mapToLegacyReplyUI(
                    forcedVotes = votes,
                    forcedFollows = follows,
                    forcedBookmarks = bookmarks,
                    annotatedStringProvider = annotatedStringProvider
                )
            }
        }
    }

    private fun getInboxFeedFlow(
        setting: InboxFeedSetting,
        until: Long,
        size: Int
    ): Flow<List<MainEvent>> {
        return combine(
            room.inboxDao()
                .getInboxReplyFlow(setting = setting, until = until, size = size)
                .firstThenDistinctDebounce(SHORT_DEBOUNCE),
            room.inboxDao()
                .getMentionRootFlow(setting = setting, until = until, size = size)
                .firstThenDistinctDebounce(SHORT_DEBOUNCE),
            forcedVotes,
            forcedFollows,
            forcedBookmarks,
        ) { replies, roots, votes, follows, bookmarks ->
            mergeToParentUIList(
                replies = replies,
                roots = roots,
                votes = votes,
                follows = follows,
                bookmarks = bookmarks,
                size = size,
                annotatedStringProvider = annotatedStringProvider
            )
        }
    }

    private fun getBookmarksFeedFlow(until: Long, size: Int): Flow<List<MainEvent>> {
        return combine(
            room.bookmarkDao()
                .getReplyFlow(until = until, size = size)
                .firstThenDistinctDebounce(SHORT_DEBOUNCE),
            room.bookmarkDao()
                .getRootPostsFlow(until = until, size = size)
                .firstThenDistinctDebounce(SHORT_DEBOUNCE),
            forcedVotes,
            forcedFollows,
            forcedBookmarks,
        ) { replies, rootPosts, votes, follows, bookmarks ->
            mergeToParentUIList(
                replies = replies,
                roots = rootPosts,
                votes = votes,
                follows = follows,
                bookmarks = bookmarks,
                size = size,
                annotatedStringProvider = annotatedStringProvider
            )
        }
    }

    fun settingHasPostsFlow(setting: FeedSetting): Flow<Boolean> {
        return when (setting) {
            is HomeFeedSetting -> room.homeFeedDao().hasHomeRootPostsFlow(setting = setting)
            is TopicFeedSetting -> room.rootPostDao().hasTopicRootPostsFlow(topic = setting.topic)
            is ProfileRootFeedSetting -> room.rootPostDao()
                .hasProfileRootPostsFlow(pubkey = setting.nprofile.publicKey().toHex())

            is ReplyFeedSetting -> room.replyDao()
                .hasProfileRepliesFlow(pubkey = setting.nprofile.publicKey().toHex())

            is ListFeedSetting -> room.rootPostDao()
                .hasListRootPostsFlow(identifier = setting.identifier)

            is InboxFeedSetting -> room.inboxDao().hasInboxFlow(setting = setting)

            BookmarksFeedSetting -> room.bookmarkDao().hasBookmarkedPostsFlow()

        }
    }
}
