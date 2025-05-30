package com.dluvian.voyage

import com.dluvian.voyage.preferences.EventPreferences
import com.dluvian.voyage.preferences.RelayPreferences
import rust.nostr.sdk.ClientBuilder
import rust.nostr.sdk.Contact
import rust.nostr.sdk.Coordinate
import rust.nostr.sdk.Event
import rust.nostr.sdk.EventBuilder
import rust.nostr.sdk.EventDeletionRequest
import rust.nostr.sdk.EventId
import rust.nostr.sdk.Filter
import rust.nostr.sdk.Kind
import rust.nostr.sdk.KindStandard
import rust.nostr.sdk.Metadata
import rust.nostr.sdk.Nip21
import rust.nostr.sdk.NostrDatabase
import rust.nostr.sdk.Options
import rust.nostr.sdk.PublicKey
import rust.nostr.sdk.RelayMetadata
import rust.nostr.sdk.RelayOptions
import rust.nostr.sdk.SendEventOutput
import rust.nostr.sdk.Tag
import rust.nostr.sdk.TagStandard
import rust.nostr.sdk.Timestamp
import rust.nostr.sdk.extractRelayList
import rust.nostr.sdk.nip21ExtractFromText

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
        getPersonalRelays().forEach { (relay, meta) ->
            val opts = RelayOptions()
                .read(meta == null || meta == RelayMetadata.READ)
                .write(meta == null || meta == RelayMetadata.WRITE)
            client.addRelayWithOpts(url = relay, opts = opts)
        }

        client.connect()
    }

    // TODO: No RelayUrl struct in nostr-sdk ?
    private suspend fun getPersonalRelays(): Map<RelayUrl, RelayMetadata?> {
        val filter = Filter()
            .author(client.signer().getPublicKey())
            .kind(Kind.fromStd(KindStandard.RELAY_LIST))
            .limit(1u)
        val event = client.database().query(filter).first()


        return if (event != null) {
            extractRelayList(event)
        } else {
            mapOf(
                Pair("wss://nos.lol", null),
                Pair("wss://relay.damus.io", null),
                Pair("wss://relay.primal.net", null)
            )
        }
    }

    suspend fun publishPost(
        subject: String,
        content: String,
        topics: List<Topic>
    ): Result<SendEventOutput> {
        val tags = mutableListOf(Tag.fromStandardized(TagStandard.Subject(subject)))

        topics.forEach { topic -> tags.add(Tag.hashtag(topic)) }

        val extractedNip21 = mutableListOf<Nip21>()
        extractedNip21.addAll(nip21ExtractFromText(subject))
        extractedNip21.addAll(nip21ExtractFromText(content))
        val extractedTags: List<Tag> = extractedNip21.map { nip21 ->
            when (nip21.asEnum()) {
                else -> TODO("wait for rust-nostr to provide mapping")
            }
        }
        tags.addAll(extractedTags)

        if (eventPreferences.isAddingClientTag()) {
            // TODO: Wait for default nullability
            tags.add(Tag.fromStandardized(TagStandard.Client(name = APP_NAME, address = null)))
        }

        val builder = EventBuilder.textNote(content = content).tags(tags).dedupTags()

        return runCatching { client.sendEventBuilder(builder) }
    }

    suspend fun publishReply(
        content: String,
        parent: Event,
    ): Result<SendEventOutput> {
        val tags: MutableList<Tag> = nip21ExtractFromText(content).map { nip21 ->
            when (nip21.asEnum()) {
                else -> TODO("wait for rust-nostr to provide mapping")
            }
        }.toMutableList()

        if (eventPreferences.isAddingClientTag()) {
            // TODO: Wait for default nullability
            tags.add(Tag.fromStandardized(TagStandard.Client(name = APP_NAME, address = null)))
        }

        val builder =
            EventBuilder.textNoteReply(content = content, replyTo = parent).tags(tags).dedupTags()

        return runCatching { client.sendEventBuilder(builder) }
    }

    suspend fun publishCrossPost(
        crossPostedEvent: Event,
        topics: List<Topic>,
    ): Result<SendEventOutput> {
        val tags = topics.map { topic -> Tag.hashtag(topic) }.toMutableList()

        if (eventPreferences.isAddingClientTag()) {
            // TODO: Wait for default nullability
            tags.add(Tag.fromStandardized(TagStandard.Client(name = APP_NAME, address = null)))
        }

        val builder = EventBuilder.repost(crossPostedEvent).tags(tags).dedupTags()

        return runCatching { client.sendEventBuilder(builder) }
    }

    suspend fun publishVote(event: Event): Result<SendEventOutput> {
        val reaction = eventPreferences.getUpvoteContent()
        val builder = EventBuilder.reaction(event = event, reaction = reaction)

        return runCatching { client.sendEventBuilder(builder) }
    }

    suspend fun publishDelete(
        eventIds: List<EventId>,
        coords: List<Coordinate> = emptyList(),
    ): Result<SendEventOutput> {
        // TODO: Wait for default nullability
        val deletion = EventDeletionRequest(ids = eventIds, coordinates = coords, reason = null)
        val builder = EventBuilder.delete(request = deletion)

        return runCatching { client.sendEventBuilder(builder) }
    }

    suspend fun publishListDeletion(ident: Ident): Result<SendEventOutput> {
        val pubkey = client.signer().getPublicKey()
        val coords = listOf(
            Coordinate(
                kind = Kind.fromStd(KindStandard.FOLLOW_SET),
                publicKey = pubkey,
                identifier = ident
            ),
            Coordinate(
                kind = Kind.fromStd(KindStandard.INTEREST_SET),
                publicKey = pubkey,
                identifier = ident
            ),
        )
        // TODO: Wait for default nullability
        val deletion = EventDeletionRequest(ids = emptyList(), coordinates = coords, reason = null)
        val builder = EventBuilder.delete(request = deletion)

        return runCatching { client.sendEventBuilder(builder) }
    }

    suspend fun publishTopicList(
        topics: List<Topic>,
    ): Result<SendEventOutput> {
        // TODO: Catch excecption
        TODO("")
    }

    suspend fun publishBookmarkList(
        eventIds: List<EventId>,
    ): Result<SendEventOutput> {
        // TODO: Catch excecption
        TODO("")
    }

    suspend fun publishNip65(
        read: List<String>, // TODO: No RelayUrl struct?
        write: List<String>
    ): Result<SendEventOutput> {
        // TODO: Catch excecption
        TODO("")
    }

    suspend fun publishProfileSet(
        identifier: String,
        title: String,
        description: String,
        pubkeys: List<PublicKey>,
    ): Result<SendEventOutput> {
        // TODO: Catch excecption
        TODO("")
    }

    suspend fun publishTopicSet(
        identifier: String,
        title: String,
        description: String,
        topics: List<Topic>,
    ): Result<SendEventOutput> {
        // TODO: Catch excecption
        TODO("")
    }

    suspend fun publishProfile(
        metadata: Metadata,
    ): Result<SendEventOutput> {
        // TODO: Catch excecption
        TODO("")
    }

    suspend fun publishGitIssue(
        repoCoordinate: Coordinate,
        subject: String,
        content: String,
        label: String,
    ): Result<SendEventOutput> {
        // TODO: Catch excecption
        TODO("")
    }

    suspend fun followProfile(pubkey: PublicKey): Result<SendEventOutput> {
        val current = TODO()
        val followed = TODO()
        val builder = EventBuilder.contactList(contacts = followed)

        return runCatching { client.sendEventBuilder(builder) }
    }

    suspend fun unfollowProfile(pubkey: PublicKey): Result<SendEventOutput> {
        val lol = Contact(publicKey = pubkey, relayUrl = null, alias = null)
        val current = TODO()
        val unfollowed = TODO()
        val builder = EventBuilder.contactList(contacts = unfollowed)

        return runCatching { client.sendEventBuilder(builder) }
    }

    suspend fun followTopic(topic: Topic): Result<SendEventOutput> {
        val current = TODO()
        val followed = TODO()
        val builder = EventBuilder.interests(list = followed)

        return runCatching { client.sendEventBuilder(builder) }
    }

    suspend fun unfollowTopic(topic: Topic): Result<SendEventOutput> {
        val current = TODO()
        val unfollowed = TODO()
        val builder = EventBuilder.interests(list = unfollowed)

        return runCatching { client.sendEventBuilder(builder) }
    }

    suspend fun addPubkeyToList(pubkey: PublicKey, ident: Ident): Result<SendEventOutput> {
        val current = TODO()
        val added = TODO()
        val builder = EventBuilder.followSet(identifier = ident, added)
        // TODO: Read name

        return runCatching { client.sendEventBuilder(builder) }
    }

    suspend fun addTopicToList(topic: Topic, ident: Ident): Result<SendEventOutput> {
        val current = TODO()
        val added = TODO()
        val builder = EventBuilder.interestSet(identifier = ident, added)
        // TODO: Read name

        return runCatching { client.sendEventBuilder(builder) }
    }

    suspend fun addBookmark(eventId: EventId): Result<SendEventOutput> {
        val current = TODO()
        val added = TODO()
        val builder = EventBuilder.bookmarks(list = added)

        return runCatching { client.sendEventBuilder(builder) }
    }

    suspend fun removeBookmark(eventId: EventId): Result<SendEventOutput> {
        val current = TODO()
        val removed = TODO()
        val builder = EventBuilder.bookmarks(list = removed)

        return runCatching { client.sendEventBuilder(builder) }
    }

    suspend fun rebroadcast(event: Event): Result<SendEventOutput> {
        TODO("")
    }

    suspend fun removeOldEvents(threshold: Timestamp) {
        client.database()
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
