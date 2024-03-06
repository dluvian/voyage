package com.dluvian.voyage.data.keys

import com.dluvian.voyage.core.PubkeyHex

interface IPubkeyProvider {
    fun getPubkeyHex(): PubkeyHex
}
