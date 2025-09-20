package com.netanel.clockit.utils

/**
 * Accept raw digits only, as-is, on save:
 * - "0815" -> 8:15
 * - "815"  -> 8:15
 * - "8" / "80" etc. are rejected (return null) to avoid guessing.
 * - (Optional) If you want to accept "08:15", strip non-digits before length check.
 */
fun parseRawHoursToMinutes(raw: String): Int? {
    val digits = raw.filter { it.isDigit() }
    if (digits.length !in 3..4) return null
    val hPart = digits.dropLast(2)
    val mPart = digits.takeLast(2)
    val h = hPart.toIntOrNull() ?: return null
    val m = mPart.toIntOrNull() ?: return null
    if (m !in 0..59) return null
    return h * 60 + m
}
