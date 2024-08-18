package com.dluvian.voyage.ui.components.bottomSheet

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.dluvian.voyage.R
import com.dluvian.voyage.core.ClosePostInfo
import com.dluvian.voyage.core.OnUpdate
import com.dluvian.voyage.data.model.PostDetails
import com.dluvian.voyage.ui.components.indicator.FullLinearProgressIndicator
import com.dluvian.voyage.ui.components.text.ClickableRelayUrl
import com.dluvian.voyage.ui.components.text.SmallHeader


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostDetailsBottomSheet(postDetails: PostDetails, onUpdate: OnUpdate) {
    val onDismiss = { onUpdate(ClosePostInfo) }
    ModalBottomSheet(onDismissRequest = onDismiss) {
        BottomSheetColumn(header = stringResource(id = R.string.post_details)) {
            if (postDetails.firstSeenIn.isEmpty() || postDetails.json.isEmpty()) {
                FullLinearProgressIndicator()
            } else {
                SmallHeader(header = stringResource(id = R.string.first_seen_in))
                ClickableRelayUrl(
                    relayUrl = postDetails.firstSeenIn,
                    onUpdate = onUpdate,
                    onClickAddition = onDismiss
                )
            }
        }
    }
}
