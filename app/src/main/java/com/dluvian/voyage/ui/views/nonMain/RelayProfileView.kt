package com.dluvian.voyage.ui.views.nonMain

import androidx.compose.foundation.clickable
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.dluvian.voyage.R
import com.dluvian.voyage.core.OnUpdate
import com.dluvian.voyage.core.copyAndToast
import com.dluvian.voyage.core.viewModel.RelayProfileViewModel
import com.dluvian.voyage.ui.components.indicator.BaseHint
import com.dluvian.voyage.ui.components.indicator.FullLinearProgressIndicator
import com.dluvian.voyage.ui.components.scaffold.SimpleGoBackScaffold
import com.dluvian.voyage.ui.theme.spacing
import rust.nostr.protocol.RelayInformationDocument

@Composable
fun RelayProfileView(vm: RelayProfileViewModel, snackbar: SnackbarHostState, onUpdate: OnUpdate) {
    val header by vm.header
    val profile by vm.profile
    val isLoading by vm.isLoading
    val nrelay by vm.nrelayUri
    val postsInDb by vm.postsInDb.value.collectAsState()

    SimpleGoBackScaffold(
        header = header,
        snackbar = snackbar,
        onUpdate = onUpdate
    ) {
        RelayProfileViewContent(
            url = header,
            nrelay = nrelay,
            profile = profile,
            postsInDb = postsInDb,
            isLoading = isLoading
        )
    }
}

@Composable
private fun RelayProfileViewContent(
    url: String,
    nrelay: String,
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
        item {
            InfoRow(infoType = stringResource(id = R.string.url), value = url)
        }
        item {
            InfoRow(infoType = stringResource(id = R.string.nostr_uri), value = nrelay)
        }
        item {
            InfoRow(
                infoType = stringResource(id = R.string.posts_in_db),
                value = postsInDb.toString()
            )
        }
        profile?.name()?.let {
            item {
                InfoRow(infoType = stringResource(id = R.string.name), value = it)
            }
        }
        profile?.description()?.let {
            item {
                InfoRow(infoType = stringResource(id = R.string.description), value = it)
            }
        }
        profile?.pubkey()?.let {
            item {
                InfoRow(infoType = stringResource(id = R.string.pubkey), value = it)
            }
        }
        profile?.contact()?.let {
            item {
                InfoRow(infoType = stringResource(id = R.string.contact), value = it)
            }
        }
        profile?.supportedNips()?.toString()?.let {
            item {
                InfoRow(infoType = stringResource(id = R.string.nips), value = it)
            }
        }
        profile?.software()?.let {
            item {
                InfoRow(infoType = stringResource(id = R.string.software), value = it)
            }
        }
        profile?.version()?.let {
            item {
                InfoRow(infoType = stringResource(id = R.string.version), value = it)
            }
        }
        profile?.limitation()?.maxMessageLength?.toString()?.let {
            item {
                InfoRow(infoType = stringResource(id = R.string.max_message_length), value = it)
            }
        }
        profile?.limitation()?.maxSubscriptions?.toString()?.let {
            item {
                InfoRow(infoType = stringResource(id = R.string.max_subscriptions), value = it)
            }
        }
        profile?.limitation()?.maxFilters?.toString()?.let {
            item {
                InfoRow(infoType = stringResource(id = R.string.max_filters), value = it)
            }
        }
        profile?.limitation()?.maxLimit?.toString()?.let {
            item {
                InfoRow(infoType = stringResource(id = R.string.max_limit), value = it)
            }
        }
        profile?.limitation()?.maxSubidLength?.toString()?.let {
            item {
                InfoRow(infoType = stringResource(id = R.string.max_sub_id_length), value = it)
            }
        }
        profile?.limitation()?.maxEventTags?.toString()?.let {
            item {
                InfoRow(infoType = stringResource(id = R.string.max_tags), value = it)
            }
        }
        profile?.limitation()?.maxContentLength?.toString()?.let {
            item {
                InfoRow(infoType = stringResource(id = R.string.max_content_length), value = it)
            }
        }
        profile?.limitation()?.minPowDifficulty?.toString()?.let {
            item {
                InfoRow(infoType = stringResource(id = R.string.min_difficulty), value = it)
            }
        }
        profile?.limitation()?.authRequired?.toString()?.let {
            item {
                InfoRow(infoType = stringResource(id = R.string.auth_required), value = it)
            }
        }
        profile?.limitation()?.paymentRequired?.toString()?.let {
            item {
                InfoRow(infoType = stringResource(id = R.string.payment_required), value = it)
            }
        }
        profile?.paymentsUrl()?.let {
            item {
                InfoRow(infoType = stringResource(id = R.string.paymentUrl), value = it)
            }
        }
        profile?.limitation()?.createdAtLowerLimit?.toHumanDatetime()?.let {
            item {
                InfoRow(infoType = stringResource(id = R.string.min_timestamp), value = it)
            }
        }
        profile?.limitation()?.createdAtUpperLimit?.toHumanDatetime()?.let {
            item {
                InfoRow(infoType = stringResource(id = R.string.max_timestamp), value = it)
            }
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
        profile?.postingPolicy()?.let {
            item {
                InfoRow(infoType = stringResource(id = R.string.policy), value = it)
            }
        }
        profile?.icon()?.let {
            item {
                InfoRow(infoType = stringResource(id = R.string.icon), value = it)
            }
        }
    }
}

@Composable
private fun InfoRow(infoType: String, value: String) {
    val context = LocalContext.current
    val clip = LocalClipboardManager.current
    val toast = stringResource(id = R.string.value_copied)
    Row(
        modifier = Modifier.padding(vertical = spacing.large),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(weight = 0.3f, fill = true)) {
            Text(text = infoType, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.width(spacing.medium))
        Column(modifier = Modifier.weight(weight = 0.7f, fill = true)) {
            Text(
                modifier = Modifier.clickable {
                    copyAndToast(text = value, toast = toast, context = context, clip = clip)
                },
                text = value
            )
        }
    }
}
