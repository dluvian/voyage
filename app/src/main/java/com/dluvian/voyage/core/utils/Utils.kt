package com.dluvian.voyage.core.utils

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.text.format.DateUtils
import android.widget.Toast
import androidx.compose.material3.SnackbarHostState
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import com.dluvian.voyage.core.Bech32
import com.dluvian.voyage.core.EventIdHex
import com.dluvian.voyage.core.MAX_EVENTS_TO_SUB
import com.dluvian.voyage.core.MAX_SUBJECT_LEN
import com.dluvian.voyage.core.PubkeyHex
import com.dluvian.voyage.core.Topic
import com.dluvian.voyage.core.VOYAGE
import com.dluvian.voyage.core.model.MainEvent
import com.dluvian.voyage.core.model.SomeReply
import com.dluvian.voyage.data.event.COMMENT_U16
import com.dluvian.voyage.data.event.POLL_U16
import com.dluvian.voyage.data.model.ForcedData
import com.dluvian.voyage.data.model.RelevantMetadata
import com.dluvian.voyage.data.nostr.LOCAL_WEBSOCKET
import com.dluvian.voyage.data.nostr.RelayUrl
import com.dluvian.voyage.data.nostr.getSubject
import com.dluvian.voyage.data.provider.AnnotatedStringProvider
import com.dluvian.voyage.data.provider.FriendProvider
import com.dluvian.voyage.data.provider.ItemSetProvider
import com.dluvian.voyage.data.provider.LockProvider
import com.dluvian.voyage.data.provider.MuteProvider
import com.dluvian.voyage.data.room.view.AdvancedProfileView
import com.dluvian.voyage.data.room.view.CommentView
import com.dluvian.voyage.data.room.view.CrossPostView
import com.dluvian.voyage.data.room.view.LegacyReplyView
import com.dluvian.voyage.data.room.view.PollOptionView
import com.dluvian.voyage.data.room.view.PollView
import com.dluvian.voyage.data.room.view.RootPostView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import rust.nostr.sdk.Alphabet
import rust.nostr.sdk.Event
import rust.nostr.sdk.Filter
import rust.nostr.sdk.Kind
import rust.nostr.sdk.KindStandard
import rust.nostr.sdk.Metadata
import rust.nostr.sdk.PublicKey
import rust.nostr.sdk.SingleLetterTag
import rust.nostr.sdk.Tag

fun PublicKey.toShortenedNpub(): String {
    return this.toBech32().shortenBech32()
}

fun Bech32.shortenBech32() = "${this.take(10)}:${this.takeLast(5)}"

fun PubkeyHex.toShortenedBech32(): String {
    if (this.isEmpty()) return ""
    val pubkey = runCatching { PublicKey.parse(this) }.getOrNull() ?: return ""
    return pubkey.toShortenedNpub()
}

fun PubkeyHex.toBech32(): String {
    if (this.isEmpty()) return ""
    return runCatching { PublicKey.parse(this).toBech32() }.getOrNull() ?: ""
}

fun Metadata.toRelevantMetadata(pubkey: PubkeyHex, createdAt: Long): RelevantMetadata {
    return RelevantMetadata(
        npub = pubkey.toBech32(),
        name = this.getNormalizedName(),
        about = this.getAbout()?.trim(),
        lightning = this.getLud16().orEmpty().ifEmpty { this.getLud06() },
        createdAt = createdAt
    )
}

fun SnackbarHostState.showToast(scope: CoroutineScope, msg: String) {
    this.currentSnackbarData?.dismiss()
    scope.launch {
        this@showToast.showSnackbar(message = msg, withDismissAction = true)
    }
}

fun <K, V> MutableMap<K, MutableList<V>>.syncedPutOrAdd(key: K, value: MutableList<V>) {
    val alreadyPresent: MutableList<V>?
    synchronized(this) {
        alreadyPresent = this.putIfAbsent(key, value)
    }
    if (alreadyPresent != null) {
        synchronized(alreadyPresent) {
            alreadyPresent.addAll(value)
        }
    }
}

fun <K, V> MutableMap<K, MutableSet<V>>.syncedPutOrAdd(key: K, value: Collection<V>) {
    val alreadyPresent: MutableSet<V>?
    synchronized(this) {
        alreadyPresent = this.putIfAbsent(key, value.toMutableSet())
    }
    if (alreadyPresent != null) {
        synchronized(alreadyPresent) {
            alreadyPresent.addAll(value)
        }
    }
}

