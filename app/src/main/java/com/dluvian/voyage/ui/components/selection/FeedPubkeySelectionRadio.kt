package com.dluvian.voyage.ui.components.selection

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.dluvian.voyage.R
import com.dluvian.voyage.filterSetting.FriendPubkeys
import com.dluvian.voyage.filterSetting.Global
import com.dluvian.voyage.filterSetting.NoPubkeys
import com.dluvian.voyage.filterSetting.PubkeySelection

@Composable
fun FeedPubkeySelectionRadio(
    current: PubkeySelection,
    target: PubkeySelection,
    onClick: () -> Unit
) {
    NamedRadio(
        isSelected = current == target,
        name = when (target) {
            NoPubkeys -> stringResource(id = R.string.none)
            FriendPubkeys -> stringResource(id = R.string.my_friends)
            Global -> stringResource(id = R.string.global)
        },
        onClick = onClick
    )
}
