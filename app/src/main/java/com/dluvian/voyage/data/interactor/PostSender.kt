package com.dluvian.voyage.data.interactor

import android.util.Log
import com.dluvian.nostr_kt.RelayUrl
import com.dluvian.nostr_kt.extractMentions
import com.dluvian.nostr_kt.getSubject
import com.dluvian.nostr_kt.secs
import com.dluvian.voyage.core.EventIdHex
import com.dluvian.voyage.core.MAX_TOPICS
import com.dluvian.voyage.core.PubkeyHex
import com.dluvian.voyage.core.Topic
import com.dluvian.voyage.core.createValidatedMainPost
import com.dluvian.voyage.core.extractCleanHashtags
import com.dluvian.voyage.core.getNormalizedTopics
import com.dluvian.voyage.data.event.ValidatedCrossPost
import com.dluvian.voyage.data.event.ValidatedReply
import com.dluvian.voyage.data.event.ValidatedRootPost
import com.dluvian.voyage.data.nostr.NostrService
import com.dluvian.voyage.data.provider.RelayProvider
import com.dluvian.voyage.data.room.dao.PostDao
import com.dluvian.voyage.data.room.dao.tx.PostInsertDao
import rust.nostr.protocol.Event
import rust.nostr.protocol.Nip19Profile
import rust.nostr.protocol.PublicKey

class PostSender(
    private val nostrService: NostrService,
    private val relayProvider: RelayProvider,
    private val postInsertDao: PostInsertDao,
    private val postDao: PostDao,
) {
    private val tag = "PostSender"

    suspend fun sendPost(
        header: String,
        body: String,
        topics: List<Topic>,
    ): Result<Event> {
        val trimmedHeader = header.trim()
        val trimmedBody = body.trim()
        val concat = "$trimmedHeader $trimmedBody"

        val mentions = extractMentionPubkeys(content = concat)
        val allTopics = topics.toMutableList()
        allTopics.addAll(extractCleanHashtags(content = concat))

        return nostrService.publishPost(
            subject = trimmedHeader,
            content = trimmedBody,
            topics = allTopics.distinct().take(MAX_TOPICS),
            mentions = mentions,
            relayUrls = relayProvider.getPublishRelays(publishTo = mentions),
        ).onSuccess { event ->
            val validatedPost = ValidatedRootPost(
                id = event.id().toHex(),
                pubkey = event.author().toHex(),
                topics = event.getNormalizedTopics(limited = false),
                subject = event.getSubject(),
                content = event.content(),
                createdAt = event.createdAt().secs(),
                relayUrl = "", // We don't know which relay accepted this note
                json = event.asJson(),
            )
            postInsertDao.insertRootPosts(posts = listOf(validatedPost))
        }.onFailure {
            Log.w(tag, "Failed to create post event", it)
        }
    }

    suspend fun sendReply(
        parentId: EventIdHex,
        recipient: PubkeyHex,
        body: String,
        relayHint: RelayUrl,
    ): Result<Event> {
        val trimmedBody = body.trim()
        val mentions = (extractMentionPubkeys(content = trimmedBody) + recipient).distinct()

        return nostrService.publishReply(
            content = trimmedBody,
            parentId = parentId,
            mentions = mentions,
            relayHint = relayHint,
            pubkeyHint = recipient,
            relayUrls = relayProvider.getPublishRelays(publishTo = mentions),
        ).onSuccess { event ->
            val validatedReply = ValidatedReply(
                id = event.id().toHex(),
                pubkey = event.author().toHex(),
                parentId = parentId,
                content = event.content(),
                createdAt = event.createdAt().secs(),
                relayUrl = "", // We don't know which relay accepted this note
                json = event.asJson(),
            )
            postInsertDao.insertReplies(replies = listOf(validatedReply))
        }.onFailure {
            Log.w(tag, "Failed to create reply event", it)
        }
    }

    suspend fun sendCrossPost(
        id: EventIdHex,
        topics: List<Topic>,
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
        val validatedMainPost = createValidatedMainPost(
            event = crossPostedEvent,
            relayUrl = post.relayUrl
        )
            ?: return Result.failure(IllegalStateException("Cross-posted event is invalid"))

        return nostrService.publishCrossPost(
            crossPostedEvent = crossPostedEvent,
            topics = topics,
            relayHint = post.relayUrl,
            relayUrls = relayProvider.getPublishRelays(),
        ).onSuccess { event ->
            val validatedCrossPost = ValidatedCrossPost(
                id = event.id().toHex(),
                pubkey = event.author().toHex(),
                topics = event.getNormalizedTopics(limited = false),
                createdAt = event.createdAt().secs(),
                relayUrl = "", // We don't know which relay accepted this note
                crossPosted = validatedMainPost,
            )
            postInsertDao.insertCrossPosts(crossPosts = listOf(validatedCrossPost))
        }.onFailure {
            Log.w(tag, "Failed to create cross-post event", it)
        }
    }

    private fun extractMentionPubkeys(content: String): List<PubkeyHex> {
        return extractMentions(content = content)
            .mapNotNull {
                runCatching { PublicKey.fromBech32(it).toHex() }.getOrNull()
                    ?: kotlin.runCatching { Nip19Profile.fromBech32(it).publicKey().toHex() }
                        .getOrNull()
            }.distinct()
    }
}
