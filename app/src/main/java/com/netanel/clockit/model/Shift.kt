package com.netanel.clockit.model

import java.time.LocalDate

data class Shift(
    val id: Long = 0L,
    val date: LocalDate,
    val hourlyRate: Double,
    val workedMinutes: Int,
    val isHolidayOrShabbat: Boolean,
    val km: Double,
    val engineCc: Int = 2000,
    val callouts: Int,
    val caughtFound: Int
)
