package com.dluvian.voyage.core.utils

import com.dluvian.voyage.core.MAX_DESCRIPTION_LEN
import com.dluvian.voyage.core.MAX_NAME_LEN
import com.dluvian.voyage.core.MAX_SUBJECT_LEN
import com.dluvian.voyage.core.MAX_TOPIC_LEN
import com.dluvian.voyage.core.Topic
import com.dluvian.voyage.data.nostr.getDescription
import com.dluvian.voyage.data.nostr.getTitle
import rust.nostr.sdk.Event
import rust.nostr.sdk.Metadata

fun String.normalizeTitle() = this.trim().take(MAX_SUBJECT_LEN).trim()

fun String.normalizeDescription() = this.trim().take(MAX_DESCRIPTION_LEN).trim()

fun Event.getNormalizedTitle() = this.getTitle()?.normalizeTitle().orEmpty()

fun Event.getNormalizedDescription() = this.getDescription()?.normalizeDescription().orEmpty()

fun Topic.normalizeTopic(): Topic {
    return this.trim()
        .dropWhile { it == '#' || it.isWhitespace() }
        .take(MAX_TOPIC_LEN)
        .lowercase()
}

private fun List<Topic>.normalizeTopics(): List<Topic> {
    return this.map { it.normalizeTopic() }
        .filter { it.isBareTopicStr() }
        .distinct()
}

fun Event.getNormalizedTopics(limit: Int = Int.MAX_VALUE): List<Topic> {
    return this.tags()
        .hashtags()
        .normalizeTopics()
        .take(limit)
}

fun normalizeName(str: String) = str.filterNot { it.isWhitespace() }.take(MAX_NAME_LEN)

fun Metadata.getNormalizedName(): String {
    val name = this.getName().orEmpty().ifBlank { this.getDisplayName() }.orEmpty()
    return normalizeName(str = name)
}
