package com.dluvian.voyage.ui.views.nonMain.search

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.dluvian.voyage.R
import com.dluvian.voyage.core.OnUpdate
import com.dluvian.voyage.core.OpenProfile
import com.dluvian.voyage.core.OpenTopic
import com.dluvian.voyage.core.Topic
import com.dluvian.voyage.core.viewModel.SearchViewModel
import com.dluvian.voyage.data.room.entity.ProfileEntity
import com.dluvian.voyage.ui.components.ClickableRow
import com.dluvian.voyage.ui.theme.AccountIcon
import com.dluvian.voyage.ui.theme.HashtagIcon
import com.dluvian.voyage.ui.theme.spacing

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
    topics: List<Topic>, profiles: List<ProfileEntity>, onUpdate: OnUpdate
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
                    header = profile.name ?: profile.pubkey,
                    imageVector = AccountIcon,
                    onClick = { onUpdate(OpenProfile(nip19 = profile.toNip19())) })
            }
        }
    }
}

@Composable
private fun SectionHeader(header: String) {
    Text(
        modifier = Modifier
            .padding(top = spacing.bigScreenEdge)
            .padding(horizontal = spacing.bigScreenEdge, vertical = spacing.large),
        text = header,
        style = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.SemiBold
    )
}
