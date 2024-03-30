package com.dluvian.voyage.core.viewModel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.dluvian.voyage.core.CreateResponseViewAction
import com.dluvian.voyage.core.SendResponse
import com.dluvian.voyage.core.model.IParentUI

class CreateResponseViewModel : ViewModel() {
    val isSendingResponse = mutableStateOf(false)

    fun openParent(parent: IParentUI) {
        TODO()
    }

    fun handle(action: CreateResponseViewAction) {
        when (action) {
            is SendResponse -> TODO()
        }
    }
}
