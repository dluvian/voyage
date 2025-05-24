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

    @Query(
        "SELECT * " +
                "FROM AdvancedProfileView " +
                "WHERE pubkey = (SELECT friendPubkey FROM weboftrust WHERE webOfTrustPubkey = :pubkey)"
    )
    fun getAdvancedProfileTrustedByFlow(pubkey: PubkeyHex): Flow<AdvancedProfileView?>

    // From account table in case we switch the account during a session
    @Query(
        "SELECT account.pubkey, IFNULL(profile.name, '') name, IFNULL(profile.createdAt, 0) createdAt " +
                "FROM account " +
                "LEFT JOIN profile ON account.pubkey = profile.pubkey " +
                "LIMIT 1"
    )
    fun getPersonalProfileFlow(): Flow<ProfileEntity?>

    @Query("SELECT * FROM AdvancedProfileView WHERE pubkey IN (:pubkeys)")
    fun getAdvancedProfilesFlow(pubkeys: Collection<PubkeyHex>): Flow<List<AdvancedProfileView>>

    @Query("SELECT * FROM AdvancedProfileView WHERE pubkey IN (SELECT friendPubkey FROM friend)")
    suspend fun getAdvancedProfilesOfFriends(): List<AdvancedProfileView>

    @Query(
        "SELECT * " +
                "FROM AdvancedProfileView " +
                "WHERE pubkey IN (SELECT pubkey FROM profileSetItem WHERE identifier = :identifier)"
    )
    suspend fun getAdvancedProfilesOfList(identifier: String): List<AdvancedProfileView>

    @Query(
        "SELECT pubkey " +
                "FROM profileSetItem " +
                "WHERE identifier = :identifier " +
                "AND pubkey NOT IN (SELECT pubkey FROM profile)"
    )
    suspend fun getUnknownPubkeysFromList(identifier: String): List<PubkeyHex>

    @Query("SELECT name FROM profile WHERE pubkey = :pubkey")
    suspend fun getName(pubkey: PubkeyHex): String?

    @Query("SELECT createdAt FROM profile WHERE pubkey = :pubkey")
    suspend fun getMaxCreatedAt(pubkey: PubkeyHex): Long?

    suspend fun getProfilesByName(name: String, limit: Int): List<AdvancedProfileView> {
        if (limit <= 0) return emptyList()

        return internalGetProfilesWithNameLike(name = name, somewhere = "%$name%", limit = limit)
    }

    @Query(
        "SELECT DISTINCT pubkey AS pk " +
                "FROM mainEvent " +
                "WHERE pk NOT IN (SELECT friendPubkey FROM friend) " +
                "AND pk NOT IN (SELECT pubkey FROM account) " +
                "AND pk IN (SELECT webOfTrustPubkey FROM weboftrust) " +
                "AND pk NOT IN (SELECT pubkey FROM profileSetItem) " +
                "GROUP BY pk " +
                "ORDER BY COUNT(pk) DESC " +
                "LIMIT :limit"
    )
    suspend fun getPopularUnfollowedPubkeys(limit: Int): List<PubkeyHex>

    @Query(
        "SELECT DISTINCT pubkey " +
                "FROM profile " +
                "WHERE pubkey IN (:pubkeys)"
    )
    suspend fun filterKnownProfiles(pubkeys: Collection<PubkeyHex>): List<PubkeyHex>

    @Query(
        "SELECT friendPubkey " +
                "FROM friend " +
                "WHERE friendPubkey NOT IN (SELECT pubkey FROM profile)"
    )
    suspend fun getUnknownFriends(): List<PubkeyHex>

    @Query(
        "SELECT * " +
                "FROM AdvancedProfileView " +
                "WHERE (name = :name OR name LIKE :somewhere) " +
                "AND name != '' " +
                "ORDER BY length(name) ASC " +
                "LIMIT :limit"
    )
    suspend fun internalGetProfilesWithNameLike(
        name: String,
        somewhere: String,
        limit: Int
    ): List<AdvancedProfileView>
}
