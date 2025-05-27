package com.dluvian.voyage.viewModel

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
    val inboxVM: InboxViewModel,
    val drawerVM: DrawerViewModel,
    val followListsVM: FollowListsViewModel,
    val bookmarksVM: BookmarksViewModel,
    val editListVM: EditListViewModel,
    val listVM: ListViewModel,
    val createGitIssueVM: CreateGitIssueViewModel,
)
