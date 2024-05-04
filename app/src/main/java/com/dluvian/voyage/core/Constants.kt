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

const val LIST_CHANGE_DEBOUNCE = 1500L

const val MAX_VOTES_TO_SUB = 200uL
const val MAX_REPLIES_TO_SUB = 150uL
const val MAX_EVENTS_TO_SUB = 1000uL
const val AUTH_TIMEOUT = DELAY_10SEC

const val MAX_RELAYS = 5
const val MAX_RELAYS_PER_PUBKEY = 2
const val MAX_POPULAR_RELAYS = 50

const val MAX_PUBKEYS = 750

const val MAX_RND_RESUB_PERCENTAGE = 0.12f

const val FEED_PAGE_SIZE = 35
const val FEED_OFFSET = 7
const val FEED_RESUB_SPAN_THRESHOLD_SECS = 4 * 60 * 60

const val MIN_RETAIN_ROOT = 500f
const val MAX_RETAIN_ROOT = 5_000f
const val DEFAULT_RETAIN_ROOT = 1500
