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
import com.dluvian.voyage.core.model.Comment
import com.dluvian.voyage.core.model.CrossPost
import com.dluvian.voyage.core.model.FriendTrust
import com.dluvian.voyage.core.model.IsInListTrust
import com.dluvian.voyage.core.model.LegacyReply
import com.dluvian.voyage.core.model.Locked
import com.dluvian.voyage.core.model.LockedOneself
import com.dluvian.voyage.core.model.MainEvent
import com.dluvian.voyage.core.model.Muted
import com.dluvian.voyage.core.model.NoTrust
import com.dluvian.voyage.core.model.Oneself
import com.dluvian.voyage.core.model.RootPost
import com.dluvian.voyage.core.model.WebTrust
import com.dluvian.voyage.core.utils.copyAndToast
import com.dluvian.voyage.core.utils.createProcessTextIntent
import com.dluvian.voyage.core.utils.getTranslators
import com.dluvian.voyage.data.nostr.createNevent
import com.dluvian.voyage.data.nostr.createNeventUri

@Composable
fun FeedItemDropdown(
    isOpen: Boolean,
    mainEvent: MainEvent,
    onDismiss: () -> Unit,
    onUpdate: OnUpdate
) {
    DropdownMenu(
        expanded = isOpen,
        onDismissRequest = onDismiss
    ) {
        FollowItem(
            mainEvent = mainEvent,
            onDismiss = onDismiss,
            onUpdate = onUpdate
        )
        FollowCrossPostedItem(
            mainEvent = mainEvent,
            onDismiss = onDismiss,
            onUpdate = onUpdate
        )

        when (mainEvent) {
            is RootPost -> {}
            is LegacyReply, is Comment -> SimpleDropdownItem(
                text = stringResource(id = R.string.open_as_root),
                onClick = {
                    onUpdate(OpenThreadRaw(nevent = createNevent(hex = mainEvent.id)))
                    onDismiss()
                })

            is CrossPost -> {}
        }

        val clip = LocalClipboardManager.current
        val context = LocalContext.current
        val idCopiedToast = stringResource(id = R.string.note_uri_copied)
        SimpleDropdownItem(
            text = stringResource(id = R.string.copy_uri),
            onClick = {
                copyAndToast(
                    text = createNeventUri(
                        hex = mainEvent.id,
                        author = mainEvent.pubkey,
                        relays = listOf(mainEvent.relayUrl).filter { it.isNotEmpty() },
                        kind = mainEvent.getKind()
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
                    text = mainEvent.content,
                    toast = contentCopiedToast,
                    context = context,
                    clip = clip
                )
                onDismiss()
            }
        )
        if (!mainEvent.isBookmarked) {
            SimpleDropdownItem(
                text = stringResource(id = R.string.bookmark),
                onClick = {
                    onUpdate(BookmarkPost(postId = mainEvent.getRelevantId()))
                    onDismiss()
                }
            )
        }
        SimpleDropdownItem(
            text = stringResource(id = R.string.rebroadcast),
            onClick = {
                // RelevantId bc repost json is not saved in db
                onUpdate(RebroadcastPost(postId = mainEvent.getRelevantId(), context = context))
                onDismiss()
            }
        )
        if (mainEvent.trustType is Oneself) {
            SimpleDropdownItem(
                text = stringResource(id = R.string.attempt_deletion),
                onClick = {
                    onUpdate(DeletePost(id = mainEvent.id))
                    onDismiss()
                }
            )
        }

        if (mainEvent.trustType !is Oneself) {
            val launcher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.StartActivityForResult()
            ) { _ -> }
            val packageManager = LocalContext.current.packageManager
            for (translator in getTranslators(packageManager = packageManager)) {
                SimpleDropdownItem(
                    text = translator.loadLabel(packageManager).toString(),
                    onClick = {
                        launcher.launch(
                            createProcessTextIntent(
                                text = mainEvent.content.text,
                                info = translator
                            )
                        )
                        onDismiss()
                    }
                )
            }
        }
        SimpleDropdownItem(
            text = stringResource(id = R.string.more),
            onClick = {
                onUpdate(OpenPostInfo(postId = mainEvent.getRelevantId()))
                onDismiss()
            }
        )
    }
}

@Composable
private fun FollowItem(
    mainEvent: MainEvent,
    onDismiss: Fn,
    onUpdate: OnUpdate
) {
    when (mainEvent.trustType) {
        Oneself, Muted, Locked, LockedOneself -> {}
        FriendTrust -> {
            SimpleDropdownItem(
                text = stringResource(id = R.string.unfollow),
                onClick = {
                    onUpdate(UnfollowProfile(pubkey = mainEvent.pubkey))
                    onDismiss()
                }
            )
        }

        NoTrust, WebTrust, IsInListTrust -> {
            SimpleDropdownItem(
                text = stringResource(id = R.string.follow),
                onClick = {
                    onUpdate(FollowProfile(pubkey = mainEvent.pubkey))
                    onDismiss()
                }
            )
        }
    }
}

@Composable
private fun FollowCrossPostedItem(
    mainEvent: MainEvent,
    onDismiss: Fn,
    onUpdate: OnUpdate
) {
    if (mainEvent is CrossPost) {
        when (mainEvent.crossPostedTrustType) {
            Oneself, Muted, Locked, LockedOneself -> {}
            FriendTrust -> {
                SimpleDropdownItem(
                    text = stringResource(id = R.string.unfollow_cross_posted_author),
                    onClick = {
                        onUpdate(UnfollowProfile(pubkey = mainEvent.crossPostedPubkey))
                        onDismiss()
                    }
                )
            }

            NoTrust, WebTrust, IsInListTrust -> {
                SimpleDropdownItem(
                    text = stringResource(id = R.string.follow_cross_posted_author),
                    onClick = {
                        onUpdate(FollowProfile(pubkey = mainEvent.crossPostedPubkey))
                        onDismiss()
                    }
                )
            }
        }
    }
}
