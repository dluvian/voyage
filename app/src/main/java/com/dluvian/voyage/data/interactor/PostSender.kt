package com.dluvian.voyage.data.interactor

import android.util.Log
import com.dluvian.nostr_kt.extractMentions
import com.dluvian.nostr_kt.getHashtags
import com.dluvian.nostr_kt.getTitle
import com.dluvian.nostr_kt.secs
import com.dluvian.voyage.core.PubkeyHex
import com.dluvian.voyage.core.extractHashtags
import com.dluvian.voyage.data.event.ValidatedRootPost
import com.dluvian.voyage.data.nostr.NostrService
import com.dluvian.voyage.data.provider.RelayProvider
import com.dluvian.voyage.data.room.dao.tx.PostInsertDao
import rust.nostr.protocol.Event
import rust.nostr.protocol.Nip19Profile
import rust.nostr.protocol.PublicKey

class PostSender(
    private val nostrService: NostrService,
    private val relayProvider: RelayProvider,
    private val postInsertDao: PostInsertDao
) {
    private val tag = "PostSender"

    suspend fun sendPost(header: String, body: String): Result<Event> {
        val trimmedHeader = header.trim()
        val trimmedBody = body.trim()
        val concat = "$trimmedHeader $trimmedBody"

        return nostrService.publishPost(
            title = trimmedHeader,
            content = trimmedBody,
            topics = extractHashtags(content = concat),
            mentions = extractMentionPubkeys(content = concat),
            relayUrls = relayProvider.getReadRelays()
        ).onSuccess { event ->
            val validatedPost = ValidatedRootPost(
                id = event.id().toHex(),
                pubkey = event.author().toHex(),
                topics = event.getHashtags(),
                title = event.getTitle(),
                content = event.content(),
                createdAt = event.createdAt().secs()
            )
            postInsertDao.insertRootPost(rootPost = validatedPost)
        }.onFailure {
            Log.w(tag, "Failed to create post event", it)
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
