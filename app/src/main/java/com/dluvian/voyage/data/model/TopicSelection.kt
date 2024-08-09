package com.dluvian.voyage.data.model

sealed class TopicSelection

sealed class HomeFeedTopicSelection : TopicSelection() {
    fun isMyTopics(): Boolean {
        return when (this) {
            MyTopics -> true
            NoTopics -> false
        }
    }
}

data object NoTopics : HomeFeedTopicSelection()
data object MyTopics : HomeFeedTopicSelection()
data class ListTopics(val identifier: String) : TopicSelection()
