package com.dluvian.voyage.data.provider

import android.util.Log
import androidx.compose.ui.platform.UriHandler
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import com.dluvian.voyage.core.ClickClickableText
import com.dluvian.voyage.core.OnUpdate
import com.dluvian.voyage.core.model.CoordinateMention
import com.dluvian.voyage.core.model.NeventMention
import com.dluvian.voyage.core.model.NostrMention
import com.dluvian.voyage.core.model.NoteMention
import com.dluvian.voyage.core.model.NprofileMention
import com.dluvian.voyage.core.model.NpubMention
import com.dluvian.voyage.core.model.RelayMention
import com.dluvian.voyage.core.utils.extractHashtags
import com.dluvian.voyage.core.utils.extractNostrMentions
import com.dluvian.voyage.core.utils.extractUrls
import com.dluvian.voyage.core.utils.shortenBech32
import com.dluvian.voyage.core.utils.shortenUrl
import com.dluvian.voyage.data.nostr.createNprofile
import com.dluvian.voyage.ui.theme.HyperlinkStyle
import com.dluvian.voyage.ui.theme.MentionAndHashtagStyle
import java.util.Collections

private const val TAG = "AnnotatedStringProvider"

class AnnotatedStringProvider(private val nameProvider: NameProvider) {
    companion object {
        const val NEVENT_TAG = "NEVENT"
        const val NOTE1_TAG = "NOTE1"
        const val NPROFILE_TAG = "NPROFILE"
        const val NPUB_TAG = "NPUB"
        const val HASHTAG = "HASHTAG"
        const val COORDINATE = "COORDINATE"
        const val RELAY = "RELAY"
    }

    private var uriHandler: UriHandler? = null
    private var onUpdate: OnUpdate? = null

    fun setUriHandler(uriHandler: UriHandler) {
        this.uriHandler = uriHandler
    }

    fun setOnUpdate(onUpdate: OnUpdate) {
        this.onUpdate = onUpdate
    }

    private val cache: MutableMap<String, AnnotatedString> =
        Collections.synchronizedMap(mutableMapOf())

    fun annotate(str: String): AnnotatedString {
        if (str.isEmpty()) return AnnotatedString("")
        val cached = cache[str]
        if (cached != null) return cached

        val urls = extractUrls(str)
        val nostrMentions = extractNostrMentions(str)
        val tokens = (urls + nostrMentions).toMutableList()
        val hashtags = extractHashtags(str).filter { hashtag ->
            tokens.none { isOverlapping(hashtag.range, it.range) }
        }
        tokens.addAll(hashtags)

        if (tokens.isEmpty()) return AnnotatedString(text = str)
        tokens.sortBy { it.range.first }

        val editedContent = StringBuilder(str)
        var isCacheable = true
        val result = buildAnnotatedString {
            for (token in tokens) {
                val firstIndex = editedContent.indexOf(token.value)
                if (firstIndex > 0) {
                    append(editedContent.subSequence(0, firstIndex))
                    editedContent.delete(0, firstIndex)
                }
                if (urls.contains(token)) {
                    pushStyledUrlAnnotation(url = token.value)
                } else if (hashtags.contains(token)) {
                    pushAnnotatedString(
                        tag = HASHTAG,
                        rawString = token.value,
                        style = MentionAndHashtagStyle,
                        displayString = token.value
                    )
                } else {
                    when (val nostrMention = NostrMention.from(token.value)) {
                        is NpubMention, is NprofileMention -> {
                            val nprofile = if (nostrMention is NprofileMention) {
                                nostrMention.nprofile
                            } else {
                                createNprofile(hex = nostrMention.hex)
                            }
                            val mentionedName = nameProvider.getName(nprofile = nprofile)
                            if (mentionedName == null) isCacheable = false
                            val name = "@${
                                mentionedName.orEmpty()
                                    .ifEmpty { nostrMention.bech32.shortenBech32() }
                            }"
                            pushAnnotatedString(
                                tag = if (nostrMention is NpubMention) NPUB_TAG else NPROFILE_TAG,
                                rawString = nostrMention.bech32,
                                style = MentionAndHashtagStyle,
                                displayString = name
                            )
                        }

                        is NoteMention, is NeventMention -> {
                            pushAnnotatedString(
                                tag = if (nostrMention is NoteMention) NOTE1_TAG else NEVENT_TAG,
                                rawString = nostrMention.bech32,
                                style = MentionAndHashtagStyle,
                                displayString = nostrMention.bech32.shortenBech32()
                            )
                        }

                        is CoordinateMention -> {
                            pushAnnotatedString(
                                tag = COORDINATE,
                                rawString = nostrMention.bech32,
                                style = MentionAndHashtagStyle,
                                displayString = nostrMention.identifier.ifEmpty { nostrMention.bech32.shortenBech32() }
                            )
                        }

                        is RelayMention -> {
                            pushAnnotatedString(
                                tag = RELAY,
                                rawString = nostrMention.bech32,
                                style = MentionAndHashtagStyle,
                                displayString = nostrMention.bech32.shortenBech32()
                            )
                        }

                        null -> {
                            Log.w(TAG, "Failed to identify ${token.value}")
                            append(token.value)
                        }

                    }
                }
                editedContent.delete(0, token.value.length)
            }
            append(editedContent)
        }
        if (isCacheable) cache[str] = result

        return result
    }

    private fun isOverlapping(hashtagRange: IntRange, otherRange: IntRange): Boolean {
        val isNotOverlapping = hashtagRange.last < otherRange.first
                || hashtagRange.first > otherRange.last
        return !isNotOverlapping
    }

    private fun AnnotatedString.Builder.pushAnnotatedString(
        tag: String,
        rawString: String,
        style: SpanStyle,
        displayString: String,
    ) {
        val clickable = LinkAnnotation
            .Clickable(tag = tag, styles = TextLinkStyles(style = style)) {
                val handler = uriHandler ?: return@Clickable
                val action = onUpdate ?: return@Clickable
                action(ClickClickableText(text = rawString, uriHandler = handler))
            }
        pushLink(clickable)
        append(displayString)
        pop()
    }

    private fun AnnotatedString.Builder.pushStyledUrlAnnotation(url: String) {
        pushLink(LinkAnnotation.Url(url = url, styles = TextLinkStyles(style = HyperlinkStyle)))
        append(shortenUrl(url = url))
        pop()
    }
}
