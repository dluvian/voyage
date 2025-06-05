package com.dluvian.voyage.model

sealed class UICtx(open val uiEvent: UIEvent)
data class FeedCtx(override val uiEvent: UIEvent) : UICtx(uiEvent = uiEvent)
data class ThreadRootCtx(override val uiEvent: UIEvent, val replyCount: UInt) :
    UICtx(uiEvent = uiEvent)

data class ThreadReplyCtx(
    override val uiEvent: UIEvent,
    val isOp: Boolean,
    val level: Int,
    val isCollapsed: Boolean,
    val hasReplies: Boolean,
) : UICtx(uiEvent = uiEvent)
