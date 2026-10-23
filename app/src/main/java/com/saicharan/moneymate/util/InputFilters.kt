package com.saicharan.moneymate.util

/** Keeps only digits and a single decimal point; caps the length. */
fun sanitizeAmountInput(raw: String, maxLength: Int = 13): String {
    val sb = StringBuilder()
    var seenDot = false
    for (c in raw) {
        when {
            c.isDigit() -> sb.append(c)
            c == '.' && !seenDot -> { sb.append(c); seenDot = true }
        }
    }
    return sb.toString().take(maxLength)
}
