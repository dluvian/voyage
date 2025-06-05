package com.dluvian.voyage.ui.views.nonMain.search

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.dluvian.voyage.R
import com.dluvian.voyage.Topic
import com.dluvian.voyage.model.Cmd
import com.dluvian.voyage.model.OpenNProfile
import com.dluvian.voyage.model.OpenTopic
import com.dluvian.voyage.model.TrustProfile
import com.dluvian.voyage.ui.components.row.ClickableProfileRow
import com.dluvian.voyage.ui.components.row.ClickableRow
import com.dluvian.voyage.ui.components.text.SectionHeader
import com.dluvian.voyage.ui.theme.HashtagIcon
import com.dluvian.voyage.ui.theme.spacing
import com.dluvian.voyage.viewModel.SearchViewModel
import rust.nostr.sdk.Nip19Profile

@Composable
fun SearchView(vm: SearchViewModel, onUpdate: (Cmd) -> Unit) {
    val topics by vm.topics
    val profiles by vm.profiles

    SearchViewContent(
        topics = topics,
        profiles = profiles,
        onUpdate = onUpdate
    )
}

@Composable
private fun SearchViewContent(
    topics: List<Topic>,
    profiles: List<TrustProfile>,
    onUpdate: (Cmd) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = spacing.xxl)
    ) {
        if (topics.isNotEmpty()) {
            item {
                SectionHeader(header = stringResource(id = R.string.topics))
            }
            items(topics) { topic ->
                ClickableRow(header = topic,
                    leadingIcon = HashtagIcon,
                    onClick = { onUpdate(OpenTopic(topic = topic)) })
            }
        }

        if (profiles.isNotEmpty()) {
            item {
                SectionHeader(header = stringResource(id = R.string.profiles))
            }
            items(profiles) { profile ->
                ClickableProfileRow(
                    profile = profile,
                    onClick = {
                        onUpdate(OpenNProfile(Nip19Profile(profile.pubkey)))
                    })
            }
        }
    }
}
