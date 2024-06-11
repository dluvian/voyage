package com.dluvian.voyage.data.room.dao

import androidx.room.Dao
import androidx.room.Query
import com.dluvian.voyage.data.model.ItemSetMeta
import kotlinx.coroutines.flow.Flow

@Dao
interface ItemSetDao {
    @Query("SELECT identifier, title FROM profileSet WHERE myPubkey = (SELECT pubkey FROM account)")
    fun getMyProfileSetMetasFlow(): Flow<List<ItemSetMeta>>

    @Query("SELECT identifier, title FROM topicSet WHERE myPubkey = (SELECT pubkey FROM account)")
    fun getMyTopicSetMetasFlow(): Flow<List<ItemSetMeta>>
}
