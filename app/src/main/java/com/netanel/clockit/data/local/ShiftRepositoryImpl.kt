package com.netanel.clockit.data.local

import com.netanel.clockit.data.ShiftRepository
import com.netanel.clockit.model.Shift
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate

class ShiftRepositoryImpl(private val dao: ShiftDao) : ShiftRepository {

    override suspend fun addShift(shift: Shift): Long = dao.insert(shift.toEntity())

    override fun observeShifts(from: LocalDate, to: LocalDate): Flow<List<Shift>> =
        dao.observeByDateRange(from, to).map { it.map(ShiftEntity::toModel) }

    override suspend fun getShift(id: Long): Shift? = dao.getById(id)?.toModel()

    override suspend fun getShiftByDate(date: LocalDate): Shift? =   // ← חדש
        dao.getByDate(date)?.toModel()

    override suspend fun updateShift(shift: Shift) = dao.update(shift.toEntity())

    override suspend fun deleteShift(id: Long) = dao.deleteById(id)
}


private fun Shift.toEntity() = ShiftEntity(
    id = id,
    date = date,
    hourlyRate = hourlyRate,
    workedMinutes = workedMinutes,
    isHolidayOrShabbat = isHolidayOrShabbat,
    km = km,
    engineCc = engineCc,             // ←
    callouts = callouts,
    stolenFound = stolenFound
)
//
private fun ShiftEntity.toModel() = Shift(
    id = id,
    date = date,
    hourlyRate = hourlyRate,
    workedMinutes = workedMinutes,
    isHolidayOrShabbat = isHolidayOrShabbat,
    km = km,
    engineCc = engineCc,             // ←
    callouts = callouts,
    stolenFound = stolenFound
)



