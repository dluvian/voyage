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
import com.dluvian.voyage.core.model.FriendTrust
import com.dluvian.voyage.core.model.IsInListTrust
import com.dluvian.voyage.core.model.Locked
import com.dluvian.voyage.core.model.LockedOneself
import com.dluvian.voyage.core.model.Muted
import com.dluvian.voyage.core.model.NoTrust
import com.dluvian.voyage.core.model.Oneself
import com.dluvian.voyage.core.model.ParentUI
import com.dluvian.voyage.core.model.ReplyUI
import com.dluvian.voyage.core.model.RootPostUI
import com.dluvian.voyage.core.model.WebTrust
import com.dluvian.voyage.core.utils.copyAndToast
import com.dluvian.voyage.core.utils.createProcessTextIntent
import com.dluvian.voyage.core.utils.getTranslators
import com.dluvian.voyage.data.nostr.createNevent
import com.dluvian.voyage.data.nostr.createNeventUri

@Composable
fun ParentRowDropdown(
    isOpen: Boolean,
    parent: ParentUI,
    onDismiss: () -> Unit,
    onUpdate: OnUpdate
) {
    DropdownMenu(
        expanded = isOpen,
        onDismissRequest = onDismiss
    ) {
        FollowItem(
            parent = parent,
            onDismiss = onDismiss,
            onUpdate = onUpdate
        )
        FollowCrossPostedItem(
            parent = parent,
            onDismiss = onDismiss,
            onUpdate = onUpdate
        )

        when (parent) {
            is ReplyUI -> SimpleDropdownItem(text = stringResource(id = R.string.open_as_root),
                onClick = {
                    onUpdate(OpenThreadRaw(nevent = createNevent(hex = parent.id)))
                    onDismiss()
                })

            is RootPostUI -> {}
        }

        val clip = LocalClipboardManager.current
        val context = LocalContext.current
        val idCopiedToast = stringResource(id = R.string.note_uri_copied)
        SimpleDropdownItem(
            text = stringResource(id = R.string.copy_uri),
            onClick = {
                copyAndToast(
                    text = createNeventUri(
                        hex = parent.id,
                        author = parent.pubkey,
                        relays = listOf(parent.relayUrl).filter { it.isNotEmpty() },
                        kind = parent.getKind()
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
                    text = parent.content,
                    toast = contentCopiedToast,
                    context = context,
                    clip = clip
                )
                onDismiss()
            }
        )
        if (!parent.isBookmarked) {
            SimpleDropdownItem(
                text = stringResource(id = R.string.bookmark),
                onClick = {
                    onUpdate(BookmarkPost(postId = parent.getRelevantId()))
                    onDismiss()
                }
            )
        }
        SimpleDropdownItem(
            text = stringResource(id = R.string.rebroadcast),
            onClick = {
                // RelevantId bc repost json is not saved in db
                onUpdate(RebroadcastPost(postId = parent.getRelevantId(), context = context))
                onDismiss()
            }
        )
        if (parent.trustType is Oneself) {
            SimpleDropdownItem(
                text = stringResource(id = R.string.attempt_deletion),
                onClick = {
                    onUpdate(DeletePost(id = parent.id))
                    onDismiss()
                }
            )
        }

        if (parent.trustType !is Oneself) {
            val launcher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.StartActivityForResult()
            ) { _ -> }
            val packageManager = LocalContext.current.packageManager
            for (translator in getTranslators(packageManager = packageManager)) {
                SimpleDropdownItem(
                    text = translator.loadLabel(packageManager).toString(),
                    onClick = {
                        launcher.launch(
                            createProcessTextIntent(text = parent.content.text, info = translator)
                        )
                        onDismiss()
                    }
                )
            }
        }
        SimpleDropdownItem(
            text = stringResource(id = R.string.more),
            onClick = {
                onUpdate(OpenPostInfo(postId = parent.getRelevantId()))
                onDismiss()
            }
        )
    }
}

@Composable
private fun FollowItem(
    parent: ParentUI,
    onDismiss: Fn,
    onUpdate: OnUpdate
) {
    when (parent.trustType) {
        Oneself, Muted, Locked, LockedOneself -> {}
        FriendTrust -> {
            SimpleDropdownItem(
                text = stringResource(id = R.string.unfollow),
                onClick = {
                    onUpdate(UnfollowProfile(pubkey = parent.pubkey))
                    onDismiss()
                }
            )
        }

        NoTrust, WebTrust, IsInListTrust -> {
            SimpleDropdownItem(
                text = stringResource(id = R.string.follow),
                onClick = {
                    onUpdate(FollowProfile(pubkey = parent.pubkey))
                    onDismiss()
                }
            )
        }
    }
}

@Composable
private fun FollowCrossPostedItem(
    parent: ParentUI,
    onDismiss: Fn,
    onUpdate: OnUpdate
) {
    if (parent is RootPostUI && parent.crossPostedTrustType != null && parent.crossPostedPubkey != null) {
        when (parent.crossPostedTrustType) {
            Oneself, Muted, Locked, LockedOneself -> {}
            FriendTrust -> {
                SimpleDropdownItem(
                    text = stringResource(id = R.string.unfollow_cross_posted_author),
                    onClick = {
                        onUpdate(UnfollowProfile(pubkey = parent.crossPostedPubkey))
                        onDismiss()
                    }
                )
            }

            NoTrust, WebTrust, IsInListTrust -> {
                SimpleDropdownItem(
                    text = stringResource(id = R.string.follow_cross_posted_author),
                    onClick = {
                        onUpdate(FollowProfile(pubkey = parent.crossPostedPubkey))
                        onDismiss()
                    }
                )
            }
        }
    }
}
