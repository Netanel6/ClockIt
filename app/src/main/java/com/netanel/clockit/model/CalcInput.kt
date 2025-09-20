package com.netanel.clockit.model

data class CalcInput(
    val hourlyRate: Double,
    val regularHours: Double,
    val overtime1Hours: Double,
    val overtime2Hours: Double,
    val isHolidayOrShabbat: Boolean,
    val km: Double,
    val callouts: Int,
    val stolenFound: Int
)
