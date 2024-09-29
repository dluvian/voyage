package com.dluvian.voyage.core.viewModel

import androidx.compose.material3.DrawerState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dluvian.voyage.core.CloseDrawer
import com.dluvian.voyage.core.DELAY_10SEC
import com.dluvian.voyage.core.DrawerViewAction
import com.dluvian.voyage.core.DrawerViewSubscribeSets
import com.dluvian.voyage.core.OpenDrawer
import com.dluvian.voyage.core.utils.launchIO
import com.dluvian.voyage.data.nostr.LazyNostrSubscriber
import com.dluvian.voyage.data.provider.ItemSetProvider
import com.dluvian.voyage.data.provider.ProfileProvider
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class DrawerViewModel(
    profileProvider: ProfileProvider,
    itemSetProvider: ItemSetProvider,
    val drawerState: DrawerState,
    private val lazyNostrSubscriber: LazyNostrSubscriber,
) :
    ViewModel() {
    val personalProfile = profileProvider.getPersonalProfileFlow()
        .stateIn(viewModelScope, SharingStarted.Eagerly, profileProvider.getDefaultProfile())
    val itemSetMetas = itemSetProvider.getMySetsFlow()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    fun handle(action: DrawerViewAction) {
        when (action) {
            is OpenDrawer -> action.scope.launch {
                drawerState.open()
            }

            is CloseDrawer -> action.scope.launch {
                drawerState.close()
            }

            DrawerViewSubscribeSets -> subSets()
        }
    }

    var job: Job? = null
    private fun subSets() {
        if (job?.isActive == true) return
        job = viewModelScope.launchIO {
            lazyNostrSubscriber.lazySubMySets()
            delay(DELAY_10SEC)
        }
    }
}
