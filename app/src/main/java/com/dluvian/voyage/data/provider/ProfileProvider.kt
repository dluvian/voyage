package com.dluvian.voyage.data.provider

import com.dluvian.voyage.core.PubkeyHex
import com.dluvian.voyage.core.toShortenedBech32
import com.dluvian.voyage.data.account.IPubkeyProvider
import com.dluvian.voyage.data.inMemory.MetadataInMemory
import com.dluvian.voyage.data.model.FullProfileUI
import com.dluvian.voyage.data.model.RelevantMetadata
import com.dluvian.voyage.data.nostr.LazyNostrSubscriber
import com.dluvian.voyage.data.room.dao.ProfileDao
import com.dluvian.voyage.data.room.entity.ProfileEntity
import com.dluvian.voyage.data.room.view.AdvancedProfileView
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf

class ProfileProvider(
    private val forcedFollowFlow: Flow<Map<PubkeyHex /* = String */, Boolean>>,
    private val pubkeyProvider: IPubkeyProvider,
    private val metadataInMemory: MetadataInMemory,
    private val profileDao: ProfileDao,
    private val friendProvider: FriendProvider,
    private val lazyNostrSubscriber: LazyNostrSubscriber,
    private val annotatedStringProvider: AnnotatedStringProvider,
) {
    fun getProfileFlow(pubkey: PubkeyHex): Flow<FullProfileUI> {
        return combine(
            profileDao.getAdvancedProfileFlow(pubkey = pubkey),
            forcedFollowFlow,
            metadataInMemory.getMetadataFlow(pubkey = pubkey)
        ) { dbProfile, forcedFollows, metadata ->
            createFullProfile(
                pubkey = pubkey,
                dbProfile = dbProfile,
                forcedFollowState = forcedFollows[pubkey],
                metadata = metadata
            )
        }
    }

    suspend fun getProfileByName(name: String, limit: Int): List<ProfileEntity> {
        return profileDao.getProfilesByName(name = name, limit = limit)
    }

    suspend fun getPopularUnfollowedProfiles(limit: Int): Flow<List<FullProfileUI>> {
        val unfollowedPubkeys = profileDao.getPopularUnfollowedPubkeys(limit = limit)
            .ifEmpty {
                val default = defaultPubkeys.toMutableSet()
                default.removeAll(friendProvider.getFriendPubkeys().toSet())
                default.remove(pubkeyProvider.getPubkeyHex())
                default
            }
        lazyNostrSubscriber.lazySubProfiles(pubkeys = unfollowedPubkeys)

        return getProfilesFlow(pubkeys = unfollowedPubkeys)
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
        val advancedProfile = AdvancedProfileView(
            pubkey = pubkey,
            name = dbProfile?.name.orEmpty().ifEmpty { pubkey.toShortenedBech32() },
            isMe = dbProfile?.isMe ?: (pubkeyProvider.getPubkeyHex() == pubkey),
            isFriend = forcedFollowState ?: dbProfile?.isFriend ?: false,
            isWebOfTrust = dbProfile?.isWebOfTrust ?: false
        )
        return FullProfileUI(
            inner = advancedProfile,
            about = metadata?.about?.let { annotatedStringProvider.annotate(it) }
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
        // nostreport
        "2edbcea694d164629854a52583458fd6d965b161e3c48b57d3aff01940558884",
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
    )
}
