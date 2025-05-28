package com.dluvian.voyage

import com.dluvian.voyage.preferences.EventPreferences
import com.dluvian.voyage.preferences.RelayPreferences
import rust.nostr.sdk.ClientBuilder
import rust.nostr.sdk.Coordinate
import rust.nostr.sdk.Event
import rust.nostr.sdk.EventBuilder
import rust.nostr.sdk.EventDeletionRequest
import rust.nostr.sdk.EventId
import rust.nostr.sdk.Filter
import rust.nostr.sdk.Kind
import rust.nostr.sdk.KindStandard
import rust.nostr.sdk.Metadata
import rust.nostr.sdk.NostrDatabase
import rust.nostr.sdk.Options
import rust.nostr.sdk.PublicKey
import rust.nostr.sdk.SendEventOutput
import rust.nostr.sdk.Tag
import rust.nostr.sdk.TagStandard
import rust.nostr.sdk.Timestamp

// TODO: No RelayUrl struct in nostr-sdk ?
typealias ReadRelays = List<String>
typealias WriteRelays = List<String>

private const val TAG = "NostrService"

class NostrService(
    private val relayPreferences: RelayPreferences,
    private val eventPreferences: EventPreferences,
    keyStore: KeyStore,
) {
    val clientOpts = Options()
        .gossip(true)
        .automaticAuthentication(relayPreferences.getSendAuth())

    val client = ClientBuilder()
        .signer(keyStore.getSigner())
        .opts(clientOpts)
        .database(NostrDatabase.lmdb("voyage_db"))
        .admitPolicy(RelayChecker())
        .build()

    suspend fun init() {
        val (read, write) = getPersonalRelays()
        // TODO: What if the same relay is present in read and write?
        read.forEach { relay -> client.addReadRelay(relay) }
        write.forEach { relay -> client.addWriteRelay(relay) }

        client.connect()
    }

    private suspend fun getPersonalRelays(): Pair<ReadRelays, WriteRelays> {
        val filter = Filter()
            .author(client.signer().getPublicKey())
            .kind(Kind.fromStd(KindStandard.RELAY_LIST))
            .limit(1u)
        val event = client.database().query(filter).first()

        TODO("Read nip65 or return default")
    }

    suspend fun publishPost(subject: String, content: String): SendEventOutput {
        val parsedHashtagsQuotesAndPubkeys = TODO("NostrParser not available?")
        // TODO: Add quotes and mentions

        val tags = listOf(Tag.fromStandardized(TagStandard.Subject(subject)))
        val builder = EventBuilder.textNote(content = content).tags(tags)

        return client.sendEventBuilder(builder)
        // TODO: Client Tag
    }

    suspend fun publishReply(
        content: String,
        parent: Event,
        mentions: List<PublicKey>,
    ): SendEventOutput {
        TODO()
        // TODO: Client Tag
    }

    suspend fun publishCrossPost(
        crossPostedEvent: Event,
        topics: List<Topic>,
    ): SendEventOutput {
        TODO()
    }

    suspend fun publishVote(event: Event): SendEventOutput {
        val reaction = eventPreferences.getUpvoteContent()
        val builder = EventBuilder.reaction(event = event, reaction = reaction)

        return client.sendEventBuilder(builder)
    }

    suspend fun publishDelete(
        eventIds: List<EventId>,
        coords: List<Coordinate> = emptyList(),
    ): SendEventOutput {
        // TODO: Issue for better SendEventOutput struct
        // TODO: Issue for default params
        val deletion = EventDeletionRequest(ids = eventIds, coordinates = coords, reason = null)
        val builder = EventBuilder.delete(request = deletion)

        return client.sendEventBuilder(builder)
    }

    suspend fun publishListDeletion(
        identifier: String,
    ): SendEventOutput {
        TODO("")
    }

    suspend fun publishTopicList(
        topics: List<Topic>,
    ): SendEventOutput {
        TODO("")
    }

    suspend fun publishBookmarkList(
        eventIds: List<EventId>,
    ): SendEventOutput {
        TODO("")
    }

    suspend fun publishNip65(
        read: List<String>, // TODO: No RelayUrl struct?
        write: List<String>
    ): SendEventOutput {
        TODO("")
    }

    suspend fun publishProfileSet(
        identifier: String,
        title: String,
        description: String,
        pubkeys: List<PublicKey>,
    ): SendEventOutput {
        TODO("")
    }

    suspend fun publishTopicSet(
        identifier: String,
        title: String,
        description: String,
        topics: List<Topic>,
    ): SendEventOutput {
        TODO("")
    }

    suspend fun publishProfile(
        metadata: Metadata,
    ): SendEventOutput {
        TODO("")
    }

    suspend fun publishGitIssue(
        repoCoordinate: Coordinate,
        subject: String,
        content: String,
        label: String,
    ): SendEventOutput {
        TODO("")
    }

    suspend fun followProfile(pubkey: PublicKey): SendEventOutput {
        val current = TODO()
        val followed = TODO()
        val builder = EventBuilder.contactList(contacts = followed)

        return client.sendEventBuilder(builder)
    }

    suspend fun unfollowProfile(pubkey: PublicKey): SendEventOutput {
        val current = TODO()
        val unfollowed = TODO()
        val builder = EventBuilder.contactList(contacts = unfollowed)

        return client.sendEventBuilder(builder)
    }

    suspend fun followTopic(topic: Topic): SendEventOutput {
        val current = TODO()
        val followed = TODO()
        val builder = EventBuilder.interests(list = followed)

        return client.sendEventBuilder(builder)
    }

    suspend fun unfollowTopic(topic: Topic): SendEventOutput {
        val current = TODO()
        val unfollowed = TODO()
        val builder = EventBuilder.interests(list = unfollowed)

        return client.sendEventBuilder(builder)
    }

    suspend fun rebroadcast(eventId: EventId): SendEventOutput {
        TODO("")
    }

    suspend fun removeOldEvents(threshold: Timestamp) {
        TODO()
    }

    // TODO: RelayUrl struct
    suspend fun addReadRelay(relay: String) {
        TODO()
    }

    // TODO: RelayUrl struct
    suspend fun addWriteRelay(relay: String) {
        TODO()
    }

    suspend fun close() {
        TODO()
    }
}
