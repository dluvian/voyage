package com.dluvian.voyage.data.model.feed

sealed class FeedPubkeySelection

data object NoPubkeys : FeedPubkeySelection()
data object Friends : FeedPubkeySelection()
data object WebOfTrust : FeedPubkeySelection()
data object Global : FeedPubkeySelection()
