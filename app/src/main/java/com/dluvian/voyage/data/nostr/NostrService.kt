package com.dluvian.voyage.data.nostr

import android.util.Log
import com.dluvian.voyage.core.EventIdHex
import com.dluvian.voyage.core.MAX_KEYS_SQL
import com.dluvian.voyage.core.PubkeyHex
import com.dluvian.voyage.core.Topic
import com.dluvian.voyage.data.KeyStore
import com.dluvian.voyage.data.event.EventMaker
import com.dluvian.voyage.data.preferences.EventPreferences
import com.dluvian.voyage.data.preferences.RelayPreferences
import rust.nostr.sdk.ClientBuilder
import rust.nostr.sdk.Coordinate
import rust.nostr.sdk.Event
import rust.nostr.sdk.EventId
import rust.nostr.sdk.Metadata
import rust.nostr.sdk.Options
import rust.nostr.sdk.PublicKey

private const val TAG = "NostrService"

class NostrService(
    relayPreferences: RelayPreferences,
    eventPreferences: EventPreferences,
) {
    private val keyStore = KeyStore()

    // Issue: Turn gossip on/off in setttings
    // TODO: Mention that auth setting only applies after restarting the app
    private val clientOpts = Options()
        .gossip(true)
        .automaticAuthentication(relayPreferences.getSendAuth())

    private val nostrClient = ClientBuilder()
        .signer(keyStore.activeSigner())
        .opts(clientOpts)
        .build()

    private val eventMaker = EventMaker(eventPreferences = eventPreferences)

    suspend fun initialize(initRelayUrls: Collection<RelayUrl>) {
        initRelayUrls.forEach { relay -> nostrClient.addRelay(relay) }
        nostrClient.connect()
    }

    suspend fun publishJson(eventJson: String): Result<Event> {
        return runCatching { Event.fromJson(json = eventJson) }
            .onSuccess { event -> nostrClient.sendEvent(event) }
            .onFailure {
                Log.w(TAG, "Failed to deserialize $eventJson")
            }
    }

    suspend fun publishPost(
        content: String,
        topics: List<Topic>,
        mentions: List<PubkeyHex>,
        quotes: List<String>,
    ): Result<Event> {
        return eventMaker.buildPost(
            content = content,
            topics = topics,
            mentions = mentions,
            quotes = quotes,
        )
            .onSuccess { nostrClient.sendEvent(event = it) }
    }

    suspend fun publishComment(
        content: String,
        parent: Event,
        mentions: List<PubkeyHex>,
        quotes: List<String>,
        topics: List<Topic>,
        relayHint: RelayUrl?,
    ): Result<Event> {
        return eventMaker.buildComment(
            parent = parent,
            mentions = mentions,
            quotes = quotes,
            topics = topics,
            relayHint = relayHint,
            content = content,
        )
            .onSuccess { nostrClient.sendEvent(event = it) }
    }

    suspend fun publishCrossPost(
        crossPostedEvent: Event,
        topics: List<Topic>,
        relayHint: RelayUrl,
    ): Result<Event> {
        return eventMaker.buildCrossPost(
            crossPostedEvent = crossPostedEvent,
            topics = topics,
            relayHint = relayHint,
        )
            .onSuccess { nostrClient.sendEvent(event = it) }
    }

    suspend fun publishVote(
        eventId: EventId,
        content: String,
        mention: PublicKey,
    ): Result<Event> {
        return eventMaker.buildVote(
            eventId = eventId,
            content = content,
            mention = mention,
        )
            .onSuccess { nostrClient.sendEvent(event = it) }
    }

    suspend fun publishDelete(
        eventId: EventId,
    ): Result<Event> {
        return eventMaker.buildDelete(eventId = eventId)
            .onSuccess { nostrClient.sendEvent(event = it) }
    }

    suspend fun publishListDeletion(
        identifier: String,
    ): Result<Event> {
        return eventMaker.buildListDelete(identifier = identifier)
            .onSuccess { nostrClient.sendEvent(event = it) }
    }

    suspend fun publishTopicList(
        topics: List<Topic>,
    ): Result<Event> {
        if (topics.size > MAX_KEYS_SQL) {
            return Result.failure(IllegalArgumentException("Too many topics"))
        }

        return eventMaker.buildTopicList(topics = topics)
            .onSuccess { nostrClient.sendEvent(event = it) }
    }

    suspend fun publishBookmarkList(
        postIds: List<EventIdHex>,
    ): Result<Event> {
        if (postIds.size > MAX_KEYS_SQL) {
            return Result.failure(IllegalArgumentException("Too many bookmarks"))
        }

        return eventMaker.buildBookmarkList(postIds = postIds)
            .onSuccess { nostrClient.sendEvent(event = it) }
    }

    suspend fun publishContactList(
        pubkeys: List<PubkeyHex>,
    ): Result<Event> {
        if (pubkeys.size > MAX_KEYS_SQL) {
            return Result.failure(IllegalArgumentException("Too many contacts"))
        }

        return eventMaker.buildContactList(pubkeys = pubkeys)
            .onSuccess { nostrClient.sendEvent(event = it) }
    }

    suspend fun publishNip65(
        relays: List<Nip65Relay>,
    ): Result<Event> {
        return eventMaker.buildNip65(relays = relays)
            .onSuccess { nostrClient.sendEvent(event = it) }
    }

    suspend fun publishProfileSet(
        identifier: String,
        title: String,
        description: String,
        pubkeys: List<PublicKey>,
    ): Result<Event> {
        if (pubkeys.size > MAX_KEYS_SQL) {
            return Result.failure(IllegalArgumentException("Too many profiles"))
        }

        return eventMaker.buildProfileSet(
            identifier = identifier,
            title = title,
            description = description,
            pubkeys = pubkeys
        )
            .onSuccess { nostrClient.sendEvent(event = it) }
    }

    suspend fun publishTopicSet(
        identifier: String,
        title: String,
        description: String,
        topics: List<Topic>,
    ): Result<Event> {
        if (topics.size > MAX_KEYS_SQL) {
            return Result.failure(IllegalArgumentException("Too many topics"))
        }

        return eventMaker.buildTopicSet(
            identifier = identifier,
            title = title,
            description = description,
            topics = topics
        )
            .onSuccess { nostrClient.sendEvent(event = it) }
    }

    suspend fun publishProfile(
        metadata: Metadata,
    ): Result<Event> {
        return eventMaker.buildProfile(metadata = metadata)
            .onSuccess { nostrClient.sendEvent(event = it) }
    }

    suspend fun publishGitIssue(
        repoCoordinate: Coordinate,
        title: String,
        content: String,
        label: String,
        mentions: List<PubkeyHex>,
        quotes: List<String>,
    ): Result<Event> {
        return eventMaker.buildGitIssue(
            repoCoordinate = repoCoordinate,
            title = title,
            content = content,
            label = label,
            mentions = mentions,
            quotes = quotes,
        )
            .onSuccess { nostrClient.sendEvent(event = it) }
    }

    suspend fun addRelay(relayUrl: String) {
        nostrClient.addRelay(url = relayUrl)
    }

    fun close() {
        nostrClient.close()
    }
}
