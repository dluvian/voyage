package com.dluvian.voyage.data.model

import com.dluvian.voyage.core.PubkeyHex
import com.dluvian.voyage.core.Topic

sealed class FeedSetting

sealed class RootFeedSetting : FeedSetting()
sealed class ReplyFeedSetting : FeedSetting()

data object HomeFeedSetting : RootFeedSetting()
data class TopicFeedSetting(val topic: Topic) : RootFeedSetting()
data class ProfileRootFeedSetting(val pubkey: PubkeyHex) : RootFeedSetting()
data class ProfileReplyFeedSetting(val pubkey: PubkeyHex) : ReplyFeedSetting()
