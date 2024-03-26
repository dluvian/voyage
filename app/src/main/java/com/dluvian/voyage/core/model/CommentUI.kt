package com.dluvian.voyage.core.model

import com.dluvian.voyage.core.EventIdHex

data class CommentUI(
    val id: EventIdHex,
    val parentId: EventIdHex,
    val comments: List<CommentUI>
)
