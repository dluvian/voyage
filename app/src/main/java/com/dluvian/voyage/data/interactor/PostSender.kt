package com.dluvian.voyage.data.interactor

import android.util.Log
import com.dluvian.voyage.core.DLUVIAN_HEX
import com.dluvian.voyage.core.EventIdHex
import com.dluvian.voyage.core.MAX_TOPICS
import com.dluvian.voyage.core.PubkeyHex
import com.dluvian.voyage.core.Topic
import com.dluvian.voyage.core.VOYAGE
import com.dluvian.voyage.core.model.LabledGitIssue
import com.dluvian.voyage.core.utils.extractCleanHashtags
import com.dluvian.voyage.core.utils.getNormalizedTopics
import com.dluvian.voyage.data.account.IMyPubkeyProvider
import com.dluvian.voyage.data.event.COMMENT_U16
import com.dluvian.voyage.data.event.EventValidator
import com.dluvian.voyage.data.event.TEXT_NOTE_U16
import com.dluvian.voyage.data.event.ValidatedComment
import com.dluvian.voyage.data.event.ValidatedCrossPost
import com.dluvian.voyage.data.event.ValidatedRootPost
import com.dluvian.voyage.data.nostr.NostrService
import com.dluvian.voyage.data.nostr.RelayUrl
import com.dluvian.voyage.data.nostr.extractMentions
import com.dluvian.voyage.data.nostr.extractQuotes
import com.dluvian.voyage.data.nostr.secs
import com.dluvian.voyage.data.provider.RelayProvider
import com.dluvian.voyage.data.room.dao.MainEventDao
import com.dluvian.voyage.data.room.dao.insert.MainEventInsertDao
import rust.nostr.sdk.Coordinate
import rust.nostr.sdk.Event
import rust.nostr.sdk.EventId
import rust.nostr.sdk.Kind
import rust.nostr.sdk.KindStandard
import rust.nostr.sdk.Nip19Event
import rust.nostr.sdk.Nip19Profile
import rust.nostr.sdk.PublicKey

private const val TAG = "PostSender"

class PostSender(
    private val nostrService: NostrService,
    private val relayProvider: RelayProvider,
    private val mainEventInsertDao: MainEventInsertDao,
    private val mainEventDao: MainEventDao,
    private val myPubkeyProvider: IMyPubkeyProvider,
) {
    suspend fun sendPost(
        body: String,
        topics: List<Topic>,
    ): Result<Event> {
        val trimmedBody = body.trim()

        val mentions = extractMentions(content = trimmedBody)
        val allTopics = topics.toMutableList()
        allTopics.addAll(extractCleanHashtags(content = trimmedBody))

        return nostrService.publishPost(
            content = trimmedBody,
            topics = allTopics.distinct().take(MAX_TOPICS),
            mentions = mentions,
            quotes = extractQuotesFromString(content = trimmedBody),
            relayUrls = relayProvider.getPublishRelays(publishTo = mentions),
        ).onSuccess { event ->
            val validatedPost = ValidatedRootPost(
                id = event.id().toHex(),
                pubkey = event.author().toHex(),
                topics = event.getNormalizedTopics(),
                content = event.content(),
                createdAt = event.createdAt().secs(),
                relayUrl = "", // We don't know which relay accepted this note
                json = event.asJson(),
                isMentioningMe = mentions.contains(myPubkeyProvider.getPubkeyHex())
            )
            mainEventInsertDao.insertRootPosts(roots = listOf(validatedPost))
        }.onFailure {
            Log.w(TAG, "Failed to create post event", it)
        }
    }

    suspend fun sendComment(
        content: String,
        parent: Event,
        relayHint: RelayUrl?,
    ): Result<Event> {
        val trimmedContent = content.trim()
        val mentions = mutableListOf<PubkeyHex>().apply {
            // Not setting parent author, bc rust-nostr is doing it
            addAll(extractMentionPubkeys(content = trimmedContent))
        }.minus(parent.tags().publicKeys().map { it.toHex() }) // rust-nostr uses p-tags of parent
            .distinct()

        return nostrService.publishComment(
            content = trimmedContent,
            parent = parent,
            mentions = mentions,
            quotes = extractQuotesFromString(content = content),
            topics = extractCleanHashtags(content = trimmedContent).take(MAX_TOPICS),
            relayHint = relayHint,
            relayUrls = relayProvider.getPublishRelays(publishTo = mentions),
        ).onSuccess { event ->
            val validatedComment = ValidatedComment(
                id = event.id().toHex(),
                pubkey = event.author().toHex(),
                createdAt = event.createdAt().secs(),
                relayUrl = "", // We don't know which relay accepted this note
                content = event.content(),
                json = event.asJson(),
                isMentioningMe = mentions.contains(myPubkeyProvider.getPubkeyHex()),
                parentId = parent.id().toHex(),
                parentKind = parent.kind().asU16(),
            )
            mainEventInsertDao.insertComments(comments = listOf(validatedComment))

        }.onFailure {
            Log.w(TAG, "Failed to create comment event", it)
        }
    }

    suspend fun sendCrossPost(
        id: EventIdHex,
        topics: List<Topic>,
    ): Result<Event> {
        val post = mainEventDao.getPost(id = id)
            ?: return Result.failure(IllegalStateException("Post not found"))
        val json = post.json
            ?: return Result.failure(IllegalStateException("Json not found"))
        if (json.isEmpty()) return Result.failure(IllegalStateException("Json is empty"))
        val crossPostedEvent = kotlin.runCatching { Event.fromJson(json) }.getOrNull()
            ?: return Result.failure(IllegalStateException("Json is not deserializable"))

        val validatedEvent = when (crossPostedEvent.kind().asU16()) {
            TEXT_NOTE_U16 -> EventValidator.createValidatedTextNote(
                event = crossPostedEvent,
                relayUrl = post.relayUrl,
                myPubkey = myPubkeyProvider.getPublicKey()
            )

            COMMENT_U16 -> EventValidator.createValidatedComment(
                event = crossPostedEvent,
                relayUrl = post.relayUrl,
                myPubkey = myPubkeyProvider.getPublicKey()
            )

            else -> {
                val kind = crossPostedEvent.kind().asU16()
                Log.w(TAG, "Cross-posting kind $kind is not supported yet")
                null
            }
        } ?: return Result.failure(IllegalStateException("Cross-posted event is invalid"))

        return nostrService.publishCrossPost(
            crossPostedEvent = crossPostedEvent,
            topics = topics,
            relayHint = post.relayUrl,
            relayUrls = relayProvider.getPublishRelays(),
        ).onSuccess { event ->
            val validatedCrossPost = ValidatedCrossPost(
                id = event.id().toHex(),
                pubkey = event.author().toHex(),
                topics = event.getNormalizedTopics(),
                createdAt = event.createdAt().secs(),
                relayUrl = "", // We don't know which relay accepted this note
                crossPostedId = validatedEvent.id,
                crossPostedThreadableEvent = validatedEvent,
            )
            mainEventInsertDao.insertCrossPosts(crossPosts = listOf(validatedCrossPost))
        }.onFailure {
            Log.w(TAG, "Failed to create cross-post event", it)
        }
    }

    private val repoCoordinate = Coordinate(
        kind = Kind.fromStd(KindStandard.GIT_REPO_ANNOUNCEMENT),
        publicKey = PublicKey.parse(DLUVIAN_HEX),
        identifier = VOYAGE
    )

    suspend fun sendGitIssue(issue: LabledGitIssue): Result<Event> {
        val trimmedHeader = issue.header.trim()
        val trimmedBody = issue.body.trim()
        val mentions = extractMentions(content = "$trimmedHeader $trimmedBody")
        val repoCoordinateStr = repoCoordinate.toString()

        return nostrService.publishGitIssue(
            repoCoordinate = repoCoordinate,
            title = trimmedHeader,
            content = trimmedBody,
            label = issue.getLabel(),
            mentions = mentions,
            quotes = extractQuotesFromString(content = trimmedBody)
                .filterNot { it == repoCoordinateStr },
            relayUrls = relayProvider.getPublishRelays(publishTo = listOf(DLUVIAN_HEX)),
        )
    }

    // TODO: Use nostr-sdk
    private fun extractMentionPubkeys(content: String): List<PubkeyHex> {
        return extractMentions(content = content)
            .mapNotNull {
                runCatching { PublicKey.parse(it).toHex() }.getOrNull()
                    ?: kotlin.runCatching { Nip19Profile.fromBech32(it).publicKey().toHex() }
                        .getOrNull()
            }.distinct()
    }

    // TODO: Use nostr-sdk
    // Either EventIdHex or Coordinate
    private fun extractQuotesFromString(content: String): List<String> {
        return extractQuotes(content = content)
            .mapNotNull {
                runCatching { Nip19Event.fromBech32(it).eventId().toHex() }.getOrNull()
                    ?: runCatching { EventId.parse(it).toHex() }.getOrNull()
                    ?: runCatching { Coordinate.parse(it).toString() }.getOrNull()
            }.distinct()
    }
}
