package com.dluvian.voyage.data.provider

import android.util.Log
import com.dluvian.nostr_kt.RelayUrl
import rust.nostr.protocol.RelayInformationDocument
import rust.nostr.protocol.nip11GetInformationDocument
import java.util.Collections

private const val TAG = "RelayProfileProvider"

class RelayProfileProvider {
    private val cache =
        Collections.synchronizedMap(mutableMapOf<RelayUrl, RelayInformationDocument>())

    suspend fun getRelayProfile(httpsUrl: String): RelayInformationDocument? {
        val cached = cache[httpsUrl]
        if (cached != null) return cached

        val fromNetwork = kotlin.runCatching {
            nip11GetInformationDocument(url = httpsUrl)
        }.onFailure {
            Log.w(TAG, "Failed to fetch RelayProfile of $httpsUrl", it)
        }.getOrNull()

        if (fromNetwork != null) cache[httpsUrl] = fromNetwork

        return fromNetwork
    }
}
