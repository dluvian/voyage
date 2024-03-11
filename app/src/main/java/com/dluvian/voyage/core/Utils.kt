package com.dluvian.voyage.core

fun getRatioInPercent(countA: Int, countB: Int): Int {
    val sum = countA + countB
    return if (sum == 0) 100 else (countA / sum) * 100
}
