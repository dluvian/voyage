package com.dluvian.voyage.filterSetting

import com.dluvian.voyage.Ident
import com.dluvian.voyage.Topic
import rust.nostr.sdk.Kind
import rust.nostr.sdk.PublicKey

sealed class FeedSetting(open val pageSize: ULong)

data class HomeFeedSetting(
    val pubkeySelection: PubkeySelection,
    val withTopics: Boolean,
    val kinds: List<Kind>,
    override val pageSize: ULong,
) : FeedSetting(pageSize = pageSize)

data class TopicFeedSetting(
    val topic: Topic,
    val kinds: List<Kind>,
    override val pageSize: ULong
) : FeedSetting(pageSize = pageSize)

data class ProfileFeedSetting(
    val pubkey: PublicKey,
    val kinds: List<Kind>,
    override val pageSize: ULong
) : FeedSetting(pageSize = pageSize)

data class InboxFeedSetting(
    val pubkeySelection: PubkeySelection,
    val kinds: List<Kind>,
    override val pageSize: ULong
) : FeedSetting(pageSize = pageSize)

data class ListFeedSetting(
    val ident: Ident,
    val kinds: List<Kind>,
    override val pageSize: ULong
) : FeedSetting(pageSize = pageSize)

data class BookmarkFeedSetting(override val pageSize: ULong) : FeedSetting(pageSize = pageSize)
