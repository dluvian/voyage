package com.dluvian.voyage.model

sealed class GitIssueType
data object BugReport : GitIssueType()
data object EnhancementRequest : GitIssueType()
