package com.dluvian.voyage.data.room.dao

import androidx.room.Dao
import androidx.room.Query
import com.dluvian.voyage.core.PubkeyHex
import com.dluvian.voyage.core.Topic
import com.dluvian.voyage.data.model.ItemSetMeta
import com.dluvian.voyage.data.room.entity.helper.TitleAndDescription
import kotlinx.coroutines.flow.Flow

private const val LIST_PAIR_NOT_EMPTY = "AND (" +
        "identifier IN (SELECT identifier FROM profileSetItem) " +
        "OR " +
        "identifier IN (SELECT identifier FROM topicSetItem)" +
        ")"

@Dao
interface ItemSetDao {
    @Query(
        "SELECT identifier, title " +
                "FROM profileSet " +
                "WHERE myPubkey = (SELECT pubkey FROM account) " +
                "AND deleted = 0 " +
                "AND identifier IN (SELECT identifier FROM profileSetItem)"
    )
    fun getMyProfileSetMetasFlow(): Flow<List<ItemSetMeta>>

    @Query(
        "SELECT identifier, title " +
                "FROM topicSet " +
                "WHERE myPubkey = (SELECT pubkey FROM account) " +
                "AND deleted = 0 " +
                "AND identifier IN (SELECT identifier FROM topicSetItem)"
    )
    fun getMyTopicSetMetasFlow(): Flow<List<ItemSetMeta>>

    @Query(
        "SELECT identifier, title " +
                "FROM profileSet " +
                "WHERE deleted = 0 " +
                "AND identifier NOT IN (SELECT identifier FROM profileSetItem WHERE pubkey = :pubkey) " +
                LIST_PAIR_NOT_EMPTY
    )
    suspend fun getAddableProfileSets(pubkey: PubkeyHex): List<ItemSetMeta>

    @Query(
        "SELECT identifier, title " +
                "FROM profileSet " +
                "WHERE deleted = 0 " +
                "AND identifier IN (SELECT identifier FROM profileSetItem WHERE pubkey = :pubkey) " +
                LIST_PAIR_NOT_EMPTY
    )
    suspend fun getNonAddableProfileSets(pubkey: PubkeyHex): List<ItemSetMeta>

    @Query(
        "SELECT identifier, title " +
                "FROM topicSet " +
                "WHERE deleted = 0 " +
                "AND identifier NOT IN (SELECT identifier FROM topicSetItem WHERE topic = :topic) " +
                LIST_PAIR_NOT_EMPTY
    )
    suspend fun getAddableTopicSets(topic: Topic): List<ItemSetMeta>

    @Query(
        "SELECT identifier, title " +
                "FROM topicSet " +
                "WHERE deleted = 0 " +
                "AND identifier IN (SELECT identifier FROM topicSetItem WHERE topic = :topic) " +
                LIST_PAIR_NOT_EMPTY
    )
    suspend fun getNonAddableTopicSets(topic: Topic): List<ItemSetMeta>

    @Query("SELECT title, description FROM profileSet WHERE identifier = :identifier")
    suspend fun getProfileSetTitleAndDescription(identifier: String): TitleAndDescription?

    @Query("SELECT title, description FROM topicSet WHERE identifier = :identifier")
    suspend fun getTopicSetTitleAndDescription(identifier: String): TitleAndDescription?

    @Query("SELECT DISTINCT pubkey FROM profileSetItem WHERE identifier = :identifier LIMIT :limit")
    suspend fun getPubkeys(identifier: String, limit: Int): List<PubkeyHex>

    @Query("SELECT DISTINCT pubkey FROM profileSetItem")
    fun getAllPubkeysFlow(): Flow<List<PubkeyHex>>

    @Query(
        "SELECT pubkey " +
                "FROM profileSetItem " +
                "WHERE identifier = :identifier " +
                "AND pubkey NOT IN (SELECT pubkey FROM nip65) " +
                "AND pubkey NOT IN (SELECT pubkey FROM lock)"
    )
    suspend fun getPubkeysWithMissingNip65(identifier: String): List<PubkeyHex>
}
