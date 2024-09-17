package com.dluvian.voyage.data.room.dao.util

import androidx.room.Dao
import androidx.room.Query
import com.dluvian.voyage.data.nostr.RelayUrl
import kotlinx.coroutines.flow.Flow

@Dao
interface CountDao {
    @Query("SELECT COUNT(*) FROM post WHERE parentId IS NULL")
    fun countRootPostsFlow(): Flow<Int>

    @Query("SELECT COUNT(*) FROM post WHERE relayUrl = :relayUrl")
    fun countEventRelaysFlow(relayUrl: RelayUrl): Flow<Int>

    @Query("SELECT COUNT(*) FROM post")
    suspend fun countAllPosts(): Int
}
