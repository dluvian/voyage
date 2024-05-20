package com.dluvian.voyage.data.provider

import com.dluvian.voyage.core.Topic
import com.dluvian.voyage.data.room.dao.PostDao
import com.dluvian.voyage.data.room.view.AdvancedProfileView
import com.dluvian.voyage.data.room.view.SimplePostView

class SearchProvider(
    private val topicProvider: TopicProvider,
    private val profileProvider: ProfileProvider,
    private val postDao: PostDao,
) {
    private val maxSearchResult = 10

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

    suspend fun getProfileSuggestions(text: String): List<AdvancedProfileView> {
        val stripped = text.stripSearchText()
        return profileProvider.getProfileByName(name = stripped, limit = maxSearchResult)
    }

    suspend fun getPostSuggestions(text: String): List<SimplePostView> {
        val stripped = text.stripSearchText()

        return postDao.getPostsByContent(content = stripped, limit = 3 * maxSearchResult)
    }

    fun getStrippedSearchText(text: String) = text.stripSearchText()

    private fun String.stripSearchText(): String {
        return this.trim().dropWhile { it == '#' || it == ' ' }.trim().lowercase()
    }
}
