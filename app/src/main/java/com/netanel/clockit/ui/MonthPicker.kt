package com.netanel.clockit.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.*

@Composable
fun MonthPicker(
    month: YearMonth,
    onPrev: () -> Unit,
    onNext: () -> Unit,
) {
    val title = "${month.month.getDisplayName(TextStyle.FULL, Locale("he","IL"))} ${month.year}"

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // שורת הכותרת + כפתורי החודשים
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedButton(onClick = onPrev) { Text("חודש קודם") }

            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary
            )

            OutlinedButton(onClick = onNext) { Text("חודש הבא") }
        }

        Spacer(Modifier.height(4.dp))

        // הסבר למחזור: תמיד 23 -> 22
        val start = month.minusMonths(1).atDay(23)
        val end = month.atDay(22)
        val fmt = java.time.format.DateTimeFormatter.ofPattern("dd LLLL", Locale("he","IL"))

        Text(
            text = "מחזור: ${start.format(fmt)} → ${end.format(fmt)}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(8.dp))
    }
}
