package com.dluvian.voyage.viewModel

data class VMContainer(
    val homeVM: HomeViewModel,
    val discoverVM: DiscoverViewModel,
    val settingsVM: SettingsViewModel,
    val searchVM: SearchViewModel,
    val profileVM: ProfileViewModel,
    val threadVM: ThreadViewModel,
    val topicVM: TopicViewModel,
    val postVM: PostViewModel,
    val replyVM: ReplyViewModel,
    val editProfileVM: EditProfileViewModel,
    val relayEditorVM: RelayEditorViewModel,
    val crossPostVM: CrossPostViewModel,
    val relayProfileVM: RelayProfileViewModel,
    val inboxVM: InboxViewModel,
    val drawerVM: DrawerViewModel,
    val followListsVM: FollowListsViewModel,
    val bookmarkVM: BookmarkViewModel,
    val gitIssueVM: GitIssueViewModel
)
