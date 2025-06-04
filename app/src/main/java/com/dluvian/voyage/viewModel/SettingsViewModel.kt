package com.dluvian.voyage.viewModel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.dluvian.voyage.model.AddClientTag
import com.dluvian.voyage.model.ChangeUpvoteContent
import com.dluvian.voyage.model.LoadSeed
import com.dluvian.voyage.model.SendAuth
import com.dluvian.voyage.model.SettingsViewCmd
import com.dluvian.voyage.model.SettingsViewOpen
import com.dluvian.voyage.nostr.NostrService
import com.dluvian.voyage.preferences.EventPreferences
import com.dluvian.voyage.preferences.RelayPreferences
import com.dluvian.voyage.provider.NameProvider

class SettingsViewModel(
    private val service: NostrService,
    private val nameProvider: NameProvider,
    private val relayPreferences: RelayPreferences,
    private val eventPreferences: EventPreferences,
) : ViewModel() {
    val npub = mutableStateOf("")
    val seed = mutableStateOf(emptyList<String>()) // Rework this to support nsec and nsecBunker
    val sendAuth = mutableStateOf(relayPreferences.getSendAuth())
    val isDeleting = mutableStateOf(false)
    val currentUpvote = mutableStateOf(eventPreferences.getUpvoteContent())
    val isAddingClientTag = mutableStateOf(eventPreferences.isAddingClientTag())

    val isLoadingAccount = mutableStateOf(false)

    fun handle(cmd: SettingsViewCmd) {
        when (cmd) {
            SettingsViewOpen -> TODO()
            LoadSeed -> TODO()

            is SendAuth -> {
                relayPreferences.setSendAuth(sendAuth = cmd.sendAuth)
                this.sendAuth.value = cmd.sendAuth
            }

            is ChangeUpvoteContent -> {
                eventPreferences.setUpvoteContent(newUpvote = cmd.newContent)
                this.currentUpvote.value = cmd.newContent
            }

            is AddClientTag -> {
                eventPreferences.setIsAddingClientTag(addClientTag = cmd.addClientTag)
                this.isAddingClientTag.value = cmd.addClientTag
            }
        }
    }
}
