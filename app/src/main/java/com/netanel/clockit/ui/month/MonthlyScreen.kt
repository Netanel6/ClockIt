package com.netanel.clockit.ui.month

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material3.Divider
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.netanel.clockit.model.Shift
import com.netanel.clockit.ui.MonthPicker
import java.text.NumberFormat
import java.time.LocalDate
import java.util.Locale

@Composable
fun MonthlyScreen(
    vm: MonthlyViewModel,
    onAddShift: (LocalDate) -> Unit
) {
    val ui by vm.uiState.collectAsState()
    val nf = remember { NumberFormat.getCurrencyInstance(Locale("he","IL")) }

    // תווית מחזור: "מחזור 23 {חודש קודם} – 22 {חודש נוכחי} {שנה}"
    Box(Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // ניווט בין חודשים
            MonthPicker(month = ui.month, onPrev = vm::prevMonth, onNext = vm::nextMonth)

            // סיכום למחזור
            SummaryCard(
                base = nf.format(ui.summary.totalBase),
                ot1 = nf.format(ui.summary.totalOt1),
                ot2 = nf.format(ui.summary.totalOt2),
                travel = nf.format(ui.summary.totalTravel),
                callouts = nf.format(ui.summary.totalCallouts),
                stolen = nf.format(ui.summary.totalStolen),
                total = nf.format(ui.summary.grandTotal)
            )

            // רשימת משמרות / מצב ריק
            if (ui.shifts.isEmpty()) {
                EmptyState(onAdd = { onAddShift(LocalDate.now()) })
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 96.dp) // מקום ל-FAB-ים
                ) {
                    items(ui.shifts) { s ->
                        ShiftRowCompact(
                            s = s,
                            onDelete = { id -> vm.deleteShift(id) }
                        )
                    }
                }
            }
        }

        // FAB-ים מוערמים
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.End
        ) {
            // הקפצה בלבד (היום)
            ExtendedFloatingActionButton(
                onClick = { vm.quickAddCalloutForDate(LocalDate.now()) },
                content = {
                     Text("הקפצה היום")
                }
            )
            // הוספת משמרת (דיאלוג)
            FloatingActionButton(onClick = { onAddShift(LocalDate.now()) }) {
                Icon(Icons.Filled.Add, contentDescription = "הוסף משמרת")
            }
        }
    }
}

@Composable
private fun SummaryCard(
    base: String,
    ot1: String,
    ot2: String,
    travel: String,
    callouts: String,
    stolen: String,
    total: String
) {
    ElevatedCard {
        Column(
            Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text("סיכום חודשי", style = MaterialTheme.typography.titleMedium)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                SummaryChip("בסיס", base, Modifier.weight(1f))
                SummaryChip("נוספות 1", ot1, Modifier.weight(1f))
                SummaryChip("נוספות 2", ot2, Modifier.weight(1f))
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                SummaryChip("נסיעות", travel, Modifier.weight(1f))
                SummaryChip("הקפצות", callouts, Modifier.weight(1f))
                SummaryChip("גנוב", stolen, Modifier.weight(1f))
            }
            Divider()
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("סה\"כ לתשלום", style = MaterialTheme.typography.titleLarge)
                Text(total, style = MaterialTheme.typography.titleLarge)
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
            Text("לחץ על הפלוס כדי להזין יום עבודה.", style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(8.dp))
            OutlinedButton(onClick = onAdd) { Text("הוסף משמרת") }
        }
    }
}

@Composable
private fun ShiftRowCompact(
    s: Shift,
    onDelete: (Long) -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    ElevatedCard {
        Column(
            Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // כותרת: תאריך + שבת/חג + מחיקה
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

            // פרטים
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
                Text("₪/שעה: ${"%.2f".format(Locale("he","IL"), s.hourlyRate)}")
                Text("הקפצות: ${s.callouts} • גנוב: ${s.stolenFound}", style = MaterialTheme.typography.labelMedium)
            }
        }
    }

    // דיאלוג אישור מחיקה
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
