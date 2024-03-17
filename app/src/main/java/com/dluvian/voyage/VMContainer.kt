package com.dluvian.voyage

import com.dluvian.voyage.core.viewModel.HomeViewModel
import com.dluvian.voyage.core.viewModel.ProfileViewModel
import com.dluvian.voyage.core.viewModel.SearchViewModel
import com.dluvian.voyage.core.viewModel.SettingsViewModel
import com.dluvian.voyage.core.viewModel.ThreadViewModel
import com.dluvian.voyage.core.viewModel.TopicViewModel

data class VMContainer(
    val homeVM: HomeViewModel,
    val settingsVM: SettingsViewModel,
    val searchVM: SearchViewModel,
    val profileVM: ProfileViewModel,
    val threadVM: ThreadViewModel,
    val topicVM: TopicViewModel,
)
