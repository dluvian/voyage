package com.dluvian.voyage.ui.views.nonMain.createResponse

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.dluvian.voyage.core.GoBack
import com.dluvian.voyage.core.OnUpdate
import com.dluvian.voyage.core.SendResponse
import com.dluvian.voyage.core.viewModel.CreateResponseViewModel
import com.dluvian.voyage.ui.components.indicator.ComingSoon
import com.dluvian.voyage.ui.views.nonMain.createPost.ContentCreationScaffold

@Composable
fun CreateResponseView(
    vm: CreateResponseViewModel,
    snackbar: SnackbarHostState,
    onUpdate: OnUpdate
) {
    val isSendingResponse by vm.isSendingResponse
    val response = remember { mutableStateOf("") }
    val context = LocalContext.current

    ContentCreationScaffold(
        showSendButton = response.value.isNotBlank(),
        isSendingContent = isSendingResponse,
        snackbar = snackbar,
        onSend = {
            onUpdate(
                SendResponse(
                    body = response.value,
                    context = context,
                    onGoBack = { onUpdate(GoBack) })
            )
        },
        onUpdate = onUpdate
    ) {
        ComingSoon()
    }
}
