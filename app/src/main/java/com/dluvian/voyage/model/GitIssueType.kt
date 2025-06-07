package com.dluvian.voyage.model

sealed class GitIssueType {
    fun label(): String {
        return when (this) {
            BugReport -> "bug"
            EnhancementRequest -> "enhancement"
        }
    }
}
data object BugReport : GitIssueType()
data object EnhancementRequest : GitIssueType()
