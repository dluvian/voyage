package com.dluvian.voyage.viewModel

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dluvian.voyage.EditListViewAction
import com.dluvian.voyage.EditListViewAddProfile
import com.dluvian.voyage.EditListViewAddTopic
import com.dluvian.voyage.EditListViewSave
import com.dluvian.voyage.R
import com.dluvian.voyage.core.DELAY_1SEC
import com.dluvian.voyage.core.MAX_KEYS_SQL
import com.dluvian.voyage.core.Topic
import com.dluvian.voyage.core.VOYAGE
import com.dluvian.voyage.core.utils.isBareTopicStr
import com.dluvian.voyage.core.utils.launchIO
import com.dluvian.voyage.core.utils.normalizeTopic
import com.dluvian.voyage.core.utils.showToast
import com.dluvian.voyage.data.interactor.ItemSetEditor
import com.dluvian.voyage.data.nostr.LazyNostrSubscriber
import com.dluvian.voyage.data.provider.ItemSetProvider
import com.dluvian.voyage.data.room.view.AdvancedProfileView
import com.dluvian.voyage.filterSetting.CustomPubkeys
import kotlinx.coroutines.delay
import java.util.UUID

class EditListViewModel(
    private val itemSetEditor: ItemSetEditor,
    private val snackbar: SnackbarHostState,
    private val itemSetProvider: ItemSetProvider,
    private val lazyNostrSubscriber: LazyNostrSubscriber,
) : ViewModel() {
    private val _identifier = mutableStateOf("")

    val isLoading = mutableStateOf(false)
    val isSaving = mutableStateOf(false)
    val title = mutableStateOf("")
    val description = mutableStateOf(TextFieldValue())
    val profiles = mutableStateOf(emptyList<AdvancedProfileView>())
    val topics = mutableStateOf(emptyList<Topic>())
    val tabIndex = mutableIntStateOf(0)

    fun createNew() {
        // Drop first part of UUID just to make the identifier shorter
        _identifier.value = VOYAGE + UUID.randomUUID().toString().dropWhile { it != '-' }
        title.value = ""
        description.value = TextFieldValue()
        profiles.value = emptyList()
        topics.value = emptyList()
    }

    fun editExisting(identifier: String) {
        isLoading.value = true
        _identifier.value = identifier

        if (identifier == itemSetProvider.identifier.value) {
            title.value = itemSetProvider.title.value
            description.value = TextFieldValue(itemSetProvider.description.value.text)
            profiles.value = itemSetProvider.profiles.value
            topics.value = itemSetProvider.topics.value
        } else {
            title.value = ""
            description.value = TextFieldValue()
            profiles.value = emptyList()
            topics.value = emptyList()
            tabIndex.intValue = 0
        }

        viewModelScope.launchIO {
            itemSetProvider.loadList(identifier = identifier)

            val pubkeys = itemSetProvider.profiles.value.map { it.pubkey }
            lazyNostrSubscriber.lazySubUnknownProfiles(selection = CustomPubkeys(pubkeys = pubkeys))
        }.invokeOnCompletion {
            title.value = itemSetProvider.title.value
            description.value = TextFieldValue(itemSetProvider.description.value.text)
            profiles.value = itemSetProvider.profiles.value
            topics.value = itemSetProvider.topics.value
            isLoading.value = false
        }
    }

    fun handle(action: EditListViewAction) {
        when (action) {
            is EditListViewSave -> saveLists(action = action)

            is EditListViewAddProfile -> {
                if (profiles.value.any { it.pubkey == action.profile.pubkey }) return
                if (profiles.value.size >= MAX_KEYS_SQL) return
                profiles.value += action.profile
            }

            is EditListViewAddTopic -> {
                val normalized = action.topic.normalizeTopic()
                if (!normalized.isBareTopicStr()) return
                if (topics.value.any { it == normalized }) return
                if (topics.value.size >= MAX_KEYS_SQL) return
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
                description = description.value.text,
                pubkeys = profiles.value.map { it.pubkey })
            val topicSet = itemSetEditor.editTopicSet(
                identifier = _identifier.value,
                title = title.value,
                description = description.value.text,
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
