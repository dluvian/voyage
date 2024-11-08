package com.dluvian.voyage.data.provider

import android.util.Log
import com.dluvian.voyage.data.nostr.RelayUrl
import rust.nostr.sdk.RelayInformationDocument
import rust.nostr.sdk.nip11GetInformationDocument
import java.util.Collections

private const val TAG = "RelayProfileProvider"

class RelayProfileProvider {
    private val cache =
        Collections.synchronizedMap(mutableMapOf<RelayUrl, RelayInformationDocument>())

    suspend fun getRelayProfile(url: String): RelayInformationDocument? {
        val cached = cache[url]
        if (cached != null) return cached

        val fromNetwork = kotlin.runCatching {
            nip11GetInformationDocument(url = url)
        }.onFailure {
            Log.w(TAG, "Failed to fetch RelayProfile of $url", it)
        }.getOrNull()

        if (fromNetwork != null) cache[url] = fromNetwork

        return fromNetwork
    }
}
