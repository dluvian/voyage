package com.dluvian.voyage.viewModel

data class VMContainer(
    val homeVM: HomeViewModel,
    val discoverVM: DiscoverViewModel,
    val settingsVM: SettingsViewModel,
    val searchVM: SearchViewModel,
    val profileVM: ProfileViewModel,
    val threadVM: ThreadViewModel,
    val topicVM: TopicViewModel,
    val createPostVM: PostViewModel,
    val createReplyVM: ReplyViewModel,
    val editProfileVM: EditProfileViewModel,
    val relayEditorVM: RelayEditorViewModel,
    val createCrossPostVM: CrossPostViewModel,
    val relayProfileVM: RelayProfileViewModel,
    val inboxVM: InboxViewModel,
    val drawerVM: DrawerViewModel,
    val followListsVM: FollowListsViewModel,
    val bookmarkVM: BookmarkViewModel,
    val editListVM: EditListViewModel,
    val listVM: ListViewModel,
    val createGitIssueVM: GitIssueViewModel,
)
