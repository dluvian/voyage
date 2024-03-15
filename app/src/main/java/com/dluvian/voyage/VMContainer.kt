package com.dluvian.voyage

import com.dluvian.voyage.core.viewModel.HomeViewModel
import com.dluvian.voyage.core.viewModel.SearchViewModel
import com.dluvian.voyage.core.viewModel.SettingsViewModel

data class VMContainer(
    val homeVM: HomeViewModel,
    val settingsVM: SettingsViewModel,
    val searchVM: SearchViewModel,
)
