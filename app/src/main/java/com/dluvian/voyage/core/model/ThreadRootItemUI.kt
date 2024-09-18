package com.dluvian.voyage.core.model

import com.dluvian.voyage.core.PubkeyHex

sealed class ThreadRootItemUI(
    open val pubkey: PubkeyHex,
    open val replyCount: Int
)

data class ThreadRootUI(
    val rootPostUI: RootPostUI
) : ThreadRootItemUI(
    pubkey = rootPostUI.pubkey,
    replyCount = rootPostUI.replyCount
)

data class ThreadPseudoRootUI(
    val legacyReplyUI: LegacyReplyUI
) : ThreadRootItemUI(
    pubkey = legacyReplyUI.pubkey,
    replyCount = legacyReplyUI.replyCount
)
