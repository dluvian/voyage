package com.dluvian.voyage.core.model

sealed class LabledGitIssue(open val header: String, open val body: String)

data class BugReport(
    override val header: String = "",
    override val body: String = ""
) : LabledGitIssue(header = header, body = body)

data class EnhancementRequest(
    override val header: String = "",
    override val body: String = ""
) : LabledGitIssue(header = header, body = body)
