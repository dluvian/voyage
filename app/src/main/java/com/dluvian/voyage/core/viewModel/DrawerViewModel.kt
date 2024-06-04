package com.dluvian.voyage.core.viewModel

import androidx.compose.material3.DrawerState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dluvian.voyage.core.CloseDrawer
import com.dluvian.voyage.core.DrawerAction
import com.dluvian.voyage.core.OpenDrawer
import com.dluvian.voyage.data.provider.ProfileProvider
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class DrawerViewModel(
    profileProvider: ProfileProvider,
    val drawerState: DrawerState,
) :
    ViewModel() {
    val personalProfile = profileProvider.getPersonalProfileFlow()
        .stateIn(viewModelScope, SharingStarted.Eagerly, profileProvider.getDefaultProfile())

    fun handle(action: DrawerAction) {
        when (action) {
            is OpenDrawer -> action.scope.launch {
                drawerState.open()
            }

            is CloseDrawer -> action.scope.launch {
                drawerState.close()
            }
        }
    }
}
