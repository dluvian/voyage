package com.dluvian.voyage.ui.components.row.mainEvent

import com.dluvian.voyage.core.PubkeyHex
import com.dluvian.voyage.core.model.MainEvent
import com.dluvian.voyage.core.model.ThreadableMainEvent

sealed class MainEventCtx(open val mainEvent: MainEvent)

data class ThreadRootCtx(
    val threadableMainEvent: ThreadableMainEvent
) : MainEventCtx(mainEvent = threadableMainEvent)

data class ThreadReplyCtx(
    val threadableMainEvent: ThreadableMainEvent,
    val opPubkey: PubkeyHex,
) : MainEventCtx(mainEvent = threadableMainEvent)

data class FeedCtx(override val mainEvent: MainEvent) : MainEventCtx(mainEvent = mainEvent)
