package com.dluvian.voyage.core.model

data class LeveledReplyUI(
    val level: Int,
    val reply: ReplyUI,
    val isCollapsed: Boolean,
    val hasLoadedReplies: Boolean,
)
