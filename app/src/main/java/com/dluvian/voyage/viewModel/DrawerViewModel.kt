package com.dluvian.voyage.viewModel

import androidx.compose.material3.DrawerState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dluvian.voyage.model.CloseDrawer
import com.dluvian.voyage.model.DrawerViewCmd
import com.dluvian.voyage.model.OpenDrawer
import com.dluvian.voyage.provider.NameProvider
import com.dluvian.voyage.provider.TrustProvider
import kotlinx.coroutines.launch
import rust.nostr.sdk.PublicKey

class DrawerViewModel(
    val drawerState: DrawerState,
    private val trustProvider: TrustProvider,
    private val nameProvider: NameProvider,
) :
    ViewModel() {
    val myPubkey = mutableStateOf<PublicKey?>(null)
    val myName = mutableStateOf<String>("")

    init {
        viewModelScope.launch {
            myPubkey.value = trustProvider.pubkey()
            myName.value = myPubkey.value?.toBech32().orEmpty()
        }
    }

    fun handle(cmd: DrawerViewCmd) {
        when (cmd) {
            is OpenDrawer -> cmd.scope.launch {
                drawerState.open()
                val pubkey = trustProvider.pubkey()
                myPubkey.value = pubkey
                myName.value = nameProvider.name(pubkey)
            }

            is CloseDrawer -> cmd.scope.launch {
                drawerState.close()
            }
        }
    }
}
