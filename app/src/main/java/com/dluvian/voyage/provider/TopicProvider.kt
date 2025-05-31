package com.dluvian.voyage.provider

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import com.dluvian.voyage.NostrService
import com.dluvian.voyage.Topic
import rust.nostr.sdk.Filter
import rust.nostr.sdk.Kind
import rust.nostr.sdk.KindStandard
import kotlin.collections.orEmpty

class TopicProvider(private val service: NostrService) {
    private val logTag = "TrustProvider"
    val topics = mutableStateOf(emptySet<Topic>())

    // TODO: Switch signer
    // TODO: Listen to database events
    suspend fun init() {
        val pubkey = service.pubkey()
        val topicFilter = Filter()
            .author(pubkey)
            .kind(Kind.fromStd(KindStandard.INTERESTS))
            .limit(1u)
        val current = service.dbQuery(topicFilter)
            .first()
            ?.tags()
            ?.hashtags()
            .orEmpty()
            .toSet()
        if (current.isEmpty()) {
            Log.i(logTag, "Topic list of $pubkey is empty or not found")
        } else {
            topics.value = current
        }
    }

    fun topics() = topics.value
}