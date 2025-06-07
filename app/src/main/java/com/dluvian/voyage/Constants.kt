package com.dluvian.voyage

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

// TODO: Upstream: remove all "xyz = null" after default nullability
// TODO: Upstream: Comparable Timestamps: Check all timestamp.asSecs() calls
// TODO: Upstream: Timestamp arithmetics (+/-1): Check all timestamp.asSecs() calls
