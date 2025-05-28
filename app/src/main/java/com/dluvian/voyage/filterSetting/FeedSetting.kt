package com.dluvian.voyage.filterSetting

import com.dluvian.voyage.core.Topic
import com.dluvian.voyage.data.filterSetting.FeedPubkeySelection
import com.dluvian.voyage.data.filterSetting.HomeFeedTopicSelection
import rust.nostr.sdk.Nip19Profile

sealed class FeedSetting

data class InboxFeedSetting(val pubkeySelection: FeedPubkeySelection) : FeedSetting()
data object BookmarksFeedSetting : FeedSetting()


sealed class MainFeedSetting : FeedSetting()

data class HomeFeedSetting(
    val topicSelection: HomeFeedTopicSelection,
    val pubkeySelection: FeedPubkeySelection,
    val showRoots: Boolean,
    val showCrossPosts: Boolean,
) : MainFeedSetting()

data class TopicFeedSetting(val topic: Topic) : MainFeedSetting()
data class ProfileFeedSetting(val nprofile: Nip19Profile) : MainFeedSetting()
data class ListFeedSetting(val identifier: String) : MainFeedSetting()
