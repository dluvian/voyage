package com.dluvian.voyage.core.viewModel

import android.util.Log
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dluvian.nostr_kt.getMetadata
import com.dluvian.nostr_kt.secs
import com.dluvian.voyage.R
import com.dluvian.voyage.core.DELAY_1SEC
import com.dluvian.voyage.core.EditProfileViewAction
import com.dluvian.voyage.core.LoadFullProfile
import com.dluvian.voyage.core.SaveProfile
import com.dluvian.voyage.core.launchIO
import com.dluvian.voyage.core.showToast
import com.dluvian.voyage.core.toRelevantMetadata
import com.dluvian.voyage.data.inMemory.MetadataInMemory
import com.dluvian.voyage.data.nostr.NostrService
import com.dluvian.voyage.data.provider.RelayProvider
import com.dluvian.voyage.data.room.dao.FullProfileDao
import com.dluvian.voyage.data.room.dao.tx.FullProfileUpsertDao
import com.dluvian.voyage.data.room.entity.FullProfileEntity
import kotlinx.coroutines.delay

private const val TAG = "EditProfileViewModel"

class EditProfileViewModel(
    private val fullProfileUpsertDao: FullProfileUpsertDao,
    private val nostrService: NostrService,
    private val snackbar: SnackbarHostState,
    private val relayProvider: RelayProvider,
    private val fullProfileDao: FullProfileDao,
    private val metadataInMemory: MetadataInMemory,
) : ViewModel() {
    val isSaving = mutableStateOf(false)
    val fullProfile = mutableStateOf<FullProfileEntity?>(null)

    fun handle(action: EditProfileViewAction) {
        when (action) {
            is LoadFullProfile -> loadProfile()
            is SaveProfile -> saveProfile(action = action)
        }
    }

    private fun loadProfile() {
        viewModelScope.launchIO {
            fullProfile.value = fullProfileDao.getFullProfile()
        }
    }

    private fun saveProfile(action: SaveProfile) {
        if (isSaving.value) return

        isSaving.value = true
        viewModelScope.launchIO {
            delay(DELAY_1SEC)
            action.onGoBack()
            nostrService.publishProfile(
                metadata = action.metadata,
                relayUrls = relayProvider.getPublishRelays(),
                signerLauncher = action.signerLauncher
            )
                .onSuccess {
                    snackbar.showToast(
                        viewModelScope,
                        action.context.getString(R.string.profile_updated)
                    )
                    val entity = FullProfileEntity.from(event = it)
                    if (entity != null) {
                        fullProfileUpsertDao.upsertProfile(profile = entity)
                        fullProfile.value = entity
                        it.getMetadata()?.let { metadata ->
                            metadataInMemory.submit(
                                pubkey = entity.pubkey,
                                metadata = metadata.toRelevantMetadata(it.createdAt().secs())
                            )
                        }
                    } else Log.w(TAG, "Failed to create FullProfileEntity from event")

                }.onFailure {
                    snackbar.showToast(
                        viewModelScope,
                        action.context.getString(R.string.failed_to_sign_profile)
                    )
                }
        }.invokeOnCompletion { isSaving.value = false }
    }
}
