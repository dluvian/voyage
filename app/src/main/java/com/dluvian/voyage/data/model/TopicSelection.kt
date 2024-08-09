package com.dluvian.voyage.data.model

sealed class TopicSelection {
    fun isMyTopics(): Boolean {
        return when (this) {
            MyTopics -> true
            NoTopics, is ListTopics -> false
        }
    }
}

data object NoTopics : TopicSelection()
data object MyTopics : TopicSelection()
data class ListTopics(val identifier: String) : TopicSelection()
