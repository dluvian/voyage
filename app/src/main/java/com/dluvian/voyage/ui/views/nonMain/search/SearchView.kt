package com.dluvian.voyage.ui.views.nonMain.search

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.dluvian.nostr_kt.createNevent
import com.dluvian.nostr_kt.createNprofile
import com.dluvian.voyage.R
import com.dluvian.voyage.core.OnUpdate
import com.dluvian.voyage.core.OpenProfile
import com.dluvian.voyage.core.OpenThreadRaw
import com.dluvian.voyage.core.OpenTopic
import com.dluvian.voyage.core.SubUnknownProfiles
import com.dluvian.voyage.core.Topic
import com.dluvian.voyage.core.model.TrustType
import com.dluvian.voyage.core.viewModel.SearchViewModel
import com.dluvian.voyage.data.room.view.AdvancedProfileView
import com.dluvian.voyage.data.room.view.SimplePostView
import com.dluvian.voyage.ui.components.row.ClickableProfileRow
import com.dluvian.voyage.ui.components.row.ClickableRow
import com.dluvian.voyage.ui.components.row.ClickableTrustIconRow
import com.dluvian.voyage.ui.components.text.SectionHeader
import com.dluvian.voyage.ui.theme.HashtagIcon
import com.dluvian.voyage.ui.theme.spacing

@Composable
fun SearchView(vm: SearchViewModel, snackbar: SnackbarHostState, onUpdate: OnUpdate) {
    val topics by vm.topics
    val profiles by vm.profiles
    val posts by vm.posts

    LaunchedEffect(key1 = Unit) {
        onUpdate(SubUnknownProfiles)
    }

    SearchScaffold(snackbar = snackbar, onUpdate = onUpdate) {
        SearchViewContent(
            topics = topics,
            profiles = profiles,
            posts = posts,
            onUpdate = onUpdate
        )
    }
}

@Composable
private fun SearchViewContent(
    topics: List<Topic>,
    profiles: List<AdvancedProfileView>,
    posts: List<SimplePostView>,
    onUpdate: OnUpdate
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
                        onUpdate(OpenProfile(nprofile = createNprofile(hex = profile.pubkey)))
                    })
            }
        }

        if (posts.isNotEmpty()) {
            item {
                SectionHeader(header = stringResource(id = R.string.posts))
            }
            items(posts) { post ->
                ClickableTrustIconRow(
                    trustType = TrustType.from(
                        isOneself = post.authorIsOneself,
                        isFriend = post.authorIsFriend,
                        isWebOfTrust = post.authorIsTrusted,
                    ),
                    header = post.subject,
                    content = post.content,
                    onClick = {
                        onUpdate(OpenThreadRaw(nevent = createNevent(hex = post.id)))
                    },
                    onTrustIconClick = {
                        onUpdate(OpenProfile(nprofile = createNprofile(hex = post.pubkey)))
                    }
                )
            }
        }
    }
}
