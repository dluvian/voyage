package com.dluvian.voyage.core.model

import androidx.compose.runtime.Stable

sealed class Vote {
    @Stable
    fun isNeutral(): Boolean {
        return this is NoVote
    }
}
data object Upvote : Vote()
data object Downvote : Vote()
data object NoVote : Vote()
