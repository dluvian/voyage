package com.dluvian.voyage.data.provider

import android.util.Log
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.UrlAnnotation
import androidx.compose.ui.text.buildAnnotatedString
import com.dluvian.nostr_kt.createNprofile
import com.dluvian.voyage.core.extractHashtags
import com.dluvian.voyage.core.extractNostrMentions
import com.dluvian.voyage.core.extractUrls
import com.dluvian.voyage.core.model.CoordinateMention
import com.dluvian.voyage.core.model.NeventMention
import com.dluvian.voyage.core.model.NostrMention
import com.dluvian.voyage.core.model.NoteMention
import com.dluvian.voyage.core.model.NprofileMention
import com.dluvian.voyage.core.model.NpubMention
import com.dluvian.voyage.core.shortenBech32
import com.dluvian.voyage.core.shortenUrl
import com.dluvian.voyage.ui.theme.HyperlinkStyle
import com.dluvian.voyage.ui.theme.MentionAndHashtagStyle
import java.util.Collections

private const val TAG = "AnnotatedStringProvider"

class AnnotatedStringProvider(
    private val nameProvider: NameProvider,
) {
    companion object {
        const val NEVENT_TAG = "NEVENT"
        const val NOTE1_TAG = "NOTE1"
        const val NPROFILE_TAG = "NPROFILE"
        const val NPUB_TAG = "NPUB"
        const val HASHTAG = "HASHTAG"
        const val COORDINATE = "COORDINATE"
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
                    pushStyledUrlAnnotation(
                        url = token.value,
                        style = HyperlinkStyle
                    )
                } else if (hashtags.contains(token)) {
                    pushAnnotatedString(
                        tag = HASHTAG,
                        annotation = token.value,
                        style = MentionAndHashtagStyle,
                        text = token.value
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
                                annotation = nostrMention.bech32,
                                style = MentionAndHashtagStyle,
                                text = name
                            )
                        }

                        is NoteMention, is NeventMention -> {
                            pushAnnotatedString(
                                tag = if (nostrMention is NoteMention) NOTE1_TAG else NEVENT_TAG,
                                annotation = nostrMention.bech32,
                                style = MentionAndHashtagStyle,
                                text = nostrMention.bech32.shortenBech32()
                            )
                        }

                        is CoordinateMention -> {
                            pushAnnotatedString(
                                tag = COORDINATE,
                                annotation = nostrMention.bech32,
                                style = MentionAndHashtagStyle,
                                text = nostrMention.identifier
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
        annotation: String,
        style: SpanStyle,
        text: String
    ) {
        pushStringAnnotation(tag = tag, annotation = annotation)
        pushStyledString(style = style, text = text)
        pop()
    }

    @OptIn(ExperimentalTextApi::class)
    private fun AnnotatedString.Builder.pushStyledUrlAnnotation(url: String, style: SpanStyle) {
        pushUrlAnnotation(UrlAnnotation(url = url))
        pushStyledString(style = style, text = shortenUrl(url))
        pop()
    }

    private fun AnnotatedString.Builder.pushStyledString(style: SpanStyle, text: String) {
        pushStyle(style = style)
        append(text)
        pop()
    }
}
