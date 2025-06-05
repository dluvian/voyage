package com.dluvian.voyage.ui.views.nonMain.profile

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import com.dluvian.voyage.core.OnUpdate
import com.dluvian.voyage.data.filterSetting.FullProfileUI
import com.dluvian.voyage.data.filterSetting.ItemSetMeta
import com.dluvian.voyage.ui.components.scaffold.VoyageScaffold

Composable () ->Unit
import com.dluvian.voyage.core.OnUpdate
import com.dluvian.voyage.data.filterSetting.FullProfileUI
import com.dluvian.voyage.data.filterSetting.ItemSetMeta
import com.dluvian.voyage.ui.components.scaffold.VoyageScaffold

@Composable
fun ProfileScaffold(
    profile: FullProfileUI,
    addableLists: List<ItemSetMeta>,
    nonAddableLists: List<ItemSetMeta>,
    snackbar: SnackbarHostState,
    onUpdate: OnUpdate,
    content: @Composable () -> Unit
) {
    VoyageScaffold(
        snackbar = snackbar,
        topBar = {
            ProfileTopAppBar(
                profile = profile,
                addableLists = addableLists,
                nonAddableLists = nonAddableLists,
                onUpdate = onUpdate
            )
        }
    ) {
        content()
    }
}
