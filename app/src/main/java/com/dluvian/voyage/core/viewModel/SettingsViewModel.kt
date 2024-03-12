package com.dluvian.voyage.core.viewModel

import androidx.lifecycle.ViewModel
import com.dluvian.voyage.core.model.AccountType
import com.dluvian.voyage.core.model.DefaultAccount
import rust.nostr.protocol.Keys

class SettingsViewModel : ViewModel() {
    val accountType: AccountType = DefaultAccount(Keys.generate().publicKey())
}