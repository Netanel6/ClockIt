package com.netanel.clockit.ui.month

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.netanel.clockit.model.MonthlySummary
import com.netanel.clockit.model.Shift
import com.netanel.clockit.ui.MonthPicker
import java.text.NumberFormat
import java.time.LocalDate
import java.util.Locale

private enum class MonthlyPage { Summary, Shifts }

@Composable
fun MonthlyScreen(
    vm: MonthlyViewModel,
    onAddShift: (LocalDate) -> Unit,
    onEditShift: (Shift) -> Unit
) {
    val ui by vm.uiState.collectAsState()
    val locale = remember { Locale("he", "IL") }
    val currencyFormat = remember(locale) { NumberFormat.getCurrencyInstance(locale) }
    val percentFormat = remember(locale) {
        NumberFormat.getPercentInstance(locale).apply { maximumFractionDigits = 0 }
    }
    var page by remember { mutableStateOf(MonthlyPage.Summary) }

    val addShiftToday = { onAddShift(LocalDate.now()) }
    val quickCalloutToday = { vm.quickAddCalloutForDate(LocalDate.now()) }

    Box(Modifier.fillMaxSize()) {
        Crossfade(targetState = page, modifier = Modifier.fillMaxSize(), label = "monthly_page") { current ->
            when (current) {
                MonthlyPage.Summary -> MonthlySummaryPage(
                    ui = ui,
                    currencyFormat = currencyFormat,
                    percentFormat = percentFormat,
                    onPrevMonth = vm::prevMonth,
                    onNextMonth = vm::nextMonth,
                    onAddShift = addShiftToday,
                    onQuickCallout = { quickCalloutToday() },
                    onViewShifts = { page = MonthlyPage.Shifts }
                )

                MonthlyPage.Shifts -> MonthlyShiftsPage(
                    ui = ui,
                    onPrevMonth = vm::prevMonth,
                    onNextMonth = vm::nextMonth,
                    onAddShift = addShiftToday,
                    onEditShift = onEditShift,
                    onDeleteShift = vm::deleteShift,
                    onBackToSummary = { page = MonthlyPage.Summary }
                )
            }
        }

        if (page == MonthlyPage.Shifts) {
            Column(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.End
            ) {
                ExtendedFloatingActionButton(onClick = { quickCalloutToday() }) {
                    Text("הוסף הקפצה להיום")
                }
                FloatingActionButton(onClick = addShiftToday) {
                    Icon(Icons.Filled.Add, contentDescription = "הוסף משמרת")
                }
            }
        }
    }
}

@Composable
private fun MonthlySummaryPage(
    ui: MonthlyViewModel.UiState,
    currencyFormat: NumberFormat,
    percentFormat: NumberFormat,
    onPrevMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onAddShift: () -> Unit,
    onQuickCallout: () -> Unit,
    onViewShifts: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        MonthPicker(month = ui.month, onPrev = onPrevMonth, onNext = onNextMonth)

        MonthlySummaryCard(
            summary = ui.summary,
            currencyFormat = currencyFormat,
            percentFormat = percentFormat
        )

        if (ui.shifts.isEmpty()) {
            Text(
                "עוד לא נוספו משמרות למחזור זה.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.weight(1f, fill = true))

        FilledTonalButton(
            onClick = onViewShifts,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("צפה במשמרות")
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onAddShift,
                modifier = Modifier.weight(1f)
            ) {
                Text("הוסף משמרת חדשה")
            }

            OutlinedButton(
                onClick = onQuickCallout,
                modifier = Modifier.weight(1f)
            ) {
                Text("הוסף הקפצה להיום")
            }
        }
    }
}

@Composable
private fun MonthlyShiftsPage(
    ui: MonthlyViewModel.UiState,
    onPrevMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onAddShift: () -> Unit,
    onEditShift: (Shift) -> Unit,
    onDeleteShift: (Long) -> Unit,
    onBackToSummary: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        MonthPicker(month = ui.month, onPrev = onPrevMonth, onNext = onNextMonth)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("משמרות", style = MaterialTheme.typography.titleMedium)
            TextButton(onClick = onBackToSummary) {
                Text("חזרה לסיכום")
            }
        }

        if (ui.shifts.isEmpty()) {
            Box(modifier = Modifier.weight(1f, fill = true)) {
                EmptyState(onAdd = onAddShift)
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f, fill = true),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 96.dp)
            ) {
                items(ui.shifts) { s ->
                    ShiftRowCompact(
                        s = s,
                        onDelete = onDeleteShift,
                        onShiftClick = onEditShift
                    )
                }
            }
        }
    }
}

