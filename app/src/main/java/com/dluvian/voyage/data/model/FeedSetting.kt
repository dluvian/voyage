package com.dluvian.voyage.data.model

import com.dluvian.voyage.core.Topic

sealed class FeedSetting
data object HomeFeedSetting : FeedSetting()
data class TopicFeedSetting(val topic: Topic) : FeedSetting()
