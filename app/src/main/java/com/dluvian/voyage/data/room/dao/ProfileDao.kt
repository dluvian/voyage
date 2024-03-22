package com.dluvian.voyage.data.room.dao

import androidx.room.Dao
import androidx.room.Query
import com.dluvian.voyage.core.PubkeyHex
import com.dluvian.voyage.data.room.entity.ProfileEntity
import com.dluvian.voyage.data.room.view.AdvancedProfileView
import kotlinx.coroutines.flow.Flow

@Dao
interface ProfileDao {
    @Query("SELECT * FROM AdvancedProfileView WHERE pubkey = :pubkey")
    fun getAdvancedProfileFlow(pubkey: PubkeyHex): Flow<AdvancedProfileView?>

    @Query("SELECT * FROM AdvancedProfileView WHERE pubkey IN (:pubkeys)")
    fun getAdvancedProfilesFlow(pubkeys: Collection<PubkeyHex>): Flow<List<AdvancedProfileView>>

    suspend fun getProfilesByName(name: String, limit: Int): List<ProfileEntity> {
        if (limit <= 0) return emptyList()

        return internalGetProfilesByName(name = "$name%", limit = limit)
    }

    @Query(
        "SELECT DISTINCT pubkey AS pk " +
                "FROM post " +
                "WHERE pk NOT IN (SELECT friendPubkey FROM friend) " +
                "GROUP BY pk " +
                "ORDER BY COUNT(pk) DESC, (SELECT EXISTS (SELECT * FROM weboftrust WHERE webOfTrustPubkey = pk)) DESC " +
                "LIMIT :limit"
    )
    suspend fun getPopularUnfollowedPubkeys(limit: Int): List<PubkeyHex>

    @Query(
        "SELECT DISTINCT pubkey " +
                "FROM profile " +
                "WHERE pubkey IN (:pubkeys)"
    )
    suspend fun filterKnownProfiles(pubkeys: Collection<PubkeyHex>): List<PubkeyHex>

    @Query("SELECT * FROM profile WHERE name LIKE :name LIMIT :limit ")
    suspend fun internalGetProfilesByName(name: String, limit: Int): List<ProfileEntity>
}