fun <K, V> MutableMap<K, MutableSet<V>>.putOrAdd(key: K, value: Collection<V>): Boolean {
    val alreadyPresent = this.putIfAbsent(key, value.toMutableSet())
    return alreadyPresent?.addAll(value) ?: false
}

@OptIn(FlowPreview::class)
fun <T> Flow<T>.firstThenDistinctDebounce(millis: Long): Flow<T> {
    return flow {
        emitAll(this@firstThenDistinctDebounce.take(1))
        emitAll(
            this@firstThenDistinctDebounce.drop(1)
                .distinctUntilChanged()
                .debounce(millis)
        )
    }
}

fun CoroutineScope.launchIO(block: suspend CoroutineScope.() -> Unit): Job {
    return this.launch(Dispatchers.IO) { block() }
}

fun <T> Collection<T>.takeRandom(n: Int): List<T> {
    return if (this.size <= n || n < 0) this.toList() else this.shuffled().take(n)
}

private val urlRegex = Regex(pattern = "https?://[^\\s]+[a-zA-Z0-9/]")
fun extractUrls(extractFrom: String) = urlRegex.findAll(extractFrom).toList()


private val nostrMentionRegex =
    Regex("(nostr:|@)(npub1|note1|nevent1|nprofile1|naddr1|nrelay)[a-zA-Z0-9]+")

fun extractNostrMentions(extractFrom: String) = nostrMentionRegex.findAll(extractFrom).toList()

private val MEDIA_SUFFIXES = listOf(
    ".jpeg",
    ".jpg",
    ".gif",
    ".png",
    ".webp",
    ".mp4",
    ".mov",
    ".mp3",
    ".webm",
    ".wav",
    ".bmp",
    ".svg",
    ".avi",
    ".mpg",
    ".mpeg",
    ".wmv"
)

fun shortenUrl(url: String): String {
    val noPrefix = url.removePrefix("https://").removePrefix("http://")
    if (!noPrefix.contains('/') || MEDIA_SUFFIXES.none { noPrefix.endsWith(it) }) {
        return noPrefix
    }

    val firstSlash = noPrefix.indexOfFirst { it == '/' }
    val lastDot = noPrefix.indexOfLast { it == '.' }

    if (lastDot - firstSlash <= 12) return noPrefix

    val firstN = firstSlash + 1
    val lastN = noPrefix.length - lastDot

    return noPrefix.take(firstN) +
            noPrefix.drop(firstN).take(3) +
            "…" +
            noPrefix.dropLast(lastN).take(3) +
            noPrefix.takeLast(lastN)
}

private val hashtagRegex = Regex("""#\w+(-\w+)*""")
private val bareTopicRegex = Regex("[^#\\s]+\$")

fun extractCleanHashtags(content: String): List<Topic> {
    return hashtagRegex.findAll(content)
        .map { it.value.normalizeTopic() }
        .distinct()
        .toList()
}

fun extractHashtags(extractFrom: String) = hashtagRegex.findAll(extractFrom).toList()

fun String.isBareTopicStr(): Boolean = bareTopicRegex.matches(this)

fun copyAndToast(text: String, toast: String, context: Context, clip: ClipboardManager) {
    copyAndToast(text = AnnotatedString(text), toast = toast, context = context, clip = clip)
}

fun copyAndToast(text: AnnotatedString, toast: String, context: Context, clip: ClipboardManager) {
    clip.setText(text)
    Toast.makeText(context, toast, Toast.LENGTH_SHORT).show()
}

fun mergeRelayFilters(vararg maps: Map<RelayUrl, List<Filter>>): Map<RelayUrl, List<Filter>> {
    val result = mutableMapOf<RelayUrl, MutableList<Filter>>()
    for (map in maps) {
        map.forEach { (relay, filters) ->
            val present = result.putIfAbsent(relay, filters.toMutableList())
            present?.addAll(filters)
        }
    }

    return result
}

val commentableKinds = listOf(
    Kind.fromStd(KindStandard.TEXT_NOTE),
    Kind(kind = COMMENT_U16),
    Kind(kind = POLL_U16)
)

val crossPostableKinds = listOf(
    Kind.fromStd(KindStandard.TEXT_NOTE),
    Kind(kind = COMMENT_U16),
)

val rootFeedableKindsNoKTag = listOf(
    Kind.fromStd(KindStandard.TEXT_NOTE),
    Kind.fromStd(KindStandard.REPOST),
    Kind(kind = POLL_U16)
)

val replyKinds = listOf(
    Kind.fromStd(KindStandard.TEXT_NOTE),
    Kind(kind = COMMENT_U16),
)

