package com.dluvian.voyage.data.interactor

import android.content.Context
import android.util.Log
import androidx.compose.material3.SnackbarHostState
import com.dluvian.nostr_kt.secs
import com.dluvian.voyage.R
import com.dluvian.voyage.core.FollowProfile
import com.dluvian.voyage.core.LIST_CHANGE_DEBOUNCE
import com.dluvian.voyage.core.ProfileEvent
import com.dluvian.voyage.core.PubkeyHex
import com.dluvian.voyage.core.SignerLauncher
import com.dluvian.voyage.core.UnfollowProfile
import com.dluvian.voyage.core.launchIO
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

private const val TAG = "ProfileFollower"

class ProfileFollower(
    private val nostrService: NostrService,
    private val relayProvider: RelayProvider,
    private val snackbar: SnackbarHostState,
    private val context: Context,
    private val friendProvider: FriendProvider,
    private val friendUpsertDao: FriendUpsertDao,
) {
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

    private var signerLauncher: SignerLauncher? = null

    private fun handleAction(
        pubkey: PubkeyHex,
        isFollowed: Boolean,
        signerLauncher: SignerLauncher
    ) {
        updateForcedFollows(pubkey = pubkey, isFollowed = isFollowed)
        this.signerLauncher = signerLauncher
        handleFollowsInBackground()
    }

    private fun updateForcedFollows(pubkey: PubkeyHex, isFollowed: Boolean) {
        synchronized(_forcedFollows) {
            val mutable = _forcedFollows.value.toMutableMap()
            mutable[pubkey] = isFollowed
            _forcedFollows.value = mutable
        }
    }

    private var job: Job? = null
    private fun handleFollowsInBackground() {
        val nonNullSignerLauncher = signerLauncher ?: return
        if (job?.isActive == true) return
        job = scope.launchIO {
            delay(LIST_CHANGE_DEBOUNCE)

            val toHandle: Map<PubkeyHex, Boolean>
            synchronized(_forcedFollows) {
                toHandle = _forcedFollows.value.toMap()
            }

            val friendsBefore = friendProvider.getFriendPubkeys().toSet()
            val friendsAdjusted = friendsBefore.toMutableSet()
            val toAdd = toHandle.filter { (_, bool) -> bool }.map { (pubkey, _) -> pubkey }
            friendsAdjusted.addAll(toAdd)
            val toRemove = toHandle.filter { (_, bool) -> !bool }.map { (pubkey, _) -> pubkey }
            friendsAdjusted.removeAll(toRemove.toSet())

            if (friendsAdjusted == friendsBefore) return@launchIO

            nostrService.publishContactList(
                pubkeys = friendsAdjusted.toList(),
                relayUrls = relayProvider.getPublishRelays(),
                signerLauncher = nonNullSignerLauncher,
            ).onSuccess { event ->
                val friendList = ValidatedContactList(
                    pubkey = event.author().toHex(),
                    friendPubkeys = event.publicKeys().map { it.toHex() }.toSet(),
                    createdAt = event.createdAt().secs()
                )
                friendUpsertDao.upsertFriends(validatedContactList = friendList)
            }
                .onFailure {
                    Log.w(TAG, "Failed to publish friend list: ${it.message}", it)
                    snackbar.showToast(
                        scope = scope,
                        msg = context.getString(R.string.failed_to_sign_contact_list)
                    )
                }
        }
    }
}