package com.dluvian.voyage.core

sealed class UIEvent


data object SystemBackPress : UIEvent()
data object GoBack : UIEvent()
data object ClickHome : UIEvent()
data object ClickTopics : UIEvent()
data object ClickInbox : UIEvent()
data object ClickCreate : UIEvent()
data object ClickSettings : UIEvent()
data class ClickUpvote(val postId: EventIdHex, val pubkey: PubkeyHex) : UIEvent()
data class ClickDownvote(val postId: EventIdHex, val pubkey: PubkeyHex) : UIEvent()
data class ClickNeutralizeVote(val postId: EventIdHex) : UIEvent()

data object RefreshHomeView : UIEvent()
