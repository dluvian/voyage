package com.dluvian.voyage.ui.components.bottomSheet

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.dluvian.voyage.R
import com.dluvian.voyage.model.CloseEventDetails
import com.dluvian.voyage.model.Cmd
import com.dluvian.voyage.ui.components.text.ClickableRelayUrl
import com.dluvian.voyage.ui.components.text.ClickableTopic
import com.dluvian.voyage.ui.components.text.CopyableText
import com.dluvian.voyage.ui.components.text.SmallHeader
import com.dluvian.voyage.ui.theme.spacing
import rust.nostr.sdk.Event
import rust.nostr.sdk.Timestamp


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostDetailsBottomSheet(event: Event, onUpdate: (Cmd) -> Unit) {
    val onDismiss = { onUpdate(CloseEventDetails) }
    ModalBottomSheet(onDismissRequest = onDismiss) {
        BottomSheetColumn(header = stringResource(id = R.string.post_details)) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = spacing.xxl)
            ) {
                item {
                    SimpleTimeSection(
                        header = stringResource(id = R.string.time),
                        time = event.createdAt(),
                    )
                }

                if (postDetails.base.firstSeenIn.isNotEmpty()) item {
                    SmallHeader(header = stringResource(id = R.string.first_seen_in))
                    ClickableRelayUrl(
                        relayUrl = postDetails.base.firstSeenIn,
                        onUpdate = onUpdate,
                        onClickAddition = onDismiss
                    )
                    Spacer(modifier = Modifier.height(spacing.large))
                }

                if (!postDetails.client.isNullOrEmpty()) item {
                    SimpleSection(
                        header = stringResource(id = R.string.client),
                        content = postDetails.client
                    )
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
                    LazyRow {
                        item { CopyableText(text = postDetails.base.json) }
                    }
                }
            }
        }
    }
}

@Composable
private fun SimpleSection(header: String, content: String) {
    SmallHeader(header = header)
    Text(text = content)
    Spacer(modifier = Modifier.height(spacing.large))
}

@Composable
private fun SimpleTimeSection(header: String, time: Timestamp) {
    SimpleSection(
        header = header,
        content = getFullDateTime(ctx = LocalContext.current, createdAt = time.asSecs())
    )
}
