package com.dluvian.voyage.nostr

import com.dluvian.voyage.APP_NAME
import com.dluvian.voyage.AlreadyFollowedException
import com.dluvian.voyage.AlreadyUnfollowedException
import com.dluvian.voyage.FailedToSignException
import com.dluvian.voyage.RelayUrl
import com.dluvian.voyage.Topic
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
import rust.nostr.sdk.GitIssue
import rust.nostr.sdk.Interests
import rust.nostr.sdk.Metadata
import rust.nostr.sdk.PublicKey
import rust.nostr.sdk.RelayMetadata
import rust.nostr.sdk.SendEventOutput
import rust.nostr.sdk.Tag
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
        val deletion = EventDeletionRequest(ids = eventIds, coordinates = coords, reason = null)
        val builder = EventBuilder.delete(request = deletion)

        val result = service.sign(builder)
        if (result.isFailure) return Result.failure(FailedToSignException())

        return Result.success(service.publish(result.getOrThrow()))
    }

    suspend fun publishNip65(
        relays: Map<RelayUrl, RelayMetadata?>,
    ): Result<SendEventOutput> {
        val builder = EventBuilder.relayList(relays)

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
        val newContacts = current.map { Contact(it, null, null) }
        val builder = EventBuilder.contactList(contacts = newContacts)

        val result = service.sign(builder)
        if (result.isFailure) return Result.failure(FailedToSignException())

        return Result.success(service.publish(result.getOrThrow()))
    }

    suspend fun unfollowProfile(pubkey: PublicKey): Result<SendEventOutput> {
        val current = trustProvider.friends().toMutableSet()
        if (!current.contains(pubkey)) return Result.failure(AlreadyFollowedException())

        current.remove(pubkey)
        val newContacts = current.map { Contact(it, null, null) }
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
