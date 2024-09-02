package com.dluvian.voyage.ui.components.bottomSheet

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.dluvian.voyage.R
import com.dluvian.voyage.core.ClosePostInfo
import com.dluvian.voyage.core.OnUpdate
import com.dluvian.voyage.data.model.PostDetails
import com.dluvian.voyage.ui.components.text.ClickableRelayUrl
import com.dluvian.voyage.ui.components.text.ClickableTopic
import com.dluvian.voyage.ui.components.text.CopyableText
import com.dluvian.voyage.ui.components.text.SmallHeader
import com.dluvian.voyage.ui.theme.spacing


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostDetailsBottomSheet(postDetails: PostDetails, onUpdate: OnUpdate) {
    val onDismiss = { onUpdate(ClosePostInfo) }
    ModalBottomSheet(onDismissRequest = onDismiss) {
        BottomSheetColumn(header = stringResource(id = R.string.post_details)) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = spacing.xxl)
            ) {
                if (postDetails.base.firstSeenIn.isNotEmpty()) item {
                    SmallHeader(header = stringResource(id = R.string.first_seen_in))
                    ClickableRelayUrl(
                        relayUrl = postDetails.base.firstSeenIn,
                        onUpdate = onUpdate,
                        onClickAddition = onDismiss
                    )
                    Spacer(modifier = Modifier.height(spacing.large))
                }

                if (postDetails.indexedTopics.isNotEmpty()) {
                    item { SmallHeader(header = stringResource(id = R.string.indexed_topics)) }
                    items(postDetails.indexedTopics) { topic ->
                        ClickableTopic(
                            topic = topic,
                            onUpdate = onUpdate,
                            onClickAddition = onDismiss
                        )
                    }
                    item { Spacer(modifier = Modifier.height(spacing.large)) }
                }

                item {
                    SmallHeader(header = stringResource(id = R.string.event_json))
                    CopyableText(text = postDetails.base.json)
                }
            }
        }
    }
}
