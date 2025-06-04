package com.dluvian.voyage.viewModel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.dluvian.voyage.model.BugReport
import com.dluvian.voyage.model.GitIssueCmd
import com.dluvian.voyage.model.GitIssueOpen

class GitIssueViewModel() : ViewModel() {
    val issueType = mutableStateOf(BugReport)
    val subject = mutableStateOf("")
    val content = mutableStateOf("")

    fun handle(cmd: GitIssueCmd) {
        when (cmd) {
            GitIssueOpen -> {
                issueType.value = BugReport
                subject.value = ""
                content.value = ""
            }
        }
    }
}
