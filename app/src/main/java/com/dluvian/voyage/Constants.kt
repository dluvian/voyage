package com.dluvian.voyage

const val APP_NAME = "voyage"
const val PAGE_SIZE = 25
const val MAX_PUBKEYS = 500
const val MAX_NAME_LEN = 32

const val SHORT_DELAY = 2000L
const val LONG_DELAY = 3 * SHORT_DELAY

private const val DAY_IN_SECS = 24 * 60 * 60
const val DB_SWEEP_THRESHOLD = (90 * DAY_IN_SECS)
