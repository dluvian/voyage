package com.dluvian.voyage

import rust.nostr.sdk.Coordinate
import rust.nostr.sdk.Kind
import rust.nostr.sdk.KindStandard
import rust.nostr.sdk.PublicKey

const val APP_NAME = "voyage"
const val PAGE_SIZE = 25
const val MAX_PUBKEYS = 500 // TODO: Use this
const val MAX_TOPICS = 5
const val MAX_NAME_LEN = 32
const val MAX_LINES_SUBJECT = 3
const val MAX_LINES_CONTENT = 12
const val MAX_RELAYS = 5

const val SHORT_DELAY = 2000L
const val LONG_DELAY = 3 * SHORT_DELAY

private const val DAY_IN_SECS = 24 * 60 * 60
const val DB_SWEEP_THRESHOLD = (90 * DAY_IN_SECS)

val DLUVIAN_PUBKEY =
    PublicKey.parse("npub1useke4f9maul5nf67dj0m9sq6jcsmnjzzk4ycvldwl4qss35fvgqjdk5ks")
val VOYAGE_REPO_COORDINATE = Coordinate(
    kind = Kind.fromStd(KindStandard.GIT_REPO_ANNOUNCEMENT),
    publicKey = DLUVIAN_PUBKEY,
    identifier = "voyage"
)

// TODO: Upstream: remove all "xyz = null" after default nullability
// TODO: Upstream: Comparable Timestamps: Check all timestamp.asSecs() calls
// TODO: Upstream: Timestamp arithmetics (+/-1): Check all timestamp.asSecs() calls
