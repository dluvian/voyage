package com.dluvian.voyage.ui.model

import com.dluvian.voyage.core.ComposableContent
import com.dluvian.voyage.core.Fn

data class Followable(
    val label: String,
    val isFollowed: Boolean,
    val icon: ComposableContent,
    val onFollow: Fn,
    val onUnfollow: Fn,
    val onOpen: Fn
)
