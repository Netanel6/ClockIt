package com.netanel.clockit.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(tableName = "shifts")
data class ShiftEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val date: LocalDate,
    val hourlyRate: Double,
    val workedMinutes: Int,
    val isHolidayOrShabbat: Boolean,
    val km: Double,
    val engineCc: Int,          // ← חדש
    val callouts: Int,
    val caughtFound: Int
)
