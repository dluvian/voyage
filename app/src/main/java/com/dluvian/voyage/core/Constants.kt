package com.dluvian.voyage.core

const val MAX_CONTENT_LEN = 8_192
const val MAX_SUBJECT_LEN = 256
const val MAX_TOPIC_LEN = 32
const val MAX_TOPICS = 7
const val MAX_NAME_LEN = 32

const val DELAY_1SEC = 1000L
const val DELAY_10SEC = 10 * DELAY_1SEC

const val RESUB_TIMEOUT = 3 * DELAY_10SEC

const val SHORT_DEBOUNCE = 300L
const val DEBOUNCE = 600L

const val LIST_CHANGE_DEBOUNCE = DELAY_1SEC

const val LAZY_RND_RESUB_LIMIT = 10uL

const val AUTH_TIMEOUT = DELAY_10SEC

const val MAX_RELAY_CONNECTIONS = 12
const val MAX_RELAYS = 5
const val MAX_RELAYS_PER_PUBKEY = 2
const val MAX_POPULAR_RELAYS = 50

const val MAX_KEYS = 750 // Filter requests might get too long for most relays. Limit to 750
const val MAX_KEYS_SQL = 4 * MAX_KEYS // Exception when query gets too long
const val MAX_EVENTS_TO_SUB = 200uL

const val FEED_PAGE_SIZE = 30
const val FEED_OFFSET = 6
const val FEED_RESUB_SPAN_THRESHOLD_SECS =
    4 * 60 * 60 // Dont resub page's time span if it covers less than 4h

const val MIN_RETAIN_ROOT = 500f
const val MAX_RETAIN_ROOT = 5_000f
const val DEFAULT_RETAIN_ROOT = 1500
