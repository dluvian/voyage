package com.dluvian.voyage

import com.dluvian.voyage.nostr.NostrService
import com.dluvian.voyage.provider.OldestUsedTimestampProvider
import rust.nostr.sdk.Filter
import rust.nostr.sdk.Kind
import rust.nostr.sdk.KindStandard
import rust.nostr.sdk.Timestamp

class DatabaseSweeper(
    private val service: NostrService,
    private val oldestUsedTimestampProvider: OldestUsedTimestampProvider
) {
    suspend fun sweep() {
        val kinds = listOf(
            KindStandard.TEXT_NOTE,
            KindStandard.REPOST,
            KindStandard.GENERIC_REPOST,
            KindStandard.COMMENT,
            KindStandard.REACTION
        )
            .map { Kind.fromStd(it) }

        val untilSecs = Timestamp.now().asSecs() - DB_SWEEP_THRESHOLD.toUInt()
        val oldestSecs = oldestUsedTimestampProvider.createdAt().asSecs()
        val until = Timestamp.fromSecs(minOf(untilSecs, oldestSecs))

        val deletion = Filter().kinds(kinds).until(until)

        service.dbDelete(deletion)
    }
}
