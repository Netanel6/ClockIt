package com.netanel.clockit.utils

fun parseRawKm(raw: String): Double? {
    if (raw.isBlank()) return null
    val canon = raw.replace(',', '.').trim()
    return canon.toDoubleOrNull()
}
