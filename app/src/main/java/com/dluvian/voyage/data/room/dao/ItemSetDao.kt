package com.dluvian.voyage.data.room.dao

import androidx.room.Dao
import androidx.room.Query
import com.dluvian.voyage.data.model.ItemSetMeta
import kotlinx.coroutines.flow.Flow


@Dao
interface ItemSetDao {
    @Query(
        "SELECT identifier, title " +
                "FROM profileSet " +
                "WHERE myPubkey = (SELECT pubkey FROM account) AND deleted = 0"
    )
    fun getMyProfileSetMetasFlow(): Flow<List<ItemSetMeta>>

    @Query(
        "SELECT identifier, title " +
                "FROM topicSet " +
                "WHERE myPubkey = (SELECT pubkey FROM account) AND deleted = 0"
    )
    fun getMyTopicSetMetasFlow(): Flow<List<ItemSetMeta>>

    @Query("SELECT title FROM profileSet WHERE identifier = :identifier")
    suspend fun getProfileSetTitle(identifier: String): String?

    @Query("SELECT title FROM topicSet WHERE identifier = :identifier")
    suspend fun getTopicSetTitle(identifier: String): String?
}
