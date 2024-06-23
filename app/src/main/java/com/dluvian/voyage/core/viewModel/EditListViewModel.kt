package com.dluvian.voyage.core.viewModel

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dluvian.voyage.R
import com.dluvian.voyage.core.DELAY_1SEC
import com.dluvian.voyage.core.EditListViewAction
import com.dluvian.voyage.core.EditListViewAddProfile
import com.dluvian.voyage.core.EditListViewAddTopic
import com.dluvian.voyage.core.EditListViewSave
import com.dluvian.voyage.core.Topic
import com.dluvian.voyage.core.isBareTopicStr
import com.dluvian.voyage.core.launchIO
import com.dluvian.voyage.core.normalizeTopic
import com.dluvian.voyage.core.showToast
import com.dluvian.voyage.data.interactor.ItemSetEditor
import com.dluvian.voyage.data.provider.ItemSetProvider
import com.dluvian.voyage.data.room.view.AdvancedProfileView
import kotlinx.coroutines.delay
import java.util.UUID

class EditListViewModel(
    private val itemSetEditor: ItemSetEditor,
    private val snackbar: SnackbarHostState,
    private val itemSetProvider: ItemSetProvider,
) : ViewModel() {
    private val _identifier = mutableStateOf("")

    val isLoading = mutableStateOf(false)
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

    fun editExisting(identifier: String) {
        isLoading.value = true
        _identifier.value = identifier

        title.value = identifier
        profiles.value = emptyList()
        topics.value = emptyList()
        tabIndex.intValue = 0

        viewModelScope.launchIO {
            title.value = itemSetProvider.getTitle(identifier = identifier)
            profiles.value = itemSetProvider.getProfilesFromList(identifier = identifier)
            topics.value = itemSetProvider.getTopicsFromList(identifier = identifier)
        }.invokeOnCompletion {
            isLoading.value = false
        }
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

        viewModelScope.launchIO {
            val profileSet = itemSetEditor.editProfileSet(
                identifier = _identifier.value,
                title = title.value,
                pubkeys = profiles.value.map { it.pubkey })
            val topicSet = itemSetEditor.editTopicSet(
                identifier = _identifier.value,
                title = title.value,
                topics = topics.value
            )

            delay(DELAY_1SEC)
            action.onGoBack()

            val msgId = when {
                profileSet.isSuccess && topicSet.isSuccess -> R.string.custom_list_updated
                profileSet.isFailure -> R.string.failed_to_sign_profile_list
                topicSet.isFailure -> R.string.failed_to_sign_topic_list
                else -> null
            }
            if (msgId != null) {
                snackbar.showToast(viewModelScope, action.context.getString(msgId))
            }
        }.invokeOnCompletion {
            isSaving.value = false
        }
    }
}
