package com.dluvian.voyage.core.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dluvian.voyage.data.provider.ProfileProvider
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn

class DrawerViewModel(
    profileProvider: ProfileProvider,
) :
    ViewModel() {
    val personalProfile = profileProvider.getPersonalProfileFlow()
        .stateIn(viewModelScope, SharingStarted.Eagerly, profileProvider.getDefaultProfile())
}
