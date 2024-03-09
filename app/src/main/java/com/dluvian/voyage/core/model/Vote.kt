package com.dluvian.voyage.core.model

import androidx.compose.runtime.Stable

sealed class Vote {
    @Stable
    fun isNeutral(): Boolean {
        return this is NoVote
    }

    companion object {
        fun from(vote: Boolean?): Vote {
            return when (vote) {
                null -> NoVote
                true -> Upvote
                false -> Downvote
            }
        }
    }
}
data object Upvote : Vote()
data object Downvote : Vote()
data object NoVote : Vote()
