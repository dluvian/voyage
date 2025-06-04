package com.dluvian.voyage.model

sealed class UICtx(open val event: UIEvent)
class FeedCtx(override val event: UIEvent) : UICtx(event = event)
class ThreadRootCtx(override val event: UIEvent) : UICtx(event = event)
class ThreadReplyCtx(
    override val event: UIEvent,
    val isOp: Boolean,
    val level: Int,
    val isCollapsed: Boolean,
    val hasReplies: Boolean
) : UICtx(event = event)
