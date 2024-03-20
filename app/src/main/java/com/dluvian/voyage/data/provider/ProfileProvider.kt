package com.dluvian.voyage.data.provider

import com.dluvian.voyage.core.PubkeyHex
import com.dluvian.voyage.core.shortenBech32
import com.dluvian.voyage.data.account.IPubkeyProvider
import com.dluvian.voyage.data.inMemory.MetadataInMemory
import com.dluvian.voyage.data.interactor.ProfileFollower
import com.dluvian.voyage.data.model.FullProfile
import com.dluvian.voyage.data.model.RelevantMetadata
import com.dluvian.voyage.data.room.dao.ProfileDao
import com.dluvian.voyage.data.room.view.AdvancedProfileView
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class ProfileProvider(
    private val profileFollower: ProfileFollower,
    private val pubkeyProvider: IPubkeyProvider,
    private val metadataInMemory: MetadataInMemory,
    private val profileDao: ProfileDao,
) {
    fun getProfileFlow(pubkey: PubkeyHex): Flow<FullProfile> {
        return combine(
            profileDao.getAdvancedProfileFlow(pubkey = pubkey),
            profileFollower.forcedFollows,
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

    private fun createFullProfile(
        pubkey: PubkeyHex,
        dbProfile: AdvancedProfileView?,
        forcedFollowState: Boolean?,
        metadata: RelevantMetadata?
    ): FullProfile {
        val advancedProfile = AdvancedProfileView(
            pubkey = pubkey,
            name = dbProfile?.name.orEmpty().ifEmpty { pubkey.shortenBech32() },
            isFriend = forcedFollowState ?: dbProfile?.isFriend ?: false,
            isWebOfTrust = dbProfile?.isWebOfTrust ?: false,
            isMe = dbProfile?.isMe ?: (pubkeyProvider.getPubkeyHex() == pubkey)
        )
        return FullProfile(
            advancedProfile = advancedProfile,
            about = metadata?.about
        )
    }
}
