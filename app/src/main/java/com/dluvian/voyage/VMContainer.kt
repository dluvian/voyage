package com.dluvian.voyage

import com.dluvian.voyage.core.viewModel.CreateCrossPostViewModel
import com.dluvian.voyage.core.viewModel.CreatePostViewModel
import com.dluvian.voyage.core.viewModel.CreateReplyViewModel
import com.dluvian.voyage.core.viewModel.DiscoverViewModel
import com.dluvian.voyage.core.viewModel.EditProfileViewModel
import com.dluvian.voyage.core.viewModel.HomeViewModel
import com.dluvian.voyage.core.viewModel.ProfileViewModel
import com.dluvian.voyage.core.viewModel.RelayEditorViewModel
import com.dluvian.voyage.core.viewModel.RelayProfileViewModel
import com.dluvian.voyage.core.viewModel.SearchViewModel
import com.dluvian.voyage.core.viewModel.SettingsViewModel
import com.dluvian.voyage.core.viewModel.ThreadViewModel
import com.dluvian.voyage.core.viewModel.TopicViewModel

data class VMContainer(
    val homeVM: HomeViewModel,
    val discoverVM: DiscoverViewModel,
    val settingsVM: SettingsViewModel,
    val searchVM: SearchViewModel,
    val profileVM: ProfileViewModel,
    val threadVM: ThreadViewModel,
    val topicVM: TopicViewModel,
    val createPostVM: CreatePostViewModel,
    val createReplyVM: CreateReplyViewModel,
    val editProfileVM: EditProfileViewModel,
    val relayEditorVM: RelayEditorViewModel,
    val createCrossPostVM: CreateCrossPostViewModel,
    val relayProfileVM: RelayProfileViewModel,
)
