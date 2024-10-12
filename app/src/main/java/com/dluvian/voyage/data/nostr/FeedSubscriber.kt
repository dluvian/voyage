package com.dluvian.voyage.data.nostr

import com.dluvian.voyage.core.MAX_EVENTS_TO_SUB
import com.dluvian.voyage.core.MAX_KEYS
import com.dluvian.voyage.core.Topic
import com.dluvian.voyage.core.utils.genericRepost
import com.dluvian.voyage.core.utils.limitRestricted
import com.dluvian.voyage.core.utils.replyKinds
import com.dluvian.voyage.core.utils.rootLikeKindsWithoutKTag
import com.dluvian.voyage.core.utils.syncedPutOrAdd
import com.dluvian.voyage.core.utils.takeRandom
import com.dluvian.voyage.core.utils.threadableKinds
import com.dluvian.voyage.data.account.IMyPubkeyProvider
import com.dluvian.voyage.data.model.FriendPubkeysNoLock
import com.dluvian.voyage.data.model.Global
import com.dluvian.voyage.data.model.HomeFeedSetting
import com.dluvian.voyage.data.model.InboxFeedSetting
import com.dluvian.voyage.data.model.ListPubkeys
import com.dluvian.voyage.data.model.ListTopics
import com.dluvian.voyage.data.model.NoPubkeys
import com.dluvian.voyage.data.model.PubkeySelection
import com.dluvian.voyage.data.model.TopicSelection
import com.dluvian.voyage.data.model.WebOfTrustPubkeys
import com.dluvian.voyage.data.provider.FriendProvider
import com.dluvian.voyage.data.provider.RelayProvider
import com.dluvian.voyage.data.provider.TopicProvider
import com.dluvian.voyage.data.room.dao.BookmarkDao
import rust.nostr.protocol.EventId
import rust.nostr.protocol.Filter
import rust.nostr.protocol.Nip19Profile
import rust.nostr.protocol.PublicKey
import rust.nostr.protocol.Timestamp

