package com.dluvian.voyage

import com.dluvian.voyage.nostr.NostrService
import com.dluvian.voyage.preferences.EventPreferences
import com.dluvian.voyage.provider.BookmarkProvider
import com.dluvian.voyage.provider.TopicProvider
import com.dluvian.voyage.provider.TrustProvider
import rust.nostr.sdk.Bookmarks
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
import rust.nostr.sdk.PublicKey
import rust.nostr.sdk.RelayMetadata
import rust.nostr.sdk.SendEventOutput
import rust.nostr.sdk.Tag
import rust.nostr.sdk.TagKind
import rust.nostr.sdk.TagStandard
import rust.nostr.sdk.Tags

class EventCreator(
    private val service: NostrService,
    private val eventPreferences: EventPreferences,
    private val trustProvider: TrustProvider,
    private val topicProvider: TopicProvider,
    private val bookmarkProvider: BookmarkProvider
) {

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

        val result = service.sign(builder)
        if (result.isFailure) return Result.failure(FailedToSignException())

        return Result.success(service.publish(result.getOrThrow()))
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

        val result = service.sign(builder)
        if (result.isFailure) return Result.failure(FailedToSignException())

        return Result.success(service.publish(result.getOrThrow()))
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

        val result = service.sign(builder)
        if (result.isFailure) return Result.failure(FailedToSignException())

        return Result.success(service.publish(result.getOrThrow()))
    }

    suspend fun publishVote(event: Event): Result<SendEventOutput> {
        val reaction = eventPreferences.getUpvoteContent()
        val builder = EventBuilder.reaction(event = event, reaction = reaction)

        val result = service.sign(builder)
        if (result.isFailure) return Result.failure(FailedToSignException())

        return Result.success(service.publish(result.getOrThrow()))
    }

    suspend fun publishDelete(
        eventIds: List<EventId>,
        coords: List<Coordinate> = emptyList(),
    ): Result<SendEventOutput> {
        // TODO: Wait for default nullability
        val deletion = EventDeletionRequest(ids = eventIds, coordinates = coords, reason = null)
        val builder = EventBuilder.delete(request = deletion)

        val result = service.sign(builder)
        if (result.isFailure) return Result.failure(FailedToSignException())

        return Result.success(service.publish(result.getOrThrow()))
    }

    suspend fun publishListDeletion(ident: Ident): Result<SendEventOutput> {
        val pubkey = service.pubkey()
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

        val result = service.sign(builder)
        if (result.isFailure) return Result.failure(FailedToSignException())

        return Result.success(service.publish(result.getOrThrow()))
    }

    suspend fun publishNip65(
        relays: Map<String, RelayMetadata?>, // TODO: No RelayUrl struct?
    ): Result<SendEventOutput> {
        val builder = EventBuilder.relayList(relays)

        val result = service.sign(builder)
        if (result.isFailure) return Result.failure(FailedToSignException())

        return Result.success(service.publish(result.getOrThrow()))
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

        val result = service.sign(builder)
        if (result.isFailure) return Result.failure(FailedToSignException())

        return Result.success(service.publish(result.getOrThrow()))
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

        val result = service.sign(builder)
        if (result.isFailure) return Result.failure(FailedToSignException())

        return Result.success(service.publish(result.getOrThrow()))
    }

    suspend fun publishProfile(
        metadata: Metadata,
    ): Result<SendEventOutput> {
        val builder = EventBuilder.metadata(metadata)

        val result = service.sign(builder)
        if (result.isFailure) return Result.failure(FailedToSignException())

        return Result.success(service.publish(result.getOrThrow()))
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

        val result = service.sign(builder)
        if (result.isFailure) return Result.failure(FailedToSignException())

        return Result.success(service.publish(result.getOrThrow()))
    }

    suspend fun followProfile(pubkey: PublicKey): Result<SendEventOutput> {
        val current = trustProvider.friends().toMutableSet()
        if (current.contains(pubkey)) return Result.failure(AlreadyFollowedException())

        current.add(pubkey)
        val newContacts = current.map { Contact(it, null, null) } // TODO: Wait for nullability
        val builder = EventBuilder.contactList(contacts = newContacts)

        val result = service.sign(builder)
        if (result.isFailure) return Result.failure(FailedToSignException())

        return Result.success(service.publish(result.getOrThrow()))
    }

    suspend fun unfollowProfile(pubkey: PublicKey): Result<SendEventOutput> {
        val current = trustProvider.friends().toMutableSet()
        if (!current.contains(pubkey)) return Result.failure(AlreadyFollowedException())

        current.remove(pubkey)
        val newContacts = current.map { Contact(it, null, null) } // TODO: Wait for nullability
        val builder = EventBuilder.contactList(contacts = newContacts)

        val result = service.sign(builder)
        if (result.isFailure) return Result.failure(FailedToSignException())

        return Result.success(service.publish(result.getOrThrow()))
    }

    suspend fun followTopic(topic: Topic): Result<SendEventOutput> {
        val current = topicProvider.topics().toMutableSet()
        if (current.contains(topic)) return Result.failure(AlreadyFollowedException())

        current.add(topic)
        val newTopics = Interests(hashtags = current.toList())
        val builder = EventBuilder.interests(newTopics)

        val result = service.sign(builder)
        if (result.isFailure) return Result.failure(FailedToSignException())

        return Result.success(service.publish(result.getOrThrow()))
    }

    suspend fun unfollowTopic(topic: Topic): Result<SendEventOutput> {
        val current = topicProvider.topics().toMutableSet()
        if (!current.contains(topic)) return Result.failure(AlreadyUnfollowedException())

        current.remove(topic)
        val newTopics = Interests(hashtags = current.toList())
        val builder = EventBuilder.interests(newTopics)

        val result = service.sign(builder)
        if (result.isFailure) return Result.failure(FailedToSignException())

        return Result.success(service.publish(result.getOrThrow()))
    }

    suspend fun addPubkeyToList(pubkey: PublicKey, ident: Ident): Result<SendEventOutput> {
        val filter = Filter()
            .author(service.pubkey())
            .kind(Kind.fromStd(KindStandard.FOLLOW_SET))
            .identifier(ident)
            .limit(1u)
        val event = service.dbQuery(filter).firstOrNull()
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

        val result = service.sign(builder)
        if (result.isFailure) return Result.failure(FailedToSignException())

        return Result.success(service.publish(result.getOrThrow()))
    }

    suspend fun addTopicToList(topic: Topic, ident: Ident): Result<SendEventOutput> {
        val filter = Filter()
            .author(service.pubkey())
            .kind(Kind.fromStd(KindStandard.INTEREST_SET))
            .identifier(ident)
            .limit(1u)
        val event = service.dbQuery(filter).firstOrNull()
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

        val result = service.sign(builder)
        if (result.isFailure) return Result.failure(FailedToSignException())

        return Result.success(service.publish(result.getOrThrow()))
    }

    suspend fun addBookmark(eventId: EventId): Result<SendEventOutput> {
        val current = bookmarkProvider.bookmarks().toMutableSet()
        if (current.contains(eventId)) return Result.failure(AlreadyFollowedException())

        current.add(eventId)
        val newBookmarks = Bookmarks(eventIds = current.toList())
        val builder = EventBuilder.bookmarks(newBookmarks)

        val result = service.sign(builder)
        if (result.isFailure) return Result.failure(FailedToSignException())

        return Result.success(service.publish(result.getOrThrow()))
    }

    suspend fun removeBookmark(eventId: EventId): Result<SendEventOutput> {
        val current = bookmarkProvider.bookmarks().toMutableSet()
        if (!current.contains(eventId)) return Result.failure(AlreadyUnfollowedException())

        current.remove(eventId)
        val newBookmarks = Bookmarks(eventIds = current.toList())
        val builder = EventBuilder.bookmarks(newBookmarks)

        val result = service.sign(builder)
        if (result.isFailure) return Result.failure(FailedToSignException())

        return Result.success(service.publish(result.getOrThrow()))
    }
}
