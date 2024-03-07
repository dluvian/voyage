package com.dluvian.voyage.core.model

sealed class Vote
data object Upvote : Vote()
data object Downvote : Vote()
data object NoVote : Vote()
