package com.dluvian.voyage.core.model

import com.dluvian.voyage.core.Topic

data class TopicFollowState(val topic: Topic, val isFollowed: Boolean)
data class TopicMuteState(val topic: Topic, val isMuted: Boolean)
