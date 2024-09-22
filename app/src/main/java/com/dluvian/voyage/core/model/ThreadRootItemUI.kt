package com.dluvian.voyage.core.model

import com.dluvian.voyage.core.PubkeyHex

sealed class ThreadRootItemUI(
    open val pubkey: PubkeyHex,
    open val replyCount: Int
)

data class ThreadRootUI(
    val rootPostUI: RootPost
) : ThreadRootItemUI(
    pubkey = rootPostUI.pubkey,
    replyCount = rootPostUI.replyCount
)

data class ThreadPseudoRootUI(
    val legacyReply: LegacyReply
) : ThreadRootItemUI(
    pubkey = legacyReply.pubkey,
    replyCount = legacyReply.replyCount
)
