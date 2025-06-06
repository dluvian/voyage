package com.dluvian.voyage

import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.widget.Toast
import androidx.compose.ui.platform.Clipboard
import androidx.compose.ui.text.AnnotatedString
import rust.nostr.sdk.Event
import rust.nostr.sdk.EventId
import rust.nostr.sdk.KindStandard
import rust.nostr.sdk.Metadata
import rust.nostr.sdk.PublicKey
import rust.nostr.sdk.TagKind

fun Event.isReply(): Boolean {
    val kindStd = this.kind().asStd()
    if (kindStd == KindStandard.COMMENT) return true
    if (kindStd == KindStandard.TEXT_NOTE && this.tags().eventIds().isNotEmpty()) return true

    return false
}

fun Event.subject(): String? {
    return this.tags().find(TagKind.Subject)?.content()?.trim()
}

fun Event.client(): String? {
    return this.tags().find(TagKind.Client)?.content()?.trim()
}

fun Event.parentId(): EventId? {
    return when (this.kind().asStd()) {
        // TODO: Upstream nip10 and nip22 extract
        KindStandard.TEXT_NOTE -> this.tags().eventIds().firstOrNull()
        KindStandard.COMMENT -> this.tags().eventIds().firstOrNull()
        null -> null
        else -> this.tags().eventIds().firstOrNull()
    }
}

fun PublicKey.shortenedNpub(): String {
    return shortenNpub(this.toBech32())
}

fun shortenNpub(npub: String): String {
    return npub.take(10) + ":" + npub.takeLast(5)
}

fun Metadata.lightning(): String? {
    return this.getLud16() ?: this.getLud06()
}

fun normalizeName(str: String): String {
    return str
        .filterNot { it.isWhitespace() } // My preferred asthetics
        .take(MAX_NAME_LEN) // Don't keep monster names
}

fun copyAndToast(text: String, toast: String, context: Context, clip: Clipboard) {
    copyAndToast(text = AnnotatedString(text), toast = toast, context = context, clip = clip)
}

fun copyAndToast(text: AnnotatedString, toast: String, context: Context, clip: Clipboard) {
    val data = ClipData.newPlainText(text.text, text.text)
    clip.nativeClipboard.setPrimaryClip(data)
    Toast.makeText(context, toast, Toast.LENGTH_SHORT).show()
}

fun getTranslators(packageManager: PackageManager): List<ResolveInfo> {
    return packageManager
        .queryIntentActivities(createBaseProcessTextIntent(), 0)
        .filter { it.activityInfo.name.contains("translate") } // lmao
}

fun createProcessTextIntent(text: String, info: ResolveInfo): Intent {
    return createBaseProcessTextIntent()
        .putExtra(Intent.EXTRA_PROCESS_TEXT, text)
        .setClassName(
            info.activityInfo.packageName,
            info.activityInfo.name
        )
}

private fun createBaseProcessTextIntent(): Intent {
    return Intent()
        .setAction(Intent.ACTION_PROCESS_TEXT)
        .setType("text/plain")
}