@Composable
private fun MonthlySummaryCard(
    summary: MonthlySummary,
    currencyFormat: NumberFormat,
    percentFormat: NumberFormat
) {
    ElevatedCard {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("סיכום חודשי", style = MaterialTheme.typography.titleMedium)
                Text(
                    currencyFormat.format(summary.grandTotal),
                    style = MaterialTheme.typography.headlineSmall
                )
            }

            PayoutDistributionChart(
                summary = summary,
                currencyFormat = currencyFormat,
                percentFormat = percentFormat
            )

            Divider()

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SummaryChip("בסיס", currencyFormat.format(summary.totalBase), Modifier.weight(1f))
                    SummaryChip("נוספות 1", currencyFormat.format(summary.totalOt1), Modifier.weight(1f))
                    SummaryChip("נוספות 2", currencyFormat.format(summary.totalOt2), Modifier.weight(1f))
                }
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SummaryChip("נסיעות", currencyFormat.format(summary.totalTravel), Modifier.weight(1f))
                    SummaryChip("הקפצות", currencyFormat.format(summary.totalCallouts), Modifier.weight(1f))
                    SummaryChip("נתפס", currencyFormat.format(summary.totalCaught), Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun PayoutDistributionChart(
    summary: MonthlySummary,
    currencyFormat: NumberFormat,
    percentFormat: NumberFormat,
    modifier: Modifier = Modifier
) {
    val entries = listOf(
        Triple("בסיס", summary.totalBase, MaterialTheme.colorScheme.primary),
        Triple("נוספות 1", summary.totalOt1, MaterialTheme.colorScheme.secondary),
        Triple("נוספות 2", summary.totalOt2, MaterialTheme.colorScheme.tertiary),
        Triple("נסיעות", summary.totalTravel, MaterialTheme.colorScheme.primaryContainer),
        Triple("הקפצות", summary.totalCallouts, MaterialTheme.colorScheme.secondaryContainer),
        Triple("נתפס", summary.totalCaught, MaterialTheme.colorScheme.tertiaryContainer)
    )
    val total = entries.sumOf { it.second }.coerceAtLeast(0.0)

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        if (total <= 0.0) {
            Text(
                "אין נתונים להצגה לחודש זה",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            entries.filter { it.second > 0.0 }.forEach { (label, amount, color) ->
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(label, style = MaterialTheme.typography.labelLarge)
                        val percentText = percentFormat.format(amount / total)
                        Text(
                            "${currencyFormat.format(amount)} • $percentText",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(MaterialTheme.shapes.small)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth((amount / total).toFloat())
                                .background(color)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SummaryChip(title: String, value: String, modifier: Modifier = Modifier) {
    Surface(
        tonalElevation = 2.dp,
        shape = MaterialTheme.shapes.medium,
        modifier = modifier
    ) {
        Column(
            Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(title, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
            Text(value, style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun EmptyState(onAdd: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        tonalElevation = 1.dp,
        shape = MaterialTheme.shapes.large
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("אין משמרות במחזור זה", style = MaterialTheme.typography.titleMedium)
            Text("לחץ על הכפתור כדי להזין יום עבודה.", style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(8.dp))
            Button(onClick = onAdd) { Text("הוסף משמרת") }
        }
    }
}

@Composable
private fun ShiftRowCompact(
    s: Shift,
    onDelete: (Long) -> Unit,
    onShiftClick: (Shift) -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    ElevatedCard(
        modifier = Modifier.clickable { onShiftClick(s) }
    ) {
        Column(
            Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("${s.date}", style = MaterialTheme.typography.titleSmall)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (s.isHolidayOrShabbat) {
                        AssistChip(onClick = {}, label = { Text("שבת/חג") })
                    }
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = "מחק משמרת",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            Text(
                "משך: %02d:%02d • ק\"מ: %.2f • סמ\"ק: %d".format(
                    s.workedMinutes / 60,
                    s.workedMinutes % 60,
                    s.km,
                    s.engineCc
                ),
                style = MaterialTheme.typography.bodyMedium
            )

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("₪/שעה: ${"%.2f".format(Locale("he", "IL"), s.hourlyRate)}")
                Text(
                    "הקפצות: ${s.callouts} • נתפס: ${s.caughtFound}",
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            confirmButton = {
                TextButton(onClick = {
                    onDelete(s.id)
                    showDeleteDialog = false
                }) { Text("מחק", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("ביטול") }
            },
            title = { Text("מחיקת משמרת") },
            text = { Text("למחוק את המשמרת בתאריך ${s.date}?") }
        )
    }
}
