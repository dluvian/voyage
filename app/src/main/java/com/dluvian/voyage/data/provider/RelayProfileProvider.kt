package com.dluvian.voyage.data.provider

import android.util.Log
import com.dluvian.nostr_kt.RelayUrl
import kotlinx.coroutines.delay
import rust.nostr.protocol.RelayInformationDocument
import java.util.Collections

private const val TAG = "RelayProfileProvider"

class RelayProfileProvider {
    private val cache =
        Collections.synchronizedMap(mutableMapOf<RelayUrl, RelayInformationDocument>())

    suspend fun getRelayProfile(httpsUrl: String): RelayInformationDocument? {
        val cached = cache[httpsUrl]
        if (cached != null) return cached

        val fromNetwork = kotlin.runCatching {
            RelayInformationDocument.get(url = httpsUrl, proxy = null)
        }.onFailure {
            Log.w(TAG, "Failed to fetch RelayProfile of $httpsUrl", it)
        }.getOrNull()

        if (fromNetwork != null) cache[httpsUrl] = fromNetwork
        delay(0) // Otherwise suspend marker is redundant

        return fromNetwork
    }
}
