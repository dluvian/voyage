package com.dluvian.voyage.data.interactor

import com.dluvian.nostr_kt.extractMentions
import com.dluvian.voyage.core.PubkeyHex
import com.dluvian.voyage.core.extractHashtags
import com.dluvian.voyage.data.nostr.NostrService
import com.dluvian.voyage.data.provider.RelayProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import rust.nostr.protocol.Nip19Profile
import rust.nostr.protocol.PublicKey

class PostSender(private val nostrService: NostrService, private val relayProvider: RelayProvider) {
    private val scope = CoroutineScope(Dispatchers.IO)

    fun sendPost(header: String, body: String) {
        val trimmedHeader = header.trim()
        val trimmedBody = body.trim()
        val concat = "$trimmedHeader $trimmedBody"

        scope.launch {
            val result = nostrService.publishPost(
                title = trimmedHeader,
                content = trimmedBody,
                topics = extractHashtags(content = concat),
                mentions = extractMentionPubkeys(content = concat),
                relayUrls = relayProvider.getReadRelays()
            )

            // TODO: insert result in db
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
