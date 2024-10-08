package com.dluvian.voyage.data.model

import com.dluvian.voyage.core.Topic
import rust.nostr.protocol.Nip19Profile

sealed class FeedSetting

data class InboxFeedSetting(val pubkeySelection: FeedPubkeySelection) : FeedSetting()
data object BookmarksFeedSetting : FeedSetting()
data class ReplyFeedSetting(val nprofile: Nip19Profile) : FeedSetting()


sealed class MainFeedSetting : FeedSetting()

data class HomeFeedSetting(
    val topicSelection: HomeFeedTopicSelection,
    val pubkeySelection: FeedPubkeySelection,
) : MainFeedSetting()

data class TopicFeedSetting(val topic: Topic) : MainFeedSetting()
data class ProfileFeedSetting(val nprofile: Nip19Profile) : MainFeedSetting()
data class ListFeedSetting(val identifier: String) : MainFeedSetting()
