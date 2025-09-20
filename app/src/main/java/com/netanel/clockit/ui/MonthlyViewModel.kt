// MonthlyViewModel.kt
package com.netanel.clockit.ui.month

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.netanel.clockit.data.ShiftRepository
import com.netanel.clockit.domain.WageCalculator
import com.netanel.clockit.model.CalcProfile
import com.netanel.clockit.model.MonthlySummary
import com.netanel.clockit.model.Shift
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth

class MonthlyViewModel(
    private val repo: ShiftRepository,
    private val calculator: WageCalculator = WageCalculator(),
    initialMonth: YearMonth = YearMonth.now()
) : ViewModel() {

    private val _profile = MutableStateFlow(CalcProfile())
    val profile: StateFlow<CalcProfile> = _profile.asStateFlow()

    private val _month = MutableStateFlow(initialMonth) // מייצג את חודש ה"סיום" (22 בו)
    val month: StateFlow<YearMonth> = _month.asStateFlow()

    data class UiState(
        val month: YearMonth,
        val shifts: List<Shift>,
        val summary: MonthlySummary
    )

    val uiState: StateFlow<UiState> =
        _month.flatMapLatest { ym ->
            val (from, to) = cycleBounds(ym) // ← 23-22
            repo.observeShifts(from, to).map { shifts ->
                UiState(
                    month = ym,
                    shifts = shifts,
                    summary = calcSummary(shifts, _profile.value, ym)
                )
            }
        }.stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            UiState(
                month = _month.value,
                shifts = emptyList(),
                summary = MonthlySummary(_month.value, 0.0,0.0,0.0,0.0,0.0,0.0,0.0)
            )
        )

    fun nextMonth() { _month.value = _month.value.plusMonths(1) }
    fun prevMonth() { _month.value = _month.value.minusMonths(1) }
    fun goTo(ym: YearMonth) { _month.value = ym }

    fun addShift(shift: Shift) = viewModelScope.launch { repo.addShift(shift) }
    fun updateShift(shift: Shift) = viewModelScope.launch { repo.updateShift(shift) }
    fun deleteShift(id: Long) = viewModelScope.launch { repo.deleteShift(id) }

    private fun calcSummary(shifts: List<Shift>, profile: CalcProfile, ym: YearMonth): MonthlySummary {
        var base = 0.0; var ot1 = 0.0; var ot2 = 0.0
        var travel = 0.0; var callouts = 0.0; var caught = 0.0

        for (s in shifts) {
            val r = calculator.calculateFromMinutes(
                workedMinutes = s.workedMinutes,
                hourlyRate = s.hourlyRate,
                isHolidayOrShabbat = s.isHolidayOrShabbat,
                km = s.km,
                engineCc = s.engineCc,
                callouts = s.callouts,
                caughtFound = s.caughtFound,
                profile = profile
            )
            base += r.basePay; ot1 += r.overtime1Pay; ot2 += r.overtime2Pay
            travel += r.travelPay; callouts += r.calloutsPay; caught += r.caughtBonusPay
        }
        val total = base + ot1 + ot2 + travel + callouts + caught
        return MonthlySummary(ym, base, ot1, ot2, travel, callouts, caught, total)
    }

    /** מחזור שמסתיים ב־22 של ym, ומתחיל ב־23 של החודש הקודם */
    private fun cycleBounds(ym: YearMonth): Pair<LocalDate, LocalDate> {
        val endInclusive = ym.atDay(22)
        val startInclusive = ym.minusMonths(1).atDay(23)
        return startInclusive to endInclusive
    }

    fun quickAddCalloutForDate(date: LocalDate) = viewModelScope.launch {
        val existing = repo.getShiftByDate(date)
        if (existing != null) {
            // עדכון קיימת: +1 הקפצה
            repo.updateShift(existing.copy(callouts = existing.callouts + 1))
        } else {
            // יצירת חדשה עם דיפולטים
            val p = profile.value
            val newShift = com.netanel.clockit.model.Shift(
                date = date,
                hourlyRate = p.hourlyRate,
                workedMinutes = 0,
                isHolidayOrShabbat = false,
                km = 0.0,
                engineCc = 2000,
                callouts = 1,
                caughtFound = 0
            )
            repo.addShift(newShift)
        }
    }
}
