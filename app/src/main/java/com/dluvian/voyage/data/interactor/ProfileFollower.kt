package com.dluvian.voyage.data.interactor

import android.content.Context
import android.util.Log
import androidx.compose.material3.SnackbarHostState
import com.dluvian.nostr_kt.secs
import com.dluvian.voyage.R
import com.dluvian.voyage.core.DEBOUNCE
import com.dluvian.voyage.core.FollowProfile
import com.dluvian.voyage.core.ProfileEvent
import com.dluvian.voyage.core.PubkeyHex
import com.dluvian.voyage.core.SignerLauncher
import com.dluvian.voyage.core.UnfollowProfile
import com.dluvian.voyage.core.showToast
import com.dluvian.voyage.data.event.ValidatedContactList
import com.dluvian.voyage.data.nostr.NostrService
import com.dluvian.voyage.data.provider.FriendProvider
import com.dluvian.voyage.data.provider.RelayProvider
import com.dluvian.voyage.data.room.dao.tx.FriendUpsertDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException

class ProfileFollower(
    private val nostrService: NostrService,
    private val relayProvider: RelayProvider,
    private val snackbar: SnackbarHostState,
    private val context: Context,
    private val friendProvider: FriendProvider,
    private val friendUpsertDao: FriendUpsertDao,
) {
    private val tag = "ProfileFollower"
    private val scope = CoroutineScope(Dispatchers.IO)
    private val _forcedFollows = MutableStateFlow(mapOf<PubkeyHex, Boolean>())
    val forcedFollowsFlow = _forcedFollows.stateIn(
        scope,
        SharingStarted.Eagerly,
        _forcedFollows.value
    )

    fun handle(action: ProfileEvent) {
        when (action) {
            is FollowProfile -> handleAction(
                pubkey = action.pubkey,
                isFollowed = true,
                signerLauncher = action.signerLauncher
            )

            is UnfollowProfile -> handleAction(
                pubkey = action.pubkey,
                isFollowed = false,
                signerLauncher = action.signerLauncher
            )
        }
    }


    private val jobs: MutableMap<PubkeyHex, Job?> = mutableMapOf()
    private fun handleAction(
        pubkey: PubkeyHex,
        isFollowed: Boolean,
        signerLauncher: SignerLauncher
    ) {
        updateForcedFollows(pubkey = pubkey, isFollowed = isFollowed)

        jobs[pubkey]?.cancel(CancellationException("User clicks fast"))
        jobs[pubkey] = scope.launch {
            delay(DEBOUNCE)

            val allFriends = friendProvider.getFriendPubkeys().toMutableSet()
            if (isFollowed) allFriends.add(pubkey) else allFriends.remove(pubkey)

            nostrService.publishContactList(
                pubkeys = allFriends.toList(),
                relayUrls = relayProvider.getPublishRelays(),
                signerLauncher = signerLauncher,
            ).onSuccess { event ->
                val friendList = ValidatedContactList(
                    pubkey = event.author().toHex(),
                    friendPubkeys = event.publicKeys().map { it.toHex() }.toSet(),
                    createdAt = event.createdAt().secs()
                )
                friendUpsertDao.upsertFriends(validatedContactList = friendList)
            }
                .onFailure {
                    Log.w(tag, "Failed to publish friend list: ${it.message}", it)
                    snackbar.showToast(
                        scope = scope,
                        msg = context.getString(R.string.failed_to_sign_contact_list)
                    )
                }
        }
    }

    private fun updateForcedFollows(pubkey: PubkeyHex, isFollowed: Boolean) {
        synchronized(_forcedFollows) {
            val mutable = _forcedFollows.value.toMutableMap()
            mutable[pubkey] = isFollowed
            _forcedFollows.value = mutable
        }
    }
}