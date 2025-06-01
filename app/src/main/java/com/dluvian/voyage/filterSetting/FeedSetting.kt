package com.dluvian.voyage.filterSetting

import com.dluvian.voyage.Topic
import rust.nostr.sdk.Kind
import rust.nostr.sdk.Nip19Profile

sealed class FeedSetting

data class InboxFeedSetting(val pubkeySelection: FeedPubkeySelection) : FeedSetting()
data object BookmarksFeedSetting : FeedSetting()


sealed class MainFeedSetting : FeedSetting()

data class HomeFeedSetting(
    val pubkeySelection: FeedPubkeySelection,
    val withTopics: Boolean,
    val kinds: List<Kind>,
    val pageSize: ULong,
) : MainFeedSetting()

data class TopicFeedSetting(val topic: Topic) : MainFeedSetting()
data class ProfileFeedSetting(val nprofile: Nip19Profile) : MainFeedSetting()
data class ListFeedSetting(val identifier: String) : MainFeedSetting()
