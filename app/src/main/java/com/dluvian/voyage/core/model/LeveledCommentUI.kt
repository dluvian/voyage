package com.dluvian.voyage.core.model

data class LeveledCommentUI(
    val level: Int,
    val comment: CommentUI,
    val isCollapsed: Boolean,
    val hasLoadedReplies: Boolean,
)
