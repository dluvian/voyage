package com.dluvian.voyage.core.model

import androidx.compose.runtime.Immutable

@Immutable
data class ThreadUI(
    val rootPost: RootPostUI,
    val comments: List<CommentUI>
)
