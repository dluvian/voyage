package com.dluvian.voyage.provider

import android.util.Log
import com.dluvian.voyage.Ident
import com.dluvian.voyage.NostrService
import rust.nostr.sdk.Filter
import rust.nostr.sdk.Kind
import rust.nostr.sdk.KindStandard
import rust.nostr.sdk.PublicKey


class TrustProvider(private val service: NostrService) {
    private val logTag = "TrustProvider"
    // TODO: Switch signer
    // TODO: Listen to database events

    // TODO: No mutable State. It should not be used in the UI root as it would trigger too many UI refreshes
    private var friends = mutableSetOf<PublicKey>()
    private var lists = mutableMapOf<Ident, Set<PublicKey>>()
    private var web = mutableMapOf<PublicKey, Set<PublicKey>>()

    suspend fun init() {
        val pubkey = service.pubkey()
        val friendFilter = Filter()
            .author(pubkey)
            .kind(Kind.fromStd(KindStandard.CONTACT_LIST))
            .limit(1u)
        val current = service.dbQuery(friendFilter)
            .first()
            ?.tags()
            ?.publicKeys()
            .orEmpty()
            .toSet()
        if (current.isEmpty()) {
            Log.i(logTag, "Contact list of $pubkey is empty or not found")
        } else {
            friends.addAll(current)
        }

        // TODO: Set lists
    }


    fun friends() = friends.toList()
    fun lists() = lists.toMap()
    fun web() = web.toMap()
}