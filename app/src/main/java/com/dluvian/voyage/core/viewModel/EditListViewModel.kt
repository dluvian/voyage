package com.dluvian.voyage.core.viewModel

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.pager.PagerState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.dluvian.voyage.core.EditListViewAction
import com.dluvian.voyage.core.EditListViewAddProfile
import com.dluvian.voyage.core.EditListViewAddTopic
import com.dluvian.voyage.core.EditListViewSave
import com.dluvian.voyage.core.Topic
import com.dluvian.voyage.core.isBareTopicStr
import com.dluvian.voyage.core.normalizeTopic
import com.dluvian.voyage.data.interactor.ItemSetEditor
import com.dluvian.voyage.data.room.view.AdvancedProfileView
import java.util.UUID

class EditListViewModel @OptIn(ExperimentalFoundationApi::class) constructor(
    val pagerState: PagerState,
    private val itemSetEditor: ItemSetEditor,
) : ViewModel() {
    private val _identifier = mutableStateOf("")

    val isSaving = mutableStateOf(false)
    val title = mutableStateOf("")
    val profiles = mutableStateOf(emptyList<AdvancedProfileView>())

    val topics = mutableStateOf(emptyList<Topic>())
    val tabIndex = mutableIntStateOf(0)

    fun createNew() {
        _identifier.value = UUID.randomUUID().toString()

        title.value = ""
        profiles.value = emptyList()
        topics.value = emptyList()
    }


    fun handle(action: EditListViewAction) {
        when (action) {
            is EditListViewSave -> saveLists(action = action)
            is EditListViewAddProfile -> {
                if (profiles.value.any { it.pubkey == action.profile.pubkey }) return
                profiles.value += action.profile
            }

            is EditListViewAddTopic -> {
                val normalized = action.topic.normalizeTopic()
                if (!normalized.isBareTopicStr()) return
                if (topics.value.any { it == normalized }) return
                topics.value += normalized
            }
        }
    }

    private fun saveLists(action: EditListViewSave) {
        if (isSaving.value) return
        isSaving.value = true
        val profileIsSuccess = itemSetEditor.editProfileSet()
        if (profileIsSuccess) {
            itemSetEditor.editTopicSet()
        }

        isSaving.value = false
    }
}