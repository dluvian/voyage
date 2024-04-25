package com.dluvian.voyage.ui.views.nonMain.search

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.dluvian.nostr_kt.createNprofile
import com.dluvian.voyage.R
import com.dluvian.voyage.core.OnUpdate
import com.dluvian.voyage.core.OpenProfile
import com.dluvian.voyage.core.OpenTopic
import com.dluvian.voyage.core.Topic
import com.dluvian.voyage.core.model.TrustType
import com.dluvian.voyage.core.viewModel.SearchViewModel
import com.dluvian.voyage.data.room.view.AdvancedProfileView
import com.dluvian.voyage.ui.components.icon.AccountIconWithBadge
import com.dluvian.voyage.ui.components.row.ClickableRow
import com.dluvian.voyage.ui.components.text.SectionHeader
import com.dluvian.voyage.ui.theme.HashtagIcon

@Composable
fun SearchView(vm: SearchViewModel, snackbar: SnackbarHostState, onUpdate: OnUpdate) {
    val topics by vm.topics
    val profiles by vm.profiles

    LaunchedEffect(key1 = Unit) {
        vm.subProfiles()
    }

    SearchScaffold(snackbar = snackbar, onUpdate = onUpdate) {
        SearchViewContent(topics = topics, profiles = profiles, onUpdate = onUpdate)
    }
}

@Composable
private fun SearchViewContent(
    topics: List<Topic>,
    profiles: List<AdvancedProfileView>,
    onUpdate: OnUpdate
) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        if (topics.isNotEmpty()) {
            item {
                SectionHeader(header = stringResource(id = R.string.topics))
            }
            items(topics) { topic ->
                ClickableRow(header = topic,
                    imageVector = HashtagIcon,
                    onClick = { onUpdate(OpenTopic(topic = topic)) })
            }
        }

        if (profiles.isNotEmpty()) {
            item {
                SectionHeader(header = stringResource(id = R.string.profiles))
            }
            items(profiles) { profile ->
                ClickableRow(
                    header = profile.name,
                    icon = {
                        AccountIconWithBadge(
                            pubkey = profile.pubkey,
                            trustType = TrustType.from(
                                isOneself = profile.isMe,
                                isFriend = profile.isFriend,
                                isWebOfTrust = profile.isWebOfTrust
                            ),
                            isSmall = true,
                        )
                    },
                    onClick = {
                        onUpdate(OpenProfile(nprofile = createNprofile(hex = profile.pubkey)))
                    })
            }
        }
    }
}
