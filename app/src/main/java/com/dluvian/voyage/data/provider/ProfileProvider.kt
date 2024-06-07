package com.dluvian.voyage.data.provider

import com.dluvian.voyage.core.PubkeyHex
import com.dluvian.voyage.core.launchIO
import com.dluvian.voyage.core.normalizeName
import com.dluvian.voyage.core.toBech32
import com.dluvian.voyage.core.toShortenedBech32
import com.dluvian.voyage.data.account.IPubkeyProvider
import com.dluvian.voyage.data.inMemory.MetadataInMemory
import com.dluvian.voyage.data.model.FullProfileUI
import com.dluvian.voyage.data.model.RelevantMetadata
import com.dluvian.voyage.data.nostr.LazyNostrSubscriber
import com.dluvian.voyage.data.nostr.NostrSubscriber
import com.dluvian.voyage.data.room.dao.ProfileDao
import com.dluvian.voyage.data.room.entity.ProfileEntity
import com.dluvian.voyage.data.room.view.AdvancedProfileView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import rust.nostr.protocol.Nip19Profile

class ProfileProvider(
    private val forcedFollowFlow: Flow<Map<PubkeyHex, Boolean>>,
    private val pubkeyProvider: IPubkeyProvider,
    private val metadataInMemory: MetadataInMemory,
    private val profileDao: ProfileDao,
    private val friendProvider: FriendProvider,
    private val lazyNostrSubscriber: LazyNostrSubscriber,
    private val nostrSubscriber: NostrSubscriber,
    private val annotatedStringProvider: AnnotatedStringProvider,
) {
    private val scope = CoroutineScope(Dispatchers.IO)
    fun getProfileFlow(nprofile: Nip19Profile, isInit: Boolean): Flow<FullProfileUI> {
        val hex = nprofile.publicKey().toHex()
        scope.launchIO {
            lazyNostrSubscriber.lazySubNip65(nprofile = nprofile)
            if (!isInit || metadataInMemory.getMetadata(pubkey = hex) == null) {
                nostrSubscriber.subProfile(nprofile = nprofile)
            }
        }
        return combine(
            profileDao.getAdvancedProfileFlow(pubkey = hex),
            forcedFollowFlow,
            metadataInMemory.getMetadataFlow(pubkey = hex)
        ) { dbProfile, forcedFollows, metadata ->
            createFullProfile(
                pubkey = hex,
                dbProfile = dbProfile,
                forcedFollowState = forcedFollows[hex],
                metadata = metadata
            )
        }
    }

    fun getPersonalProfileFlow(): Flow<ProfileEntity> {
        return combine(
            profileDao.getPersonalProfileFlow(),
            metadataInMemory.getMetadataFlow()
        ) { profile, meta ->
            val nonNull = profile ?: getDefaultProfile()
            val name = nonNull.name
                .ifEmpty { meta[nonNull.pubkey]?.name.orEmpty() }
                .ifEmpty { nonNull.pubkey.toShortenedBech32() }
            nonNull.copy(name = name)
        }
    }

    fun getDefaultProfile(): ProfileEntity {
        val hex = pubkeyProvider.getPubkeyHex()
        return ProfileEntity(
            pubkey = hex,
            name = hex.toShortenedBech32(),
            createdAt = 0L
        )
    }

    suspend fun getProfileByName(name: String, limit: Int): List<AdvancedProfileView> {
        return profileDao.getProfilesByName(name = name, limit = 2 * limit)
            .sortedByDescending { it.isFriend }
            .take(limit)
    }

    suspend fun getPopularUnfollowedProfiles(limit: Int): Flow<List<FullProfileUI>> {
        val unfollowedPubkeys = profileDao.getPopularUnfollowedPubkeys(limit = limit)
            .ifEmpty {
                val default = defaultPubkeys.toMutableSet()
                default.removeAll(friendProvider.getFriendPubkeys().toSet())
                default.remove(pubkeyProvider.getPubkeyHex())
                default
            }
        lazyNostrSubscriber.lazySubUnknownProfiles(pubkeys = unfollowedPubkeys)

        return getProfilesFlow(pubkeys = unfollowedPubkeys)
    }

    suspend fun getMyFriendsFlow(): Flow<List<FullProfileUI>> {
        // We want to be able to unfollow on the same list
        val friends = profileDao.getAdvancedProfilesOfFriends()
        val friendsWithoutProfile = profileDao.getUnknownFriends()

        return forcedFollowFlow.map { forcedFollows ->
            friends.map { friend ->
                createFullProfile(
                    pubkey = friend.pubkey,
                    dbProfile = friend,
                    forcedFollowState = forcedFollows[friend.pubkey],
                    metadata = null
                )
            } + friendsWithoutProfile.map { pubkey ->
                createFullProfile(
                    pubkey = pubkey,
                    dbProfile = null,
                    forcedFollowState = forcedFollows[pubkey],
                    metadata = null
                )
            }
        }
    }

    private fun getProfilesFlow(pubkeys: Collection<PubkeyHex>): Flow<List<FullProfileUI>> {
        if (pubkeys.isEmpty()) return flowOf(emptyList())

        return combine(
            profileDao.getAdvancedProfilesFlow(pubkeys = pubkeys),
            forcedFollowFlow,
        ) { dbProfiles, forcedFollows ->
            dbProfiles.map { dbProfile ->
                createFullProfile(
                    pubkey = dbProfile.pubkey,
                    dbProfile = dbProfile,
                    forcedFollowState = forcedFollows[dbProfile.pubkey],
                    metadata = null
                )
            }
        }
    }

    private fun createFullProfile(
        pubkey: PubkeyHex,
        dbProfile: AdvancedProfileView?,
        forcedFollowState: Boolean?,
        metadata: RelevantMetadata?
    ): FullProfileUI {
        val name = normalizeName(metadata?.name.orEmpty().ifEmpty { dbProfile?.name.orEmpty() })
            .ifEmpty { pubkey.toShortenedBech32() }
        val advancedProfile = AdvancedProfileView(
            pubkey = pubkey,
            name = name,
            isMe = dbProfile?.isMe ?: (pubkeyProvider.getPubkeyHex() == pubkey),
            isFriend = forcedFollowState ?: dbProfile?.isFriend ?: friendProvider.isFriend(pubkey),
            isWebOfTrust = dbProfile?.isWebOfTrust ?: false
        )
        return FullProfileUI(
            inner = advancedProfile,
            npub = pubkey.toBech32(),
            about = metadata?.about?.let { annotatedStringProvider.annotate(it) },
            lightning = metadata?.lightning,
        )
    }

    private val defaultPubkeys = listOf(
        // dluvian
        "e4336cd525df79fa4d3af364fd9600d4b10dce4215aa4c33ed77ea0842344b10",
        // fiatjaf
        "3bf0c63fcb93463407af97a5e5ee64fa883d107ef9e558472c4eb9aaaefa459d",
        // hodlbod
        "97c70a44366a6535c145b333f973ea86dfdc2d7a99da618c40c64705ad98e322",
        // jack
        "82341f882b6eabcd2ba7f1ef90aad961cf074af15b9ef44a09f9d2a8fbfbe6a2",
        // odell
        "04c915daefee38317fa734444acee390a8269fe5810b2241e5e6dd343dfbecc9",
        // pablof7z
        "fa984bd7dbb282f07e16e7ae87b26a2a7b9b90b7246a44771f0cf5ae58018f52",
        // gigi
        "6e468422dfb74a5738702a8823b9b28168abab8655faacb6853cd0ee15deee93",
        // kieran
        "63fe6318dc58583cfe16810f86dd09e18bfd76aabc24a0081ce2856f330504ed",
        // fishcake
        "8fb140b4e8ddef97ce4b821d247278a1a4353362623f64021484b372f948000c",
        // lynalden
        "eab0e756d32b80bcd464f3d844b8040303075a13eabc3599a762c9ac7ab91f4f",
        // yonle
        "347a2370900d19b4e4756221594e8bda706ae5c785de09e59e4605f91a03f49c",
        // gladstein
        "58c741aa630c2da35a56a77c1d05381908bd10504fdd2d8b43f725efa6d23196",
        // tanel
        "5c508c34f58866ec7341aaf10cc1af52e9232bb9f859c8103ca5ecf2aa93bf78",
        // yukikishimoto
        "68d81165918100b7da43fc28f7d1fc12554466e1115886b9e7bb326f65ec4272",
        // greenart7c3
        "7579076d9aff0a4cfdefa7e2045f2486c7e5d8bc63bfc6b45397233e1bbfcb19",
        // preston
        "85080d3bad70ccdcd7f74c29a44f55bb85cbcd3dd0cbb957da1d215bdb931204",
        // unclebob
        "2ef93f01cd2493e04235a6b87b10d3c4a74e2a7eb7c3caf168268f6af73314b5",
        // gsovereignty
        "d91191e30e00444b942c0e82cad470b32af171764c2275bee0bd99377efd4075",
        // american_hodl
        "1afe0c74e3d7784eba93a5e3fa554a6eeb01928d12739ae8ba4832786808e36d",
        // semisol
        "52b4a076bcbbbdc3a1aefa3735816cf74993b1b8db202b01c883c58be7fad8bd",
        // nunyabidness
        "6389be6491e7b693e9f368ece88fcd145f07c068d2c1bbae4247b9b5ef439d32",
        // franzap
        "726a1e261cc6474674e8285e3951b3bb139be9a773d1acf49dc868db861a1c11",
        // max_hillebrand
        "b7ed68b062de6b4a12e51fd5285c1e1e0ed0e5128cda93ab11b4150b55ed32fc",
        // laeserin
        "dd664d5e4016433a8cd69f005ae1480804351789b59de5af06276de65633d319",
        // sommerfeld
        "d0debf9fb12def81f43d7c69429bb784812ac1e4d2d53a202db6aac7ea4b466c",
        // cyph3rp9nk
        "fcf70a45cfa817eaa813b9ba8a375d713d3169f4a27f3dcac3d49112df67d37e",
        // nvk
        "e88a691e98d9987c964521dff60025f60700378a4879180dcbbb4a5027850411",
        // alex_gleason
        "79c2cae114ea28a981e7559b4fe7854a473521a8d22a66bbab9fa248eb820ff6"
    )
}
