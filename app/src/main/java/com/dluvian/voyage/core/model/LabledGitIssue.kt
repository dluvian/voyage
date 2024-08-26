package com.dluvian.voyage.core.model

sealed class LabledGitIssue(open val header: String, open val body: String) {
    fun getLabel(): String {
        return when (this) {
            is BugReport -> "bug"
            is EnhancementRequest -> "enhancement"
        }
    }
}

data class BugReport(
    override val header: String = "",
    override val body: String = ""
) : LabledGitIssue(header = header, body = body)

data class EnhancementRequest(
    override val header: String = "",
    override val body: String = ""
) : LabledGitIssue(header = header, body = body)
