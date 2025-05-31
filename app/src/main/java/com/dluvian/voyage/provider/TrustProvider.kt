package com.dluvian.voyage.provider

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import com.dluvian.voyage.Ident
import com.dluvian.voyage.NostrService
import rust.nostr.sdk.Event
import rust.nostr.sdk.Filter
import rust.nostr.sdk.Kind
import rust.nostr.sdk.KindStandard
import rust.nostr.sdk.PublicKey


class TrustProvider(private val service: NostrService) {
    private val logTag = "TrustProvider"

    // TODO: No mutable State. It should not be used in the UI root as it would trigger too many UI refreshes
    private val friends = mutableStateOf(emptySet<PublicKey>())
    private val lists = mutableStateOf(emptyMap<Ident, Set<PublicKey>>())
    private val web = mutableStateOf(emptyMap<PublicKey, Set<PublicKey>>())

    suspend fun init() {
        val pubkey = service.signer().getPublicKey()
        val friendFilter = Filter()
            .author(pubkey)
            .kind(Kind.fromStd(KindStandard.CONTACT_LIST))
            .limit(1u)
        val current = service.database()
            .query(friendFilter)
            .first()
            ?.tags()
            ?.publicKeys()
            .orEmpty()
            .toSet()
        if (current.isEmpty()) {
            Log.i(logTag, "Contact list of $pubkey is empty or not found")
        } else {
            friends.value = current
        }

        // TODO: Set lists
    }

    override suspend fun updateSigner() {
        TODO("Not yet implemented")
    }

    override suspend fun update(event: Event) {
        TODO("Not yet implemented")
    }

    fun friends() = friends.value
    fun lists() = lists.value
    fun web() = web.value
}