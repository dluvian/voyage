package com.dluvian.voyage.core

const val MAX_CONTENT_LEN = 8_192
const val MAX_TITLE_LEN = 256
const val MAX_TOPIC_LEN = 32
const val MAX_TOPICS = 3
const val MAX_NAME_LEN = 32

const val DELAY_1SEC = 1000L
const val DELAY_10SEC = 10 * DELAY_1SEC

const val RESUB_TIMEOUT = DELAY_10SEC

const val SHORT_DEBOUNCE = 300L
const val DEBOUNCE = 600L

const val MAX_EVENTS_TO_SUB = 1000uL

const val MAX_RELAYS = 6
const val MAX_RELAYS_PER_PUBKEY = 6

const val MAX_PUBKEYS = 750

const val RND_RESUB_COUNT = 20

const val FEED_PAGE_SIZE = 35
const val FEED_OFFSET = 7

const val TWO_WEEKS_IN_SECS = 14 * 24 * 60 * 60

const val MIN_RETAIN_ROOT = 500f
const val MAX_RETAIN_ROOT = 10_000f
const val DEFAULT_RETAIN_ROOT = 1500
