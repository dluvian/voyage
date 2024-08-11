package com.dluvian.voyage.ui.components.selection

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.dluvian.voyage.R
import com.dluvian.voyage.core.Fn
import com.dluvian.voyage.data.model.FeedPubkeySelection
import com.dluvian.voyage.data.model.FriendPubkeys
import com.dluvian.voyage.data.model.Global
import com.dluvian.voyage.data.model.NoPubkeys
import com.dluvian.voyage.data.model.WebOfTrustPubkeys

@Composable
fun FeedPubkeySelectionRadio(
    current: FeedPubkeySelection,
    target: FeedPubkeySelection,
    onClick: Fn
) {
    NamedRadio(
        isSelected = current == target,
        name = when (target) {
            NoPubkeys -> stringResource(id = R.string.none)
            FriendPubkeys -> stringResource(id = R.string.my_friends)
            WebOfTrustPubkeys -> stringResource(id = R.string.web_of_trust)
            Global -> stringResource(id = R.string.global)
        },
        onClick = onClick
    )
}
