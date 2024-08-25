package com.dluvian.voyage.core.model

sealed class GitIssue(open val header: String, open val body: String)

data class BugReport(
    override val header: String = "",
    override val body: String = ""
) : GitIssue(header = header, body = body)

data class EnhancementRequest(
    override val header: String = "",
    override val body: String = ""
) : GitIssue(header = header, body = body)

data class GeneralFeedback(
    override val header: String = "",
    override val body: String = ""
) : GitIssue(header = header, body = body)
