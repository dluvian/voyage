package com.dluvian.voyage.data.model

sealed class TopicSelection
data object MyTopics : TopicSelection()
data class ListTopics(val identifier: String) : TopicSelection()
