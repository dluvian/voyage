package com.dluvian.voyage.data.nostr

import android.util.Log
import com.dluvian.voyage.core.MAX_EVENTS_TO_SUB
import com.dluvian.voyage.core.MAX_KEYS
import com.dluvian.voyage.core.Topic
import com.dluvian.voyage.core.utils.limitRestricted
import com.dluvian.voyage.core.utils.syncedPutOrAdd
import com.dluvian.voyage.core.utils.takeRandom
import com.dluvian.voyage.core.utils.textNoteAndRepostKinds
import com.dluvian.voyage.data.account.IMyPubkeyProvider
import com.dluvian.voyage.data.model.FriendPubkeys
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import rust.nostr.protocol.EventId
import rust.nostr.protocol.Filter
import rust.nostr.protocol.Kind
import rust.nostr.protocol.KindEnum
import rust.nostr.protocol.Nip19Profile
import rust.nostr.protocol.PublicKey
import rust.nostr.protocol.Timestamp

private const val TAG = "NostrFeedSubscriber"

class NostrFeedSubscriber(
    private val scope: CoroutineScope,
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

        val peopleJob = if (pubkeySelection !is NoPubkeys) {
            scope.launch {
                relayProvider
                    .getObserveRelays(selection = pubkeySelection)
                    .filter { (_, pubkeys) -> pubkeys.isNotEmpty() || pubkeySelection is Global }
                    .forEach { (relayUrl, pubkeys) ->
                        val publicKeys = pubkeys.takeRandom(MAX_KEYS).map { PublicKey.fromHex(it) }
                        val pubkeysNoteFilter = Filter()
                            .kinds(kinds = textNoteAndRepostKinds)
                            .apply { if (publicKeys.isNotEmpty()) authors(authors = publicKeys) }
                            .since(timestamp = sinceTimestamp)
                            .until(timestamp = untilTimestamp)
                            .limitRestricted(limit = limit)
                        val pubkeysNoteFilters = mutableListOf(pubkeysNoteFilter)
                        result.syncedPutOrAdd(relayUrl, pubkeysNoteFilters)
                    }
            }
        } else null

        val topics = topicProvider.getTopicSelection(
            topicSelection = topicSelection,
            limit = MAX_KEYS
        )
        if (topics.isNotEmpty()) {
            val topicedNoteFilter = Filter()
                .kinds(kinds = textNoteAndRepostKinds)
                .hashtags(hashtags = topics)
                .since(timestamp = sinceTimestamp)
                .until(timestamp = untilTimestamp)
                .limitRestricted(limit = limit)
            val topicedNoteFilters = mutableListOf(topicedNoteFilter)

            relayProvider.getReadRelays().forEach { relay ->
                result.syncedPutOrAdd(relay, topicedNoteFilters)
            }
        }

        peopleJob?.join()

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
            .kinds(kinds = textNoteAndRepostKinds)
            .hashtag(hashtag = topic)
            .since(timestamp = Timestamp.fromSecs(since))
            .until(timestamp = Timestamp.fromSecs(until))
            .limitRestricted(limit = limit)
        val topicedNoteFilters = mutableListOf(topicedNoteFilter)

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
        if (limit <= 0u || since >= until) return emptyMap()

        val result = mutableMapOf<RelayUrl, MutableList<Filter>>()

        val pubkeyNoteFilter = Filter()
            .kinds(kinds = textNoteAndRepostKinds)
            .author(author = nprofile.publicKey())
            .since(timestamp = Timestamp.fromSecs(since))
            .until(timestamp = Timestamp.fromSecs(until))
            .limitRestricted(limit = limit)
        val pubkeyNoteFilters = mutableListOf(pubkeyNoteFilter)

        relayProvider.getObserveRelays(nprofile = nprofile).forEach { relay ->
            val present = result.putIfAbsent(relay, pubkeyNoteFilters)
            present?.addAll(pubkeyNoteFilters)
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
            FriendPubkeys -> friendProvider.getFriendPubkeys()
            Global -> null
            WebOfTrustPubkeys -> null
            NoPubkeys -> return emptyMap()
        }?.map { PublicKey.fromHex(it) }

        val mentionFilter = Filter()
            .kind(kind = Kind.fromEnum(KindEnum.TextNote))
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
        Log.d(TAG, "getBookmarksFeedSubscription")

        val ids = bookmarkDao.getUnknownBookmarks()
            .takeRandom(MAX_KEYS)
            .map { EventId.fromHex(it) }

        if (ids.isEmpty()) return emptyMap()

        val bookedMarkedNotesFilter = Filter()
            .kind(kind = Kind.fromEnum(KindEnum.TextNote)) // We don't support bookmarking the repost itself
            .events(ids = ids)
            .since(timestamp = Timestamp.fromSecs(since))
            .until(timestamp = Timestamp.fromSecs(until))
            .limitRestricted(limit = limit)
        val filters = mutableListOf(bookedMarkedNotesFilter)

        return relayProvider.getReadRelays().associateWith { filters }
    }
}
