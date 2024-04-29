package com.dluvian.voyage.ui.components.dropdown

import androidx.compose.material3.DropdownMenu
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.dluvian.nostr_kt.createNeventStr
import com.dluvian.voyage.R
import com.dluvian.voyage.core.DeletePost
import com.dluvian.voyage.core.Fn
import com.dluvian.voyage.core.FollowProfile
import com.dluvian.voyage.core.OnUpdate
import com.dluvian.voyage.core.SignerLauncher
import com.dluvian.voyage.core.UnfollowProfile
import com.dluvian.voyage.core.copyAndToast
import com.dluvian.voyage.core.getSignerLauncher
import com.dluvian.voyage.core.model.FriendTrust
import com.dluvian.voyage.core.model.IParentUI
import com.dluvian.voyage.core.model.NoTrust
import com.dluvian.voyage.core.model.Oneself
import com.dluvian.voyage.core.model.RootPostUI
import com.dluvian.voyage.core.model.WebTrust

@Composable
fun ParentRowDropdown(
    isOpen: Boolean,
    parent: IParentUI,
    onDismiss: () -> Unit,
    onUpdate: OnUpdate
) {
    val signerLauncher = getSignerLauncher(onUpdate = onUpdate)
    DropdownMenu(
        expanded = isOpen,
        onDismissRequest = onDismiss
    ) {
        FollowItem(
            parent = parent,
            signerLauncher = signerLauncher,
            onDismiss = onDismiss,
            onUpdate = onUpdate
        )
        FollowCrossPostedItem(
            parent = parent,
            signerLauncher = signerLauncher,
            onDismiss = onDismiss,
            onUpdate = onUpdate
        )
        val clip = LocalClipboardManager.current
        val context = LocalContext.current
        val idCopiedToast = stringResource(id = R.string.note_id_copied)
        SimpleDropdownItem(
            text = stringResource(id = R.string.copy_id),
            onClick = {
                copyAndToast(
                    text = createNeventStr(
                        hex = parent.id,
                        author = parent.pubkey,
                        relays = listOf(parent.relayUrl).filter { it.isNotEmpty() }),
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
        if (parent.trustType is Oneself) {
            SimpleDropdownItem(
                text = stringResource(id = R.string.attempt_deletion),
                onClick = {
                    onUpdate(DeletePost(id = parent.id, signerLauncher = signerLauncher))
                    onDismiss()
                }
            )
        }
    }
}

@Composable
private fun FollowItem(
    parent: IParentUI,
    signerLauncher: SignerLauncher,
    onDismiss: Fn,
    onUpdate: OnUpdate
) {
    when (parent.trustType) {
        Oneself -> {}
        FriendTrust -> {
            SimpleDropdownItem(
                text = stringResource(id = R.string.unfollow),
                onClick = {
                    onUpdate(
                        UnfollowProfile(pubkey = parent.pubkey, signerLauncher = signerLauncher)
                    )
                    onDismiss()
                }
            )
        }

        NoTrust, WebTrust -> {
            SimpleDropdownItem(
                text = stringResource(id = R.string.follow),
                onClick = {
                    onUpdate(
                        FollowProfile(pubkey = parent.pubkey, signerLauncher = signerLauncher)
                    )
                    onDismiss()
                }
            )
        }
    }
}

@Composable
private fun FollowCrossPostedItem(
    parent: IParentUI,
    signerLauncher: SignerLauncher,
    onDismiss: Fn,
    onUpdate: OnUpdate
) {
    if (parent is RootPostUI && parent.crossPostedTrustType != null && parent.crossPostedPubkey != null) {
        when (parent.crossPostedTrustType) {
            Oneself -> {}
            FriendTrust -> {
                SimpleDropdownItem(
                    text = stringResource(id = R.string.unfollow_cross_posted_author),
                    onClick = {
                        onUpdate(
                            UnfollowProfile(
                                pubkey = parent.crossPostedPubkey,
                                signerLauncher = signerLauncher
                            )
                        )
                        onDismiss()
                    }
                )
            }

            NoTrust, WebTrust -> {
                SimpleDropdownItem(
                    text = stringResource(id = R.string.follow_cross_posted_author),
                    onClick = {
                        onUpdate(
                            FollowProfile(
                                pubkey = parent.crossPostedPubkey,
                                signerLauncher = signerLauncher
                            )
                        )
                        onDismiss()
                    }
                )
            }
        }
    }
}
