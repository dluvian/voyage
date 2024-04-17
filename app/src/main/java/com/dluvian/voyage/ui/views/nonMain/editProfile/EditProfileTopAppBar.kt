package com.dluvian.voyage.ui.views.nonMain.editProfile

import androidx.compose.runtime.Composable
import com.dluvian.voyage.core.Fn
import com.dluvian.voyage.core.OnUpdate
import com.dluvian.voyage.ui.components.bar.GoBackTopAppBar
import com.dluvian.voyage.ui.components.iconButton.SaveIconButton
import com.dluvian.voyage.ui.components.indicator.TopBarCircleProgressIndicator

@Composable
fun EditProfileTopAppBar(
    showSaveButton: Boolean,
    isSaving: Boolean,
    onSave: Fn,
    onUpdate: OnUpdate
) {
    GoBackTopAppBar(
        actions = {
            if (showSaveButton && !isSaving) {
                SaveIconButton(onSave = onSave)
            }
            if (isSaving) TopBarCircleProgressIndicator()
        },
        onUpdate = onUpdate
    )
}
