package com.dluvian.voyage.core.model

data class LeveledReplyUI(
    val level: Int,
    val reply: LegacyReply,
    val isCollapsed: Boolean,
    val hasLoadedReplies: Boolean,
)