val reactionKind = Kind.fromStd(KindStandard.TEXT_NOTE)

val reactionaryKinds = replyKinds + reactionKind

val threadableKinds = listOf(
    Kind.fromStd(KindStandard.TEXT_NOTE),
    Kind(kind = COMMENT_U16),
    Kind(kind = POLL_U16)
)


fun Event.getTrimmedSubject(maxLen: Int = MAX_SUBJECT_LEN): String? {
    return this.getSubject()?.trim()?.take(maxLen)
}

fun Filter.limitRestricted(limit: ULong, upperLimit: ULong = MAX_EVENTS_TO_SUB): Filter {
    return this.limit(minOf(limit, upperLimit))
}

fun Filter.genericRepost(): Filter {
    return this
        .kind(Kind.fromStd(KindStandard.GENERIC_REPOST))
        .customTags(
            tag = SingleLetterTag.lowercase(Alphabet.K),
            contents = crossPostableKinds.map { it.asU16().toString() }
        )
}

fun getTranslators(packageManager: PackageManager): List<ResolveInfo> {
    return packageManager
        .queryIntentActivities(createBaseProcessTextIntent(), 0)
        .filter { it.activityInfo.name.contains("translate") } // lmao
}

private fun createBaseProcessTextIntent(): Intent {
    return Intent()
        .setAction(Intent.ACTION_PROCESS_TEXT)
        .setType("text/plain")
}

fun createProcessTextIntent(text: String, info: ResolveInfo): Intent {
    return createBaseProcessTextIntent()
        .putExtra(Intent.EXTRA_PROCESS_TEXT, text)
        .setClassName(
            info.activityInfo.packageName,
            info.activityInfo.name
        )
}

fun mergeToMainEventUIList(
    roots: Collection<RootPostView>,
    crossPosts: Collection<CrossPostView>,
    polls: Collection<PollView>,
    pollOptions: Collection<PollOptionView>,
    legacyReplies: Collection<LegacyReplyView>,
    comments: Collection<CommentView>,
    forcedData: ForcedData,
    size: Int,
    annotatedStringProvider: AnnotatedStringProvider,
): List<MainEvent> {
    return mergeToMainEventUIList(
        roots = roots,
        crossPosts = crossPosts,
        polls = polls,
        pollOptions = pollOptions,
        legacyReplies = legacyReplies,
        comments = comments,
        votes = forcedData.votes,
        follows = forcedData.follows,
        bookmarks = forcedData.bookmarks,
        size = size,
        annotatedStringProvider = annotatedStringProvider
    )
}

fun mergeToMainEventUIList(
    roots: Collection<RootPostView>,
    crossPosts: Collection<CrossPostView>,
    polls: Collection<PollView>,
    pollOptions: Collection<PollOptionView>,
    legacyReplies: Collection<LegacyReplyView>,
    comments: Collection<CommentView>,
    votes: Map<EventIdHex, Boolean>,
    follows: Map<PubkeyHex, Boolean>,
    bookmarks: Map<EventIdHex, Boolean>,
    size: Int,
    annotatedStringProvider: AnnotatedStringProvider,
): List<MainEvent> {
    val applicableTimestamps = roots.asSequence()
        .map { it.createdAt }
        .plus(crossPosts.map { it.createdAt })
        .plus(legacyReplies.map { it.createdAt })
        .plus(comments.map { it.createdAt })
        .plus(polls.map { it.createdAt })
        .sortedDescending()
        .take(size)
        .toSet()

    val result = mutableListOf<MainEvent>()
    for (post in roots) {
        if (!applicableTimestamps.contains(post.createdAt)) continue
        val mapped = post.mapToRootPostUI(
            forcedVotes = votes,
            forcedFollows = follows,
            forcedBookmarks = bookmarks,
            annotatedStringProvider = annotatedStringProvider
        )
        result.add(mapped)
    }
    for (cross in crossPosts) {
        if (!applicableTimestamps.contains(cross.createdAt)) continue
        val mapped = cross.mapToCrossPostUI(
            forcedVotes = votes,
            forcedFollows = follows,
            forcedBookmarks = bookmarks,
            annotatedStringProvider = annotatedStringProvider
        )
        result.add(mapped)
    }
    for (poll in polls) {
        if (!applicableTimestamps.contains(poll.createdAt)) continue
        val mapped = poll.mapToPollUI(
            pollOptions = pollOptions.filter { it.pollId == poll.id },
            forcedVotes = votes,
            forcedFollows = follows,
            forcedBookmarks = bookmarks,
            annotatedStringProvider = annotatedStringProvider
        )
        result.add(mapped)
    }
    for (reply in legacyReplies) {
        if (!applicableTimestamps.contains(reply.createdAt)) continue
        val mapped = reply.mapToLegacyReplyUI(
            forcedVotes = votes,
            forcedFollows = follows,
            forcedBookmarks = bookmarks,
            annotatedStringProvider = annotatedStringProvider
        )
        result.add(mapped)
    }
    for (comment in comments) {
        if (!applicableTimestamps.contains(comment.createdAt)) continue
        val mapped = comment.mapToCommentUI(
            forcedVotes = votes,
            forcedFollows = follows,
            forcedBookmarks = bookmarks,
            annotatedStringProvider = annotatedStringProvider
        )
        result.add(mapped)
    }

    return result.sortedByDescending { it.createdAt }.take(size)
}

