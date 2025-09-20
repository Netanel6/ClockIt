package com.netanel.clockit.ui.month

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.netanel.clockit.model.Shift
import java.time.*

@Composable
fun MonthGrid(
    month: YearMonth,
    shiftsByDate: Map<LocalDate, List<Shift>>,
    onDayClick: (LocalDate) -> Unit
) {
    val firstDay = month.atDay(1)
    val daysInMonth = month.lengthOfMonth()
    val firstDowIndex = ((firstDay.dayOfWeek.value % 7)) // Sunday=0 ... Saturday=6

    val totalCells = firstDowIndex + daysInMonth
    val rows = (totalCells + 6) / 7 // עד 6 שורות

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        // כותרות ימים
        Row(Modifier.fillMaxWidth()) {
            listOf("א", "ב", "ג", "ד", "ה", "ו", "ש").forEach { d ->
                Text(
                    d,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }

        // שורות
        var dayCounter = 1
        repeat(rows) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                repeat(7) { col ->
                    val cellIndex = it * 7 + col
                    if (cellIndex < firstDowIndex || dayCounter > daysInMonth) {
                        Spacer(Modifier.weight(1f))
                    } else {
                        val date = month.atDay(dayCounter)
                        val shifts = shiftsByDate[date].orEmpty()
                        DayCell(
                            date = date,
                            hasShifts = shifts.isNotEmpty(),
                            onClick = { onDayClick(date) },
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                        )
                        dayCounter++
                    }
                }
            }
        }
    }
}

@Composable
private fun DayCell(
    date: LocalDate,
    hasShifts: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedButton(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = modifier
    ) {
        Box(Modifier.fillMaxSize()) {
            Text(
                text = date.dayOfMonth.toString(),
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(6.dp),
                style = MaterialTheme.typography.labelLarge
            )
            if (hasShifts) {
                AssistChip(
                    onClick = onClick,
                    label = { Text("✔") },
                    modifier = Modifier.align(Alignment.BottomStart)
                )
            }
        }
    }
}
