package com.dluvian.voyage.data.model

import com.dluvian.voyage.core.Topic

sealed class FeedSettings
data object HomeFeedSetting : FeedSettings()
data class TopicFeedSetting(val topic: Topic) : FeedSettings()
