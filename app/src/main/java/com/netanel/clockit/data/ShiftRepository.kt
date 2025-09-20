package com.netanel.clockit.data

import com.netanel.clockit.model.Shift
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface ShiftRepository {
    suspend fun addShift(shift: Shift): Long
    fun observeShifts(from: LocalDate, to: LocalDate): Flow<List<Shift>>
    suspend fun getShift(id: Long): Shift?
    suspend fun getShiftByDate(date: LocalDate): Shift?
    suspend fun updateShift(shift: Shift)
    suspend fun deleteShift(id: Long)
}
