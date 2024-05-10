package com.dluvian.voyage.ui.views.nonMain.profile

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.zIndex
import com.dluvian.voyage.R
import com.dluvian.voyage.core.ClickText
import com.dluvian.voyage.core.OnUpdate
import com.dluvian.voyage.core.ProfileViewAppend
import com.dluvian.voyage.core.ProfileViewRefresh
import com.dluvian.voyage.core.viewModel.ProfileViewModel
import com.dluvian.voyage.ui.components.Feed
import com.dluvian.voyage.ui.components.FullHorizontalDivider
import com.dluvian.voyage.ui.components.indicator.ComingSoon
import com.dluvian.voyage.ui.components.text.AnnotatedText
import com.dluvian.voyage.ui.theme.spacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileView(vm: ProfileViewModel, snackbar: SnackbarHostState, onUpdate: OnUpdate) {
    val profile by vm.profile.value.collectAsState()

    ProfileScaffold(profile = profile, snackbar = snackbar, onUpdate = onUpdate) {
        val index = remember { mutableIntStateOf(0) }
        Column {
            // Set higher zIndex to hide resting refresh indicator
            PrimaryTabRow(modifier = Modifier.zIndex(2f), selectedTabIndex = index.intValue) {
                Tab(
                    selected = index.intValue == 0,
                    onClick = { index.intValue = 0 },
                    text = { Text("Posts") })
                Tab(
                    selected = index.intValue == 1,
                    onClick = { index.intValue = 1 },
                    text = { Text("Replies") })
                Tab(
                    selected = index.intValue == 2,
                    onClick = { index.intValue = 2 },
                    text = { Text("About") })
                Tab(
                    selected = index.intValue == 3,
                    onClick = { index.intValue = 3 },
                    text = { Text("Relays") })
            }
            when (index.intValue) {
                0 -> Feed(
                    paginator = vm.paginator,
                    state = vm.feedState,
                    onRefresh = { onUpdate(ProfileViewRefresh) },
                    onAppend = { onUpdate(ProfileViewAppend) },
                    onUpdate = onUpdate,
                )

                2 -> About(
                    about = profile.about ?: AnnotatedString(text = ""),
                    onUpdate = onUpdate
                )

                else -> ComingSoon()

            }
        }
    }
}

@Composable
private fun About(about: AnnotatedString, onUpdate: OnUpdate) {
    val uriHandler = LocalUriHandler.current
    Column {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = spacing.bigScreenEdge, vertical = spacing.screenEdge),
        ) {

            Text(
                text = stringResource(id = R.string.about),
                style = MaterialTheme.typography.titleMedium
            )
            AnnotatedText(
                text = about,
                onClick = { offset ->
                    onUpdate(ClickText(text = about, offset = offset, uriHandler = uriHandler))
                }
            )
        }
        FullHorizontalDivider()
    }
}
