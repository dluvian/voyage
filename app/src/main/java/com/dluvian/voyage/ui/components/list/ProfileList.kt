package com.dluvian.voyage.ui.components.list

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.dluvian.voyage.model.TrustProfile
import com.dluvian.voyage.ui.components.icon.TrustIcon


@Composable
fun ProfileList(
    profiles: List<TrustProfile>,
    state: LazyListState,
    isRemovable: Boolean = false,
    firstRow: @Composable () -> Unit = {},
    onRemove: (Int) -> Unit = {},
    onClick: (Int) -> Unit = {},
) {
    val mappedProfiles = remember(profiles) {
        profiles.map { profile ->
            ItemProps(
                first = { TrustIcon(profile = profile) },
                second = profile.uiName(),
            )
        }
    }
    ItemList(
        items = mappedProfiles,
        state = state,
        isRemovable = isRemovable,
        firstRow = firstRow,
        onRemove = onRemove,
        onClick = onClick
    )
}