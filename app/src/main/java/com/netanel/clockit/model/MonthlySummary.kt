package com.netanel.clockit.model

import java.time.YearMonth

data class MonthlySummary(
    val month: YearMonth,
    val totalBase: Double,
    val totalOt1: Double,
    val totalOt2: Double,
    val totalTravel: Double,
    val totalCallouts: Double,
    val totalCaught: Double,
    val grandTotal: Double
)
