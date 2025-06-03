package com.dluvian.voyage.viewModel

import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import com.dluvian.voyage.Topic
import com.dluvian.voyage.model.EditExistingList
import com.dluvian.voyage.model.EditListViewAddProfile
import com.dluvian.voyage.model.EditListViewAddTopic
import com.dluvian.voyage.model.EditListViewCmd
import com.dluvian.voyage.model.EditNewList
import com.dluvian.voyage.nostr.Subscriber
import rust.nostr.sdk.PublicKey
import java.util.UUID

class EditListViewModel(
    private val subscriber: Subscriber,
) : ViewModel() {
    private val _identifier = mutableStateOf("")

    val isLoading = mutableStateOf(false)
    val isSaving = mutableStateOf(false)
    val title = mutableStateOf("")
    val description = mutableStateOf(TextFieldValue())
    val profiles = mutableStateOf(emptyList<Pair<PublicKey, String>>())
    val topics = mutableStateOf(emptyList<Topic>())
    val tabIndex = mutableIntStateOf(0)

    fun handle(cmd: EditListViewCmd) {
        when (cmd) {
            is EditListViewAddProfile -> {
                if (profiles.value.any { it.first == cmd.profile.first }) return
                profiles.value += cmd.profile
            }

            is EditListViewAddTopic -> {
                TODO("Normalize")
                val normalized = cmd.topic
                topics.value += normalized
            }

            EditNewList -> {
                _identifier.value = UUID.randomUUID().toString()
                title.value = ""
                description.value = TextFieldValue()
                profiles.value = emptyList()
                topics.value = emptyList()
            }

            is EditExistingList -> {
                isLoading.value = true
                _identifier.value = cmd.ident

                TODO("Load lists from db, sub unknown profile names, magic")

                isLoading.value = false

            }
        }
    }
}
