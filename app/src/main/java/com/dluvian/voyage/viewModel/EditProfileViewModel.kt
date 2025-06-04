package com.dluvian.voyage.viewModel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dluvian.voyage.model.EditProfileViewCmd
import com.dluvian.voyage.model.ShowEditProfileView
import com.dluvian.voyage.provider.ProfileProvider
import kotlinx.coroutines.launch
import rust.nostr.sdk.Metadata


class EditProfileViewModel(
    private val profileProvider: ProfileProvider,
) : ViewModel() {
    val profile = mutableStateOf<Metadata>(Metadata())

    fun handle(action: EditProfileViewCmd) {
        when (action) {
            ShowEditProfileView -> viewModelScope.launch {
                profile.value = profileProvider.myProfile() ?: Metadata()
            }
        }
    }
}