fun mergeToSomeReplyUIList(
    legacyReplies: Collection<LegacyReplyView>,
    comments: Collection<CommentView>,
    votes: Map<EventIdHex, Boolean>,
    follows: Map<PubkeyHex, Boolean>,
    bookmarks: Map<EventIdHex, Boolean>,
    size: Int,
    annotatedStringProvider: AnnotatedStringProvider,
): List<SomeReply> {
    val result = mutableListOf<SomeReply>()

    mergeToMainEventUIList(
        roots = emptyList(),
        crossPosts = emptyList(),
        polls = emptyList(),
        pollOptions = emptyList(),
        legacyReplies = legacyReplies,
        comments = comments,
        votes = votes,
        follows = follows,
        bookmarks = bookmarks,
        size = size,
        annotatedStringProvider = annotatedStringProvider
    ).forEach { if (it is SomeReply) result.add(it) }

    return result
}

fun createAdvancedProfile(
    pubkey: PubkeyHex,
    dbProfile: AdvancedProfileView?,
    forcedFollowState: Boolean?,
    forcedMuteState: Boolean?,
    metadata: RelevantMetadata?,
    myPubkey: PubkeyHex,
    friendProvider: FriendProvider,
    muteProvider: MuteProvider,
    itemSetProvider: ItemSetProvider,
    lockProvider: LockProvider,
): AdvancedProfileView {
    val name = normalizeName(metadata?.name.orEmpty().ifEmpty { dbProfile?.name.orEmpty() })
        .ifEmpty { pubkey.toShortenedBech32() }
    return AdvancedProfileView(
        pubkey = pubkey,
        name = name,
        isMe = dbProfile?.isMe ?: (myPubkey == pubkey),
        isFriend = forcedFollowState ?: dbProfile?.isFriend
        ?: friendProvider.isFriend(pubkey = pubkey),
        isWebOfTrust = dbProfile?.isWebOfTrust ?: false,
        isMuted = forcedMuteState ?: dbProfile?.isMuted ?: muteProvider.isMuted(pubkey = pubkey),
        isInList = dbProfile?.isInList ?: itemSetProvider.isInAnySet(pubkey = pubkey),
        isLocked = dbProfile?.isLocked ?: lockProvider.isLocked(pubkey = pubkey)
    )
}

fun createLocalRelayUrl(port: Int?): String? {
    return if (port != null) "$LOCAL_WEBSOCKET$port" else null
}

fun Collection<RelayUrl>.addLocalRelay(port: Int?): List<RelayUrl> {
    val local = createLocalRelayUrl(port = port)
    return if (local != null) {
        mutableListOf(local).apply { addAll(this@addLocalRelay) }
    } else {
        this.toList()
    }
}

fun String.containsNoneIgnoreCase(strs: Collection<String>): Boolean {
    return strs.none { this.contains(other = it, ignoreCase = true) }
}

fun String.toTextFieldValue() = TextFieldValue(text = this, selection = TextRange(this.length))

fun createVoyageClientTag() = Tag.parse(listOf("client", VOYAGE))

fun getFullDateTime(ctx: Context, createdAt: Long): String {
    return DateUtils.formatDateTime(
        ctx,
        createdAt * 1000,
        DateUtils.FORMAT_SHOW_TIME or
                DateUtils.FORMAT_SHOW_DATE or
                DateUtils.FORMAT_SHOW_YEAR or
                DateUtils.FORMAT_SHOW_WEEKDAY or
                DateUtils.FORMAT_ABBREV_ALL
    ) + "  ($createdAt)"
}
