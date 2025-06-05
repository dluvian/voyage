package com.dluvian.voyage.ui.views.nonMain

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.dluvian.voyage.R
import com.dluvian.voyage.model.Cmd
import com.dluvian.voyage.ui.components.indicator.BaseHint
import com.dluvian.voyage.ui.components.indicator.FullLinearProgressIndicator
import com.dluvian.voyage.ui.components.scaffold.SimpleGoBackScaffold
import com.dluvian.voyage.ui.components.text.CopyableText
import com.dluvian.voyage.ui.theme.spacing
import com.dluvian.voyage.viewModel.RelayProfileViewModel
import rust.nostr.sdk.RelayInformationDocument

@Composable
fun RelayProfileView(
    vm: RelayProfileViewModel,
    snackbar: SnackbarHostState,
    onUpdate: (Cmd) -> Unit
) {
    val header by vm.header
    val profile by vm.profile
    val isLoading by vm.isLoading
    val postsInDb by vm.postsInDb.value.collectAsState()

    SimpleGoBackScaffold(
        header = header,
        snackbar = snackbar,
        onUpdate = onUpdate
    ) {
        RelayProfileViewContent(
            url = header,
            profile = profile,
            postsInDb = postsInDb,
            isLoading = isLoading
        )
    }
}

@Composable
private fun RelayProfileViewContent(
    url: String,
    profile: RelayInformationDocument?,
    postsInDb: Int,
    isLoading: Boolean
) {
    if (isLoading) FullLinearProgressIndicator()
    else if (profile == null) BaseHint(stringResource(id = R.string.relay_profile_not_found))

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = spacing.screenEdge,
            end = spacing.screenEdge,
            bottom = spacing.bigScreenEdge
        )
    ) {
        item { InfoRow(infoType = stringResource(id = R.string.url), value = url) }
        item {
            InfoRow(
                infoType = stringResource(id = R.string.posts_in_db),
                value = postsInDb.toString()
            )
        }
        item { InfoRow(infoType = stringResource(id = R.string.name), value = profile?.name()) }
        item {
            InfoRow(
                infoType = stringResource(id = R.string.description),
                value = profile?.description()
            )
        }
        item { InfoRow(infoType = stringResource(id = R.string.pubkey), value = profile?.pubkey()) }
        item {
            InfoRow(
                infoType = stringResource(id = R.string.contact),
                value = profile?.contact()
            )
        }
        profile?.supportedNips()?.let {
            if (it.isNotEmpty()) item {
                InfoRow(infoType = stringResource(id = R.string.nips), value = it.toString())
            }
        }
        item {
            InfoRow(
                infoType = stringResource(id = R.string.software),
                value = profile?.software()
            )
        }
        item {
            InfoRow(
                infoType = stringResource(id = R.string.version),
                value = profile?.version()
            )
        }
        item {
            InfoRow(
                infoType = stringResource(id = R.string.max_message_length),
                value = profile?.limitation()?.maxMessageLength?.toString()
            )
        }
        item {
            InfoRow(
                infoType = stringResource(id = R.string.max_subscriptions),
                value = profile?.limitation()?.maxSubscriptions?.toString()
            )
        }
        item {
            InfoRow(
                infoType = stringResource(id = R.string.max_filters),
                value = profile?.limitation()?.maxFilters?.toString()
            )
        }
        item {
            InfoRow(
                infoType = stringResource(id = R.string.max_limit),
                value = profile?.limitation()?.maxLimit?.toString()
            )
        }
        item {
            InfoRow(
                infoType = stringResource(id = R.string.max_sub_id_length),
                value = profile?.limitation()?.maxSubidLength?.toString()
            )
        }
        item {
            InfoRow(
                infoType = stringResource(id = R.string.max_tags),
                value = profile?.limitation()?.maxEventTags?.toString()
            )
        }
        item {
            InfoRow(
                infoType = stringResource(id = R.string.max_content_length),
                value = profile?.limitation()?.maxContentLength?.toString()
            )
        }
        item {
            InfoRow(
                infoType = stringResource(id = R.string.min_difficulty),
                value = profile?.limitation()?.minPowDifficulty?.toString()
            )
        }
        item {
            InfoRow(
                infoType = stringResource(id = R.string.auth_required),
                value = profile?.limitation()?.authRequired?.toString()
            )
        }
        item {
            InfoRow(
                infoType = stringResource(id = R.string.payment_required),
                value = profile?.limitation()?.paymentRequired?.toString()
            )
        }
        item {
            InfoRow(
                infoType = stringResource(id = R.string.paymentUrl),
                value = profile?.paymentsUrl()
            )
        }
        item {
            InfoRow(
                infoType = stringResource(id = R.string.min_timestamp),
                value = profile?.limitation()?.createdAtLowerLimit?.toHumanDatetime()
            )
        }
        item {
            InfoRow(
                infoType = stringResource(id = R.string.max_timestamp),
                value = profile?.limitation()?.createdAtUpperLimit?.toHumanDatetime()
            )
        }
        profile?.relayCountries()?.let {
            if (it.isNotEmpty()) item {
                InfoRow(infoType = stringResource(id = R.string.countries), value = it.toString())
            }
        }
        profile?.languageTags()?.let {
            if (it.isNotEmpty()) item {
                InfoRow(infoType = stringResource(id = R.string.languages), value = it.toString())
            }
        }
        item {
            InfoRow(
                infoType = stringResource(id = R.string.policy),
                value = profile?.postingPolicy()
            )
        }
        item { InfoRow(infoType = stringResource(id = R.string.icon), value = profile?.icon()) }
    }
}

@Composable
private fun InfoRow(infoType: String, value: String?) {
    if (!value.isNullOrBlank()) {
        Row(
            modifier = Modifier.padding(vertical = spacing.large),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(weight = 0.3f, fill = true)) {
                Text(text = infoType, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.width(spacing.medium))
            Column(modifier = Modifier.weight(weight = 0.7f, fill = true)) {
                CopyableText(text = value)
            }
        }
    }
}
