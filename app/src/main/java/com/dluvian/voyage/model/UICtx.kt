package com.dluvian.voyage.model

sealed class UICtx(open val uiEvent: UIEvent)
class FeedCtx(override val uiEvent: UIEvent) : UICtx(uiEvent = uiEvent)
class ThreadRootCtx(override val uiEvent: UIEvent, val replyCount: UInt) : UICtx(uiEvent = uiEvent)
class ThreadReplyCtx(
    override val uiEvent: UIEvent,
    val isOp: Boolean,
    val level: Int,
    val isCollapsed: Boolean,
    val hasReplies: Boolean
) : UICtx(uiEvent = uiEvent)
