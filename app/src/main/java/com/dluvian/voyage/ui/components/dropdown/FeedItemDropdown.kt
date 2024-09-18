package com.dluvian.voyage.ui.components.dropdown


import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.DropdownMenu
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.dluvian.voyage.R
import com.dluvian.voyage.core.BookmarkPost
import com.dluvian.voyage.core.DeletePost
import com.dluvian.voyage.core.Fn
import com.dluvian.voyage.core.FollowProfile
import com.dluvian.voyage.core.OnUpdate
import com.dluvian.voyage.core.OpenPostInfo
import com.dluvian.voyage.core.OpenThreadRaw
import com.dluvian.voyage.core.RebroadcastPost
import com.dluvian.voyage.core.UnfollowProfile
import com.dluvian.voyage.core.model.CrossPostUI
import com.dluvian.voyage.core.model.FeedItemUI
import com.dluvian.voyage.core.model.FriendTrust
import com.dluvian.voyage.core.model.IsInListTrust
import com.dluvian.voyage.core.model.LegacyReplyUI
import com.dluvian.voyage.core.model.Locked
import com.dluvian.voyage.core.model.LockedOneself
import com.dluvian.voyage.core.model.Muted
import com.dluvian.voyage.core.model.NoTrust
import com.dluvian.voyage.core.model.Oneself
import com.dluvian.voyage.core.model.RootPostUI
import com.dluvian.voyage.core.model.WebTrust
import com.dluvian.voyage.core.utils.copyAndToast
import com.dluvian.voyage.core.utils.createProcessTextIntent
import com.dluvian.voyage.core.utils.getTranslators
import com.dluvian.voyage.data.nostr.createNevent
import com.dluvian.voyage.data.nostr.createNeventUri

@Composable
fun FeedItemDropdown(
    isOpen: Boolean,
    feedItem: FeedItemUI,
    onDismiss: () -> Unit,
    onUpdate: OnUpdate
) {
    DropdownMenu(
        expanded = isOpen,
        onDismissRequest = onDismiss
    ) {
        FollowItem(
            feedItem = feedItem,
            onDismiss = onDismiss,
            onUpdate = onUpdate
        )
        FollowCrossPostedItem(
            feedItem = feedItem,
            onDismiss = onDismiss,
            onUpdate = onUpdate
        )

        when (feedItem) {
            is RootPostUI -> {}
            is LegacyReplyUI -> SimpleDropdownItem(
                text = stringResource(id = R.string.open_as_root),
                onClick = {
                    onUpdate(OpenThreadRaw(nevent = createNevent(hex = feedItem.id)))
                    onDismiss()
                })

            is CrossPostUI -> {}
        }

        val clip = LocalClipboardManager.current
        val context = LocalContext.current
        val idCopiedToast = stringResource(id = R.string.note_uri_copied)
        SimpleDropdownItem(
            text = stringResource(id = R.string.copy_uri),
            onClick = {
                copyAndToast(
                    text = createNeventUri(
                        hex = feedItem.id,
                        author = feedItem.pubkey,
                        relays = listOf(feedItem.relayUrl).filter { it.isNotEmpty() },
                        kind = feedItem.getKind()
                    ),
                    toast = idCopiedToast,
                    context = context,
                    clip = clip
                )
                onDismiss()
            }
        )
        val contentCopiedToast = stringResource(id = R.string.content_copied)
        SimpleDropdownItem(
            text = stringResource(id = R.string.copy_content),
            onClick = {
                copyAndToast(
                    text = feedItem.content,
                    toast = contentCopiedToast,
                    context = context,
                    clip = clip
                )
                onDismiss()
            }
        )
        if (!feedItem.isBookmarked) {
            SimpleDropdownItem(
                text = stringResource(id = R.string.bookmark),
                onClick = {
                    onUpdate(BookmarkPost(postId = feedItem.id)) // TODO: Does bookmarking crossposts work?
                    onDismiss()
                }
            )
        }
        SimpleDropdownItem(
            text = stringResource(id = R.string.rebroadcast),
            onClick = {
                // RelevantId bc repost json is not saved in db
                onUpdate(RebroadcastPost(postId = feedItem.getRelevantId(), context = context))
                onDismiss()
            }
        )
        if (feedItem.trustType is Oneself) {
            SimpleDropdownItem(
                text = stringResource(id = R.string.attempt_deletion),
                onClick = {
                    onUpdate(DeletePost(id = feedItem.id)) // TODO: Does deleting cross post work?
                    onDismiss()
                }
            )
        }

        if (feedItem.trustType !is Oneself) {
            val launcher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.StartActivityForResult()
            ) { _ -> }
            val packageManager = LocalContext.current.packageManager
            for (translator in getTranslators(packageManager = packageManager)) {
                SimpleDropdownItem(
                    text = translator.loadLabel(packageManager).toString(),
                    onClick = {
                        launcher.launch(
                            createProcessTextIntent(text = feedItem.content.text, info = translator)
                        )
                        onDismiss()
                    }
                )
            }
        }
        SimpleDropdownItem(
            text = stringResource(id = R.string.more),
            onClick = {
                onUpdate(OpenPostInfo(postId = feedItem.getRelevantId()))
                onDismiss()
            }
        )
    }
}

@Composable
private fun FollowItem(
    feedItem: FeedItemUI,
    onDismiss: Fn,
    onUpdate: OnUpdate
) {
    when (feedItem.trustType) {
        Oneself, Muted, Locked, LockedOneself -> {}
        FriendTrust -> {
            SimpleDropdownItem(
                text = stringResource(id = R.string.unfollow),
                onClick = {
                    onUpdate(UnfollowProfile(pubkey = feedItem.pubkey))
                    onDismiss()
                }
            )
        }

        NoTrust, WebTrust, IsInListTrust -> {
            SimpleDropdownItem(
                text = stringResource(id = R.string.follow),
                onClick = {
                    onUpdate(FollowProfile(pubkey = feedItem.pubkey))
                    onDismiss()
                }
            )
        }
    }
}

@Composable
private fun FollowCrossPostedItem(
    feedItem: FeedItemUI,
    onDismiss: Fn,
    onUpdate: OnUpdate
) {
    if (feedItem is CrossPostUI) {
        when (feedItem.crossPostedTrustType) {
            Oneself, Muted, Locked, LockedOneself -> {}
            FriendTrust -> {
                SimpleDropdownItem(
                    text = stringResource(id = R.string.unfollow_cross_posted_author),
                    onClick = {
                        onUpdate(UnfollowProfile(pubkey = feedItem.crossPostedPubkey))
                        onDismiss()
                    }
                )
            }

            NoTrust, WebTrust, IsInListTrust -> {
                SimpleDropdownItem(
                    text = stringResource(id = R.string.follow_cross_posted_author),
                    onClick = {
                        onUpdate(FollowProfile(pubkey = feedItem.crossPostedPubkey))
                        onDismiss()
                    }
                )
            }
        }
    }
}
