package com.dluvian.voyage.core.viewModel

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.pager.PagerState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.dluvian.voyage.core.EditListViewAction
import com.dluvian.voyage.core.EditListViewSave
import com.dluvian.voyage.core.Topic
import com.dluvian.voyage.data.room.view.AdvancedProfileView
import java.util.UUID

class EditListViewModel @OptIn(ExperimentalFoundationApi::class) constructor(
    val pagerState: PagerState
) : ViewModel() {
    private val identifier = mutableStateOf("")
    val title = mutableStateOf("")
    val profiles = mutableStateOf(emptyList<AdvancedProfileView>())
    val topics = mutableStateOf(emptyList<Topic>())
    val tabIndex = mutableIntStateOf(0)

    fun createNew() {
        identifier.value = UUID.randomUUID().toString()
        title.value = ""
        profiles.value = emptyList()
        topics.value = emptyList()
    }


    fun handle(action: EditListViewAction) {
        when (action) {
            is EditListViewSave -> {}
        }
    }
}