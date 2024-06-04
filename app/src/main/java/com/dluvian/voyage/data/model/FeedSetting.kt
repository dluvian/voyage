package com.dluvian.voyage.data.model

import com.dluvian.voyage.core.PubkeyHex
import com.dluvian.voyage.core.Topic

sealed class FeedSetting

data object InboxFeedSetting : FeedSetting()
data object BookmarksFeedSetting : FeedSetting()
data class ReplyFeedSetting(val pubkey: PubkeyHex) : FeedSetting()


sealed class RootFeedSetting : FeedSetting()

data object HomeFeedSetting : RootFeedSetting()
data class TopicFeedSetting(val topic: Topic) : RootFeedSetting()
data class ProfileRootFeedSetting(val pubkey: PubkeyHex) : RootFeedSetting()