class FeedSubscriber(
    private val relayProvider: RelayProvider,
    private val topicProvider: TopicProvider,
    private val myPubkeyProvider: IMyPubkeyProvider,
    private val bookmarkDao: BookmarkDao,
    private val friendProvider: FriendProvider
) {
    suspend fun getHomeFeedSubscriptions(
        setting: HomeFeedSetting,
        until: ULong,
        since: ULong,
        limit: ULong
    ): Map<RelayUrl, List<Filter>> {
        return getPeopleAndTopicFeed(
            pubkeySelection = setting.pubkeySelection,
            topicSelection = setting.topicSelection,
            until = until,
            since = since,
            limit = limit
        )
    }

    suspend fun getListFeedSubscriptions(
        identifier: String,
        until: ULong,
        since: ULong,
        limit: ULong
    ): Map<RelayUrl, List<Filter>> {
        return getPeopleAndTopicFeed(
            pubkeySelection = ListPubkeys(identifier = identifier),
            topicSelection = ListTopics(identifier = identifier),
            until = until,
            since = since,
            limit = limit
        )
    }

    private suspend fun getPeopleAndTopicFeed(
        pubkeySelection: PubkeySelection,
        topicSelection: TopicSelection,
        until: ULong,
        since: ULong,
        limit: ULong
    ): Map<RelayUrl, List<Filter>> {
        if (limit <= 0u || since >= until) return emptyMap()

        val result = mutableMapOf<RelayUrl, MutableList<Filter>>()

        val sinceTimestamp = Timestamp.fromSecs(since)
        val untilTimestamp = Timestamp.fromSecs(until)

        val createBaseFilter = { pubkeys: List<PublicKey> ->
            Filter()
                .let { if (pubkeys.isNotEmpty()) it.authors(authors = pubkeys) else it }
                .since(timestamp = sinceTimestamp)
                .until(timestamp = untilTimestamp)
                .limitRestricted(limit = limit)
        }

        if (pubkeySelection !is NoPubkeys) {
                relayProvider
                    .getObserveRelays(selection = pubkeySelection)
                    .filter { (_, pubkeys) -> pubkeys.isNotEmpty() || pubkeySelection is Global }
                    .forEach { (relayUrl, pubkeys) ->
                        val publicKeys = pubkeys.takeRandom(MAX_KEYS).map { PublicKey.fromHex(it) }
                        val pubkeysNoteFilter = createBaseFilter(publicKeys)
                            .kinds(kinds = rootLikeKindsWithoutKTag)
                        val genericRepostFilter = createBaseFilter(publicKeys).genericRepost()
                        val filters = mutableListOf(pubkeysNoteFilter, genericRepostFilter)
                        result.syncedPutOrAdd(relayUrl, filters)
                    }
        }

        val topics = topicProvider.getTopicSelection(
            topicSelection = topicSelection,
            limit = MAX_KEYS
        )
        if (topics.isNotEmpty()) {
            val topicedNoteFilter = createBaseFilter(emptyList())
                .kinds(kinds = rootLikeKindsWithoutKTag)
                .hashtags(hashtags = topics)
            val genericRepostFilter = createBaseFilter(emptyList())
                .genericRepost()
                .hashtags(hashtags = topics)
            val filters = mutableListOf(topicedNoteFilter, genericRepostFilter)

            relayProvider.getReadRelays().forEach { relay ->
                result.syncedPutOrAdd(relay, filters)
            }
        }

        return result
    }

    fun getTopicFeedSubscription(
        topic: Topic,
        until: ULong,
        since: ULong,
        limit: ULong
    ): Map<RelayUrl, List<Filter>> {
        if (limit <= 0u || topic.isBlank() || since >= until) return emptyMap()

        val result = mutableMapOf<RelayUrl, MutableList<Filter>>()

        val topicedNoteFilter = Filter()
            .kinds(kinds = rootLikeKindsWithoutKTag)
            .hashtag(hashtag = topic)
            .since(timestamp = Timestamp.fromSecs(since))
            .until(timestamp = Timestamp.fromSecs(until))
            .limitRestricted(limit = limit)
        val genericRepostFilter = Filter()
            .genericRepost()
            .hashtag(hashtag = topic)
            .since(timestamp = Timestamp.fromSecs(since))
            .until(timestamp = Timestamp.fromSecs(until))
            .limitRestricted(limit = limit)
        val topicedNoteFilters = mutableListOf(topicedNoteFilter, genericRepostFilter)

        relayProvider.getReadRelays().forEach { relay ->
            result.syncedPutOrAdd(relay, topicedNoteFilters)
        }

        return result
    }

    suspend fun getProfileFeedSubscription(
        nprofile: Nip19Profile,
        until: ULong,
        since: ULong,
        limit: ULong
    ): Map<RelayUrl, List<Filter>> {
        return getPubkeyFeedSubscription(
            nprofile = nprofile,
            feedKind = MainFeed,
            until = until,
            since = since,
            limit = limit
        )
    }

    suspend fun getReplyFeedSubscription(
        nprofile: Nip19Profile,
        until: ULong,
        since: ULong,
        limit: ULong
    ): Map<RelayUrl, List<Filter>> {
        return getPubkeyFeedSubscription(
            nprofile = nprofile,
            feedKind = ReplyFeed,
            until = until,
            since = since,
            limit = limit
        )
    }

    private sealed class FeedKind
    private data object MainFeed : FeedKind()
    private data object ReplyFeed : FeedKind()

    private suspend fun getPubkeyFeedSubscription(
        nprofile: Nip19Profile,
        feedKind: FeedKind,
        until: ULong,
        since: ULong,
        limit: ULong
    ): Map<RelayUrl, List<Filter>> {
        if (limit <= 0u || since >= until) return emptyMap()

        val result = mutableMapOf<RelayUrl, MutableList<Filter>>()

        val createBaseFilter = {
            Filter()
                .author(author = nprofile.publicKey())
                .since(timestamp = Timestamp.fromSecs(since))
                .until(timestamp = Timestamp.fromSecs(until))
                .limitRestricted(limit = limit)
        }

        val pubkeyNoteFilter = createBaseFilter()
            .let {
                when (feedKind) {
                    MainFeed -> it.kinds(kinds = rootLikeKindsWithoutKTag)
                    ReplyFeed -> it.kinds(kinds = replyKinds)
                }
            }

        val filters = mutableListOf(pubkeyNoteFilter)

        when (feedKind) {
            MainFeed -> filters.add(createBaseFilter().genericRepost())
            ReplyFeed -> {}
        }

        relayProvider.getObserveRelays(nprofile = nprofile).forEach { relay ->
            val present = result.putIfAbsent(relay, filters)
            present?.addAll(filters)
        }

        return result
    }

    fun getInboxFeedSubscription(
        setting: InboxFeedSetting,
        until: ULong,
        since: ULong,
        limit: ULong
    ): Map<RelayUrl, List<Filter>> {
        if (limit <= 0u || since >= until) return emptyMap()

        val pubkeys = when (setting.pubkeySelection) {
            FriendPubkeysNoLock -> friendProvider.getFriendPubkeysNoLock()
            Global -> null
            WebOfTrustPubkeys -> null
            NoPubkeys -> return emptyMap()
        }?.map { PublicKey.fromHex(it) }

        val mentionFilter = Filter()
            .kinds(kinds = threadableKinds)
            .pubkey(pubkey = myPubkeyProvider.getPublicKey())
            .since(timestamp = Timestamp.fromSecs(since))
            .until(timestamp = Timestamp.fromSecs(until))
            .limitRestricted(limit = MAX_EVENTS_TO_SUB)

        return relayProvider.getReadRelays()
            .associateWith {
                val filter = if (pubkeys != null) {
                    mentionFilter.authors(pubkeys.takeRandom(MAX_KEYS))
                } else mentionFilter

                listOf(filter)
            }
    }

    suspend fun getBookmarksFeedSubscription(
        until: ULong,
        since: ULong,
        limit: ULong
    ): Map<RelayUrl, List<Filter>> {
        if (limit <= 0u) return emptyMap()

        val ids = bookmarkDao.getUnknownBookmarks()
            .takeRandom(MAX_KEYS)
            .map { EventId.fromHex(it) }

        if (ids.isEmpty()) return emptyMap()

        val bookedMarkedNotesFilter = Filter()
            .kinds(kinds = threadableKinds) // We don't support bookmarking the repost itself
            .ids(ids = ids)
            .since(timestamp = Timestamp.fromSecs(since))
            .until(timestamp = Timestamp.fromSecs(until))
            .limitRestricted(limit = limit)
        val filters = mutableListOf(bookedMarkedNotesFilter)

        return relayProvider.getReadRelays().associateWith { filters }
    }
}
