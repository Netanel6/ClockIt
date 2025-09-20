package com.netanel.clockit.model

data class CalcProfile(
    val hourlyRate: Double = 50.0,

    // ערכי ברירת מחדל לשאר התוספות
    val calloutRate: Double = 225.0,
    val stolenBonus: Double = 225.0,

    val regularDailyThreshold: Double = 8.0,
    val overtime1Multiplier: Double = 1.25,
    val overtime2Multiplier: Double = 1.50,
    val overtime1CapMinutes: Int = 120,

    val holidayMultiplier: Double = 2.0,
    val holidayAffectsAllBuckets: Boolean = true
) {
    fun kmRateFor(engineCc: Int): Double = when {
        engineCc <= 2000 -> 3.19
        engineCc in 2001..3000 -> 3.64
        else -> 3.64 // אם תרצה מדרגה נוספת ל-3000+, תגיד ונוסיף
    }
}
