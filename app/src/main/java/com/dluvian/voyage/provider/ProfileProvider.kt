package com.dluvian.voyage.provider

import android.util.Log
import com.dluvian.voyage.nostr.NostrService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import rust.nostr.sdk.Filter
import rust.nostr.sdk.Kind
import rust.nostr.sdk.KindStandard
import rust.nostr.sdk.Metadata
import rust.nostr.sdk.PublicKey
import rust.nostr.sdk.Timestamp

class ProfileProvider(private val service: NostrService) {
    private val logTag = "ProfileProvider"
    private val scope = CoroutineScope(Dispatchers.IO)

    suspend fun myProfile(subNew: Boolean = true): Metadata? {
        val pubkey = service.pubkey()

        return profile(pubkey, subNew)
    }

    suspend fun profile(pubkey: PublicKey, subNew: Boolean = true): Metadata? {
        val filter = Filter().author(pubkey).kind(Kind.fromStd(KindStandard.METADATA)).limit(1u)
        val dbProfile = service.dbQuery(filter).firstOrNull()
        if (dbProfile == null) {
            Log.i(logTag, "No profile event in database")
            if (subNew) scope.launch {
                service.subscribe(filter)
            }

            return null
        }

        val metadata = runCatching { Metadata.fromJson(dbProfile.content()) }.getOrNull()
        if (metadata == null) Log.i(logTag, "Invalid profile event in database")
        if (subNew) {
            val since = dbProfile.createdAt().asSecs() + 1u
            scope.launch {
                service.subscribe(filter.since(Timestamp.fromSecs(since)))
            }
        }

        return metadata
    }
}