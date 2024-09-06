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
import com.dluvian.voyage.data.event.EventValidator
import com.dluvian.voyage.data.event.ValidatedCrossPost
import com.dluvian.voyage.data.event.ValidatedReply
import com.dluvian.voyage.data.event.ValidatedRootPost
import com.dluvian.voyage.data.nostr.NostrService
import com.dluvian.voyage.data.nostr.RelayUrl
import com.dluvian.voyage.data.nostr.extractMentions
import com.dluvian.voyage.data.nostr.extractQuotes
import com.dluvian.voyage.data.nostr.getSubject
import com.dluvian.voyage.data.nostr.secs
import com.dluvian.voyage.data.provider.RelayProvider
import com.dluvian.voyage.data.room.dao.PostDao
import com.dluvian.voyage.data.room.dao.tx.PostInsertDao
import rust.nostr.protocol.Coordinate
import rust.nostr.protocol.Event
import rust.nostr.protocol.EventId
import rust.nostr.protocol.Kind
import rust.nostr.protocol.KindEnum
import rust.nostr.protocol.Nip19Event
import rust.nostr.protocol.Nip19Profile
import rust.nostr.protocol.PublicKey

private const val TAG = "PostSender"

class PostSender(
    private val nostrService: NostrService,
    private val relayProvider: RelayProvider,
    private val postInsertDao: PostInsertDao,
    private val postDao: PostDao,
    private val myPubkeyProvider: IMyPubkeyProvider,
) {
    suspend fun sendPost(
        header: String,
        body: String,
        topics: List<Topic>,
        isAnon: Boolean,
    ): Result<Event> {
        val trimmedHeader = header.trim()
        val trimmedBody = body.trim()
        val concat = "$trimmedHeader $trimmedBody"

        val mentions = extractMentionsFromString(content = concat, isAnon = isAnon)
        val allTopics = topics.toMutableList()
        allTopics.addAll(extractCleanHashtags(content = concat))

        return nostrService.publishPost(
            subject = trimmedHeader,
            content = trimmedBody,
            topics = allTopics.distinct().take(MAX_TOPICS),
            mentions = mentions,
            quotes = extractQuotesFromString(content = concat),
            relayUrls = relayProvider.getPublishRelays(publishTo = mentions),
            isAnon = isAnon,
        ).onSuccess { event ->
            val validatedPost = ValidatedRootPost(
                id = event.id().toHex(),
                pubkey = event.author().toHex(),
                topics = event.getNormalizedTopics(),
                subject = event.getSubject(),
                content = event.content(),
                createdAt = event.createdAt().secs(),
                relayUrl = "", // We don't know which relay accepted this note
                json = event.asJson(),
                isMentioningMe = mentions.contains(myPubkeyProvider.getPubkeyHex())
            )
            postInsertDao.insertRootPosts(posts = listOf(validatedPost))
        }.onFailure {
            Log.w(TAG, "Failed to create post event", it)
        }
    }

    suspend fun sendReply(
        parentId: EventIdHex,
        recipient: PubkeyHex,
        body: String,
        relayHint: RelayUrl,
        isAnon: Boolean,
    ): Result<Event> {
        val trimmedBody = body.trim()
        val mentions = mutableListOf(recipient).apply {
            addAll(extractMentionPubkeys(content = trimmedBody))
            postDao.getParentAuthor(id = parentId)?.let { grandparentAuthor ->
                add(grandparentAuthor)
            }
            if (!isAnon) removeIf { it == myPubkeyProvider.getPubkeyHex() }
        }.distinct()

        return nostrService.publishReply(
            content = trimmedBody,
            parentId = parentId,
            mentions = mentions,
            quotes = extractQuotesFromString(content = trimmedBody),
            relayHint = relayHint,
            pubkeyHint = recipient,
            relayUrls = relayProvider.getPublishRelays(publishTo = mentions),
            isAnon = isAnon,
        ).onSuccess { event ->
            val validatedReply = ValidatedReply(
                id = event.id().toHex(),
                pubkey = event.author().toHex(),
                parentId = parentId,
                content = event.content(),
                createdAt = event.createdAt().secs(),
                relayUrl = "", // We don't know which relay accepted this note
                json = event.asJson(),
                isMentioningMe = mentions.contains(myPubkeyProvider.getPubkeyHex())
            )
            postInsertDao.insertReplies(replies = listOf(validatedReply))
        }.onFailure {
            Log.w(TAG, "Failed to create reply event", it)
        }
    }

    suspend fun sendCrossPost(
        id: EventIdHex,
        topics: List<Topic>,
        isAnon: Boolean,
    ): Result<Event> {
        val post = postDao.getPost(id = id)
            ?: return Result.failure(IllegalStateException("Post not found"))
        val json = post.json
            ?: return Result.failure(IllegalStateException("Json not found"))
        if (json.isEmpty()) return Result.failure(IllegalStateException("Json is empty"))
        val crossPostedEvent = kotlin.runCatching { Event.fromJson(json) }.getOrNull()
            ?: return Result.failure(IllegalStateException("Json is not deserializable"))
        if (post.crossPostedId != null) {
            return Result.failure(IllegalStateException("Can't cross-post a cross-post"))
        }
        val validatedMainPost = EventValidator.createValidatedMainPost(
            event = crossPostedEvent,
            relayUrl = post.relayUrl,
            myPubkey = myPubkeyProvider.getPublicKey()
        )
            ?: return Result.failure(IllegalStateException("Cross-posted event is invalid"))

        return nostrService.publishCrossPost(
            crossPostedEvent = crossPostedEvent,
            topics = topics,
            relayHint = post.relayUrl,
            relayUrls = relayProvider.getPublishRelays(),
            isAnon = isAnon,
        ).onSuccess { event ->
            val validatedCrossPost = ValidatedCrossPost(
                id = event.id().toHex(),
                pubkey = event.author().toHex(),
                topics = event.getNormalizedTopics(),
                createdAt = event.createdAt().secs(),
                relayUrl = "", // We don't know which relay accepted this note
                crossPosted = validatedMainPost,
            )
            postInsertDao.insertCrossPosts(crossPosts = listOf(validatedCrossPost))
        }.onFailure {
            Log.w(TAG, "Failed to create cross-post event", it)
        }
    }

    private val repoCoordinate = Coordinate(
        kind = Kind.fromEnum(KindEnum.GitRepoAnnouncement),
        publicKey = PublicKey.fromHex(hex = DLUVIAN_HEX),
        identifier = VOYAGE
    )

    suspend fun sendGitIssue(
        issue: LabledGitIssue,
        isAnon: Boolean,
    ): Result<Event> {
        val trimmedHeader = issue.header.trim()
        val trimmedBody = issue.body.trim()
        val mentions = extractMentionsFromString(
            content = "$trimmedHeader $trimmedBody",
            isAnon = isAnon
        )

        return nostrService.publishGitIssue(
            repoCoordinate = repoCoordinate,
            subject = trimmedHeader,
            content = trimmedBody,
            label = issue.getLabel(),
            mentions = mentions,
            // TODO: Once rust-nostr implements it: .filterNot { it == repoCoordinate.toString() },
            quotes = extractQuotesFromString(content = trimmedBody),
            relayUrls = relayProvider.getPublishRelays(publishTo = listOf(DLUVIAN_HEX)),
            isAnon = isAnon,
        )
    }

    private fun extractMentionPubkeys(content: String): List<PubkeyHex> {
        return extractMentions(content = content)
            .mapNotNull {
                runCatching { PublicKey.fromBech32(it).toHex() }.getOrNull()
                    ?: kotlin.runCatching { Nip19Profile.fromBech32(it).publicKey().toHex() }
                        .getOrNull()
            }.distinct()
    }

    private fun extractMentionsFromString(content: String, isAnon: Boolean): List<PubkeyHex> {
        return extractMentionPubkeys(content = content).let { pubkeys ->
            if (!isAnon) pubkeys.filter { it != myPubkeyProvider.getPubkeyHex() }
            else pubkeys
        }
    }

    // Either EventIdHex or Coordinate
    private fun extractQuotesFromString(content: String): List<String> {
        return extractQuotes(content = content)
            .mapNotNull {
                runCatching { Nip19Event.fromBech32(it).eventId().toHex() }.getOrNull()
                    ?: runCatching { EventId.fromBech32(it).toHex() }.getOrNull()
                    ?: runCatching {
                        Coordinate.fromBech32(it).let { coord ->
                            val kind = coord.kind().asU16()
                            val pubkey = coord.publicKey().toHex()
                            val identifier = coord.identifier()
                            "$kind:$pubkey:$identifier".removeSuffix(":")
                        }
                    }.getOrNull()
            }.distinct()
    }
}
