package com.dluvian.voyage.provider

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import com.dluvian.voyage.Ident
import rust.nostr.sdk.Client
import rust.nostr.sdk.Event
import rust.nostr.sdk.Filter
import rust.nostr.sdk.Kind
import rust.nostr.sdk.KindStandard
import rust.nostr.sdk.PublicKey


class TrustProvider(private val client: Client) : IProvider {
    private val logTag = "TrustProvider"

    // TODO: No mutable State. It should not be used in the UI root as it would trigger too many UI refreshes
    val friends = mutableStateOf(emptySet<PublicKey>())
    val lists = mutableStateOf(emptyMap<Ident, Set<PublicKey>>())
    val web = mutableStateOf(emptyMap<PublicKey, Set<PublicKey>>())

    override suspend fun init() {
        val pubkey = client.signer().getPublicKey()
        val friendFilter = Filter()
            .author(pubkey)
            .kind(Kind.fromStd(KindStandard.CONTACT_LIST))
            .limit(1u)
        val current = client.database()
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