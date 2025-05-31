package com.dluvian.voyage

import com.dluvian.voyage.preferences.EventPreferences
import com.dluvian.voyage.preferences.RelayPreferences
import com.dluvian.voyage.provider.BookmarkProvider
import com.dluvian.voyage.provider.TopicProvider
import com.dluvian.voyage.provider.TrustProvider
import rust.nostr.sdk.Bookmarks
import rust.nostr.sdk.ClientBuilder
import rust.nostr.sdk.Contact
import rust.nostr.sdk.Coordinate
import rust.nostr.sdk.Event
import rust.nostr.sdk.EventBuilder
import rust.nostr.sdk.EventDeletionRequest
import rust.nostr.sdk.EventId
import rust.nostr.sdk.Filter
import rust.nostr.sdk.GitIssue
import rust.nostr.sdk.Interests
import rust.nostr.sdk.Kind
import rust.nostr.sdk.KindStandard
import rust.nostr.sdk.Metadata
import rust.nostr.sdk.NostrDatabase
import rust.nostr.sdk.Options
import rust.nostr.sdk.PublicKey
import rust.nostr.sdk.RelayMetadata
import rust.nostr.sdk.RelayOptions
import rust.nostr.sdk.SendEventOutput
import rust.nostr.sdk.Tag
import rust.nostr.sdk.TagKind
import rust.nostr.sdk.TagStandard
import rust.nostr.sdk.Tags
import rust.nostr.sdk.Timestamp
import rust.nostr.sdk.extractRelayList

class NostrService(
    private val relayPreferences: RelayPreferences,
    private val eventPreferences: EventPreferences,
    keyStore: KeyStore,
) {
    private val logTag = "NostrService"
    private val clientOpts = Options()
        .gossip(true)
        .automaticAuthentication(relayPreferences.getSendAuth())

    private val client = ClientBuilder()
        .signer(keyStore.getSigner())
        .opts(clientOpts)
        .database(NostrDatabase.lmdb("voyage_db"))
        .admitPolicy(RelayChecker())
        .build()

    private val trustProvider = TrustProvider(client)
    private val topicProvider = TopicProvider(client)
    private val bookmarkProvider = BookmarkProvider(client)

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
        tags.addAll(Tags.fromText(subject).toVec())
        tags.addAll(Tags.fromText(content).toVec())

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
        val tags = Tags.fromText(content).toVec().toMutableList()

        if (eventPreferences.isAddingClientTag()) {
            // TODO: Wait for default nullability
            tags.add(Tag.fromStandardized(TagStandard.Client(name = APP_NAME, address = null)))
        }

        val builder = EventBuilder
            .textNoteReply(content = content, replyTo = parent)
            .tags(tags)
            .dedupTags()

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

    suspend fun publishNip65(
        relays: Map<String, RelayMetadata?>, // TODO: No RelayUrl struct?
    ): Result<SendEventOutput> {
        val builder = EventBuilder.relayList(relays)

        return runCatching { client.sendEventBuilder(builder) }
    }

    suspend fun publishPubkeySet(
        ident: Ident,
        title: String,
        description: String,
        pubkeys: List<PublicKey>,
    ): Result<SendEventOutput> {
        val tags = listOf(
            Tag.fromStandardized(TagStandard.Title(title)),
            Tag.fromStandardized(TagStandard.Description(description))
        )
        val builder = EventBuilder.followSet(identifier = ident, publicKeys = pubkeys).tags(tags)

        return runCatching { client.sendEventBuilder(builder) }
    }

    suspend fun publishTopicSet(
        ident: String,
        title: String,
        description: String,
        topics: List<Topic>,
    ): Result<SendEventOutput> {
        val tags = listOf(
            Tag.fromStandardized(TagStandard.Title(title)),
            Tag.fromStandardized(TagStandard.Description(description))
        )
        val builder = EventBuilder.interestSet(identifier = ident, hashtags = topics).tags(tags)

        return runCatching { client.sendEventBuilder(builder) }
    }

    suspend fun publishProfile(
        metadata: Metadata,
    ): Result<SendEventOutput> {
        val builder = EventBuilder.metadata(metadata)

        return runCatching { client.sendEventBuilder(builder) }
    }

    suspend fun publishGitIssue(
        repoCoord: Coordinate,
        subject: String,
        content: String,
        label: String,
    ): Result<SendEventOutput> {
        val issue = GitIssue(
            repository = repoCoord,
            content = content,
            subject = subject,
            labels = listOf(label)
        )
        val builder = EventBuilder.gitIssue(issue)

        return runCatching { client.sendEventBuilder(builder) }
    }

    suspend fun followProfile(pubkey: PublicKey): Result<SendEventOutput> {
        val current = trustProvider.friends().toMutableSet()
        if (current.contains(pubkey)) return Result.failure(AlreadyFollowedException())

        current.add(pubkey)
        val newContacts = current.map { Contact(it, null, null) } // TODO: Wait for nullability
        val builder = EventBuilder.contactList(contacts = newContacts)

        return runCatching { client.sendEventBuilder(builder) }
    }

    suspend fun unfollowProfile(pubkey: PublicKey): Result<SendEventOutput> {
        val current = trustProvider.friends().toMutableSet()
        if (!current.contains(pubkey)) return Result.failure(AlreadyFollowedException())

        current.remove(pubkey)
        val newContacts = current.map { Contact(it, null, null) } // TODO: Wait for nullability
        val builder = EventBuilder.contactList(contacts = newContacts)

        return runCatching { client.sendEventBuilder(builder) }
    }

    suspend fun followTopic(topic: Topic): Result<SendEventOutput> {
        val current = topicProvider.topics().toMutableSet()
        if (current.contains(topic)) return Result.failure(AlreadyFollowedException())

        current.add(topic)
        val newTopics = Interests(hashtags = current.toList())
        val builder = EventBuilder.interests(newTopics)

        return runCatching { client.sendEventBuilder(builder) }
    }

    suspend fun unfollowTopic(topic: Topic): Result<SendEventOutput> {
        val current = topicProvider.topics().toMutableSet()
        if (!current.contains(topic)) return Result.failure(AlreadyUnfollowedException())

        current.remove(topic)
        val newTopics = Interests(hashtags = current.toList())
        val builder = EventBuilder.interests(newTopics)

        return runCatching { client.sendEventBuilder(builder) }
    }

    suspend fun addPubkeyToList(pubkey: PublicKey, ident: Ident): Result<SendEventOutput> {
        val filter = Filter()
            .author(client.signer().getPublicKey())
            .kind(Kind.fromStd(KindStandard.FOLLOW_SET))
            .identifier(ident)
            .limit(1u)
        val event = client.database().query(filter).first()
        if (event == null) return Result.failure(EventNotInDatabaseException())

        val pubkeys = event.tags().publicKeys().toMutableSet()
        if (pubkeys.contains(pubkey)) return Result.failure(AlreadyFollowedException())

        pubkeys.add(pubkey)
        val tags = listOf(
            event.tags().findStandardized(TagKind.Title),
            event.tags().findStandardized(TagKind.Description)
        ).mapNotNull { std -> std?.let { Tag.fromStandardized(std) } }
        val builder = EventBuilder
            .followSet(identifier = ident, publicKeys = pubkeys.toList())
            .tags(tags)

        return runCatching { client.sendEventBuilder(builder) }
    }

    suspend fun addTopicToList(topic: Topic, ident: Ident): Result<SendEventOutput> {
        val filter = Filter()
            .author(client.signer().getPublicKey())
            .kind(Kind.fromStd(KindStandard.INTEREST_SET))
            .identifier(ident)
            .limit(1u)
        val event = client.database().query(filter).first()
        if (event == null) return Result.failure(EventNotInDatabaseException())

        val topics = event.tags().hashtags().toMutableSet()
        if (topics.contains(topic)) return Result.failure(AlreadyFollowedException())

        topics.add(topic)
        val tags = listOf(
            event.tags().findStandardized(TagKind.Title),
            event.tags().findStandardized(TagKind.Description)
        ).mapNotNull { std -> std?.let { Tag.fromStandardized(std) } }
        val builder = EventBuilder
            .interestSet(identifier = ident, hashtags = topics.toList())
            .tags(tags)

        return runCatching { client.sendEventBuilder(builder) }
    }

    suspend fun addBookmark(eventId: EventId): Result<SendEventOutput> {
        val current = bookmarkProvider.bookmarks().toMutableSet()
        if (current.contains(eventId)) return Result.failure(AlreadyFollowedException())

        current.add(eventId)
        val newBookmarks = Bookmarks(eventIds = current.toList())
        val builder = EventBuilder.bookmarks(newBookmarks)

        return runCatching { client.sendEventBuilder(builder) }
    }

    suspend fun removeBookmark(eventId: EventId): Result<SendEventOutput> {
        val current = bookmarkProvider.bookmarks().toMutableSet()
        if (!current.contains(eventId)) return Result.failure(AlreadyUnfollowedException())

        current.remove(eventId)
        val newBookmarks = Bookmarks(eventIds = current.toList())
        val builder = EventBuilder.bookmarks(newBookmarks)

        return runCatching { client.sendEventBuilder(builder) }
    }

    suspend fun rebroadcast(event: Event): SendEventOutput {
        val relays = client.relays().keys.toList()

        return client.sendEventTo(urls = relays, event = event)
    }

    suspend fun dbRemoveOldData(threshold: Timestamp) {
        val kinds = listOf(
            KindStandard.TEXT_NOTE,
            KindStandard.REPOST,
            KindStandard.COMMENT,
            KindStandard.REACTION
        )
            .map { Kind.fromStd(it) }
        val deletion = Filter().kinds(kinds).until(threshold)

        client.database().delete(deletion)
    }

    suspend fun close() {
        client.shutdown()
    }
}
