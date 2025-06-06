package com.dluvian.voyage.ui.components.dropdown


import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.DropdownMenu
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.dluvian.voyage.R
import com.dluvian.voyage.copyAndToast
import com.dluvian.voyage.createProcessTextIntent
import com.dluvian.voyage.getTranslators
import com.dluvian.voyage.isReply
import com.dluvian.voyage.model.BookmarkPost
import com.dluvian.voyage.model.Cmd
import com.dluvian.voyage.model.DeletePost
import com.dluvian.voyage.model.FollowProfile
import com.dluvian.voyage.model.FollowedProfile
import com.dluvian.voyage.model.OneselfProfile
import com.dluvian.voyage.model.OpenThread
import com.dluvian.voyage.model.Rebroadcast
import com.dluvian.voyage.model.ShowEventDetails
import com.dluvian.voyage.model.TrustedProfile
import com.dluvian.voyage.model.UIEvent
import com.dluvian.voyage.model.UnfollowProfile
import com.dluvian.voyage.model.UnknownProfile
import rust.nostr.sdk.Nip19Event

@Composable
fun FeedItemDropdown(
    isOpen: Boolean,
    uiEvent: UIEvent,
    onDismiss: () -> Unit,
    onUpdate: (Cmd) -> Unit
) {
    DropdownMenu(
        expanded = isOpen,
        onDismissRequest = onDismiss
    ) {
        FollowItem(
            uiEvent = uiEvent,
            onDismiss = onDismiss,
            onUpdate = onUpdate
        )
        FollowCrossPostedItem(
            uiEvent = uiEvent,
            onDismiss = onDismiss,
            onUpdate = onUpdate
        )

        if (uiEvent.event.isReply()) SimpleDropdownItem(
            text = stringResource(id = R.string.open_as_root),
            onClick = {
                onUpdate(OpenThread(uiEvent.event))
                onDismiss()
            })

        val clip = LocalClipboard.current
        val context = LocalContext.current
        val idCopiedToast = stringResource(id = R.string.note_uri_copied)
        SimpleDropdownItem(
            text = stringResource(id = R.string.copy_uri),
            onClick = {
                copyAndToast(
                    text = Nip19Event.fromEvent(uiEvent.event).toNostrUri(),
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
                    text = uiEvent.event.content(),
                    toast = contentCopiedToast,
                    context = context,
                    clip = clip
                )
                onDismiss()
            }
        )
        if (!uiEvent.bookmarked) {
            SimpleDropdownItem(
                text = stringResource(id = R.string.bookmark),
                onClick = {
                    onUpdate(BookmarkPost(uiEvent.event.id()))
                    onDismiss()
                }
            )
        }
        SimpleDropdownItem(
            text = stringResource(id = R.string.rebroadcast),
            onClick = {
                onUpdate(Rebroadcast(uiEvent.event))
                onDismiss()
            }
        )
        if (uiEvent.authorProfile is OneselfProfile) {
            SimpleDropdownItem(
                text = stringResource(id = R.string.attempt_deletion),
                onClick = {
                    onUpdate(DeletePost(uiEvent.event))
                    onDismiss()
                }
            )
        }

        if (uiEvent.authorProfile !is OneselfProfile) {
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
                                text = uiEvent.annotatedContent.text,
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
                onUpdate(ShowEventDetails(uiEvent.event))
                onDismiss()
            }
        )
    }
}

@Composable
private fun FollowItem(
    uiEvent: UIEvent,
    onDismiss: () -> Unit,
    onUpdate: (Cmd) -> Unit
) {
    when (uiEvent.authorProfile) {
        is OneselfProfile -> {}
        is FollowedProfile -> {
            SimpleDropdownItem(
                text = stringResource(id = R.string.unfollow),
                onClick = {
                    onUpdate(UnfollowProfile(pubkey = uiEvent.authorProfile.pubkey))
                    onDismiss()
                }
            )
        }

        is TrustedProfile, is UnknownProfile -> {
            SimpleDropdownItem(
                text = stringResource(id = R.string.follow),
                onClick = {
                    onUpdate(FollowProfile(pubkey = uiEvent.authorProfile.pubkey))
                    onDismiss()
                }
            )
        }
    }
}

@Composable
private fun FollowCrossPostedItem(
    uiEvent: UIEvent,
    onDismiss: () -> Unit,
    onUpdate: (Cmd) -> Unit
) {
    if (uiEvent.inner != null) {
        when (uiEvent.inner.authorProfile) {
            is OneselfProfile -> {}
            is FollowedProfile -> {
                SimpleDropdownItem(
                    text = stringResource(id = R.string.unfollow_cross_posted_author),
                    onClick = {
                        onUpdate(UnfollowProfile(uiEvent.inner.authorProfile.pubkey))
                        onDismiss()
                    }
                )
            }

            is TrustedProfile, is UnknownProfile -> {
                SimpleDropdownItem(
                    text = stringResource(id = R.string.follow_cross_posted_author),
                    onClick = {
                        onUpdate(FollowProfile(uiEvent.inner.authorProfile.pubkey))
                        onDismiss()
                    }
                )
            }
        }
    }
}
