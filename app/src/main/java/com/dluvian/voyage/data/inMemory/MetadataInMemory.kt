package com.dluvian.voyage.data.inMemory

import com.dluvian.voyage.core.DEBOUNCE
import com.dluvian.voyage.core.PubkeyHex
import com.dluvian.voyage.data.model.RelevantMetadata
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.util.Collections

class MetadataInMemory {
    private val map = Collections.synchronizedMap(mutableMapOf<PubkeyHex, RelevantMetadata>())

    fun submit(pubkey: PubkeyHex, metadata: RelevantMetadata) {
        val alreadyPresent = map.putIfAbsent(pubkey, metadata)
        if (alreadyPresent != null && metadata.createdAt > alreadyPresent.createdAt) {
            map[pubkey] = metadata
        }
    }

    fun getMetadata(pubkey: PubkeyHex): RelevantMetadata? {
        return map[pubkey]
    }

    fun getMetadataFlow(pubkey: PubkeyHex): Flow<RelevantMetadata?> {
        return flow {
            var lastMetadata = map[pubkey]
            emit(lastMetadata)

            while (true) {
                delay(DEBOUNCE)
                val newMetadata = map[pubkey]
                if (newMetadata != lastMetadata) {
                    lastMetadata = newMetadata
                    emit(newMetadata)
                }
            }
        }
    }
}
