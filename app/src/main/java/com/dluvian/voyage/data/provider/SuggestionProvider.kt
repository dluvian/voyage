package com.dluvian.voyage.data.provider

import com.dluvian.voyage.core.Topic
import com.dluvian.voyage.data.room.entity.ProfileEntity

class SuggestionProvider(
    private val topicProvider: TopicProvider,
    private val profileProvider: ProfileProvider
) {
    private val maxSearchResult = 5

    suspend fun getTopicSuggestions(text: String): List<Topic> {
        val stripped = text.stripSearchText()
        return topicProvider.getAllTopics()
            .asSequence()
            .filter { it.contains(other = stripped, ignoreCase = true) }
            .sortedBy { it.length }
            .distinctBy { it.lowercase() }
            .take(maxSearchResult)
            .toList()
    }

    suspend fun getProfileSuggestions(text: String): List<ProfileEntity> {
        val stripped = text.stripSearchText()
        return profileProvider.getProfileByName(name = stripped, limit = maxSearchResult)
    }

    fun getStrippedSearchText(text: String) = text.stripSearchText()

    private fun String.stripSearchText(): String {
        return this.trim().dropWhile { it == '#' || it == ' ' }.trim().lowercase()
    }
}
