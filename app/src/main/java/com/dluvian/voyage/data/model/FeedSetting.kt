package com.dluvian.voyage.data.model

import com.dluvian.voyage.core.Topic
import rust.nostr.protocol.Nip19Profile

sealed class FeedSetting

data object InboxFeedSetting : FeedSetting()
data object BookmarksFeedSetting : FeedSetting()
data class ReplyFeedSetting(val nprofile: Nip19Profile) : FeedSetting()


sealed class RootFeedSetting : FeedSetting()

data class HomeFeedSetting(
    val topicSelection: HomeFeedTopicSelection,
    val pubkeySelection: HomeFeedPubkeySelection,
) : RootFeedSetting()

data class TopicFeedSetting(val topic: Topic) : RootFeedSetting()
data class ProfileRootFeedSetting(val nprofile: Nip19Profile) : RootFeedSetting()
data class ListFeedSetting(val identifier: String) : RootFeedSetting()
