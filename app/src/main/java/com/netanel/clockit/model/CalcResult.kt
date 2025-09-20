package com.netanel.clockit.model

data class CalcResult(
    val basePay: Double,
    val overtime1Pay: Double,
    val overtime2Pay: Double,
    val travelPay: Double,
    val calloutsPay: Double,
    val caughtBonusPay: Double,
    val total: Double
)
