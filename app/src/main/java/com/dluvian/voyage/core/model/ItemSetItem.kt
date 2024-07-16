package com.dluvian.voyage.core.model

import com.dluvian.voyage.core.PubkeyHex
import com.dluvian.voyage.core.Topic

sealed class ItemSetItem(val value: String)
data class ItemSetProfile(val pubkey: PubkeyHex) : ItemSetItem(value = pubkey)
data class ItemSetTopic(val topic: Topic) : ItemSetItem(value = topic)
