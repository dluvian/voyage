package com.dluvian.voyage.data.room.dao.util

import androidx.room.Dao
import androidx.room.Query
import com.dluvian.voyage.data.nostr.RelayUrl
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

@Dao
interface CountDao {
    @Query("SELECT COUNT(*) FROM rootPost")
    fun countRootPostsFlow(): Flow<Int>

    fun countEventRelaysFlow(relayUrl: RelayUrl): Flow<Int> {
        return combine(
            internalCountRootPostRelaysFlow(relayUrl = relayUrl),
            internalCountLegacyReplyRelaysFlow(relayUrl = relayUrl),
        ) { rootCount, legacyReplyCount -> rootCount + legacyReplyCount }
    }

    suspend fun countAllPosts(): Int {
        return internalCountRootPosts() + internalCountLegacyReplies()
    }

    @Query("SELECT COUNT(*) FROM rootPost WHERE relayUrl = :relayUrl")
    fun internalCountRootPostRelaysFlow(relayUrl: RelayUrl): Flow<Int>

    @Query("SELECT COUNT(*) FROM legacyReply WHERE relayUrl = :relayUrl")
    fun internalCountLegacyReplyRelaysFlow(relayUrl: RelayUrl): Flow<Int>

    @Query("SELECT COUNT(*) FROM rootPost")
    fun internalCountRootPosts(): Int

    @Query("SELECT COUNT(*) FROM legacyReply")
    fun internalCountLegacyReplies(): Int
}
