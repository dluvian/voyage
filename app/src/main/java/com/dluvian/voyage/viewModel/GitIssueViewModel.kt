package com.dluvian.voyage.viewModel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.dluvian.voyage.model.BugReport

class GitIssueViewModel() : ViewModel() {
    val issueType = mutableStateOf(BugReport)
    val subject = mutableStateOf(false)
    val content = mutableStateOf("")
}
