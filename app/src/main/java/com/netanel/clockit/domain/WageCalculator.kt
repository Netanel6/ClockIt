package com.netanel.clockit.domain

import com.netanel.clockit.model.*

class WageCalculator {
    fun calculateFromMinutes(
        workedMinutes: Int,
        hourlyRate: Double,
        isHolidayOrShabbat: Boolean,
        km: Double,
        engineCc: Int,                     // ← חדש
        callouts: Int,
        caughtFound: Int,
        profile: CalcProfile
    ): CalcResult {
        val holidayMul = if (isHolidayOrShabbat) profile.holidayMultiplier else 1.0

        val regularCap = (profile.regularDailyThreshold * 60).toInt().coerceAtLeast(0)
        val regularMinutes = workedMinutes.coerceAtMost(regularCap)
        val afterRegular = (workedMinutes - regularMinutes).coerceAtLeast(0)
        val ot1Minutes = afterRegular.coerceAtMost(profile.overtime1CapMinutes)
        val ot2Minutes = (afterRegular - ot1Minutes).coerceAtLeast(0)

        fun minutesToHours(m: Int) = m / 60.0
        fun applyHoliday(x: Double) = if (profile.holidayAffectsAllBuckets) x * holidayMul else x

        var base = hourlyRate * minutesToHours(regularMinutes)
        base = if (profile.holidayAffectsAllBuckets) applyHoliday(base) else base * holidayMul

        var ot1 = hourlyRate * minutesToHours(ot1Minutes) * profile.overtime1Multiplier
        var ot2 = hourlyRate * minutesToHours(ot2Minutes) * profile.overtime2Multiplier
        if (profile.holidayAffectsAllBuckets) { ot1 *= holidayMul; ot2 *= holidayMul }

        val kmRate = profile.kmRateFor(engineCc)
        val travel = kmRate * km

        val calloutsPay = profile.calloutRate * callouts
        val caught = profile.caughtBonus * caughtFound

        val total = base + ot1 + ot2 + travel + calloutsPay + caught
        return CalcResult(base, ot1, ot2, travel, calloutsPay, caught, total)
    }
}
