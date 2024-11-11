package com.dluvian.voyage.core

const val DLUVIAN_HEX = "e4336cd525df79fa4d3af364fd9600d4b10dce4215aa4c33ed77ea0842344b10"
const val VOYAGE = "voyage"

const val WEEK_IN_SECS = 7 * 24 * 60 * 60

const val MAX_CONTENT_LEN = 8_192
const val MAX_DESCRIPTION_LEN = 1024
const val MAX_SUBJECT_LEN = 256
const val MAX_MUTE_WORD_LEN = 32
const val MAX_NAME_LEN = 32
const val MAX_TOPIC_LEN = 32
const val MAX_TOPICS = 5
const val MAX_POLL_OPTIONS = 12
const val MAX_POLL_OPTION_LEN = 128

const val MAX_SUBJECT_LINES = 3
const val MAX_CONTENT_LINES = 12

const val DELAY_1SEC = 1000L
const val DELAY_10SEC = 10 * DELAY_1SEC

const val REBROADCAST_DELAY = 2 * DELAY_1SEC

const val RESUB_TIMEOUT = 2 * DELAY_10SEC

const val SHORT_DEBOUNCE = 300L
const val DEBOUNCE = 600L

const val LIST_CHANGE_DEBOUNCE = DELAY_1SEC

const val LAZY_RND_RESUB_LIMIT = 10uL

const val AUTH_TIMEOUT = DELAY_10SEC

const val MAX_RELAYS = 5
const val MAX_RELAYS_PER_PUBKEY = 2
const val MAX_POPULAR_RELAYS = 50

const val MIN_AUTOPILOT_RELAYS = MAX_RELAYS
const val DEFAULT_AUTOPILOT_RELAYS = 12
const val MAX_AUTOPILOT_RELAYS = 25

const val MAX_KEYS = 750 // Filter requests might get too long for most relays. Limit to 750
const val MAX_KEYS_SQL = 4 * MAX_KEYS // Exception when query gets too long
const val MAX_EVENTS_TO_SUB = 200uL

const val FEED_PAGE_SIZE = 30
const val FEED_OFFSET = 6

const val MIN_RETAIN_ROOT = 500f
const val MAX_RETAIN_ROOT = 5_000f
const val DEFAULT_RETAIN_ROOT = 1500
