package com.netanel.clockit.ui.month

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.consumeWindowInsets // ← NEW
import androidx.compose.foundation.layout.WindowInsets      // ← NEW
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.windowInsetsPadding // ← (optional)
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DatePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.foundation.layout.imePadding // ← NEW

import com.netanel.clockit.model.Shift
import com.netanel.clockit.ui.component.IntField
import com.netanel.clockit.ui.component.NumberField
import com.netanel.clockit.ui.component.RawHoursField
import com.netanel.clockit.utils.DateUtils
import com.netanel.clockit.utils.parseRawHoursToMinutes
import com.netanel.clockit.utils.parseRawKm
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddShiftDialog(
    initialDate: LocalDate,
    defaultHourly: Double,
    onDismiss: () -> Unit,
    onSave: (Shift) -> Unit
) {
    // --- States ---
    var hourly by remember { mutableDoubleStateOf(defaultHourly) }

    var hoursText by remember { mutableStateOf("") }
    var hoursError by remember { mutableStateOf<String?>(null) }

    var engineCcText by remember { mutableStateOf("2000") }
    var engineCcError by remember { mutableStateOf<String?>(null) }

    var kmText by remember { mutableStateOf("") }
    var kmError by remember { mutableStateOf<String?>(null) }

    var callouts by remember { mutableIntStateOf(0) }
    var stolen by remember { mutableIntStateOf(0) }
    var isHoliday by remember { mutableStateOf(false) }

    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = DateUtils.localDateToUtcMillis(initialDate)
    )

    val configuration = LocalConfiguration.current
    val maxHeight = (configuration.screenHeightDp * 0.9f).dp
    val scroll = rememberScrollState()

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .heightIn(max = maxHeight)
                .navigationBarsPadding()
                .imePadding()
        ) {
            Column(
                modifier = Modifier
                    .consumeWindowInsets(WindowInsets.ime)
                    .verticalScroll(scroll)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("הוספת משמרת", style = MaterialTheme.typography.titleLarge)

                DatePicker(
                    state = datePickerState,
                    modifier = Modifier.fillMaxWidth()
                )

                NumberField(
                    label = "שכר לשעה (₪)",
                    value = hourly,
                    onValueChange = { hourly = it }
                )

                RawHoursField(
                    label = "שעות עבודה (0815 / 815)",
                    text = hoursText,
                    onTextChange = {
                        hoursText = it
                        hoursError = null
                    }
                )
                if (hoursError != null) {
                    Text(
                        text = hoursError!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                OutlinedTextField(
                    label = { Text("נפח מנוע (סמ\"ק)") },
                    value = engineCcText,
                    onValueChange = {
                        engineCcText = it
                        engineCcError = null
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                if (engineCcError != null) {
                    Text(
                        text = engineCcError!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                OutlinedTextField(
                    label = { Text("ק\"מ נסיעה (למשל 12.5)") },
                    value = kmText,
                    onValueChange = {
                        kmText = it
                        kmError = null
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                if (kmError != null) {
                    Text(
                        text = kmError!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                IntField(
                    label = "מס' הקפצות",
                    value = callouts,
                    onValueChange = { callouts = it }
                )

                IntField(
                    label = "מס' מציאות רכב גנוב",
                    value = stolen,
                    onValueChange = { stolen = it }
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("שבת/חג")
                    Switch(checked = isHoliday, onCheckedChange = { isHoliday = it })
                }

                Spacer(Modifier.weight(1f)) // ← דוחף את הכפתורים לתחתית גם כשהמקלדת פתוחה

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TextButton(onClick = onDismiss) { Text("ביטול") }
                    OutlinedButton(onClick = {
                        val engineCc = engineCcText.filter { it.isDigit() }.let { digits ->
                            when {
                                digits.isEmpty() -> 2000
                                else -> digits.toIntOrNull()
                            }
                        }
                        if (engineCc == null) {
                            engineCcError = "הזן נפח מנוע בספרות (למשל 1600 / 2000)"
                            return@OutlinedButton
                        }

                        val workedMinutes = parseRawHoursToMinutes(hoursText)
                        if (workedMinutes == null) {
                            hoursError = "פורמט שעות לא תקין. הזן 0815 או 815 (HHMM)."
                            return@OutlinedButton
                        }

                        val kmVal = parseRawKm(kmText)
                        if (kmVal == null) {
                            kmError = "ק\"מ לא תקין (אפשר נקודה או פסיק)"
                            return@OutlinedButton
                        }

                        val date = datePickerState.selectedDateMillis?.let {
                            DateUtils.utcMillisToLocalDate(it)
                        } ?: initialDate

                        onSave(
                            Shift(
                                date = date,
                                hourlyRate = hourly,
                                workedMinutes = workedMinutes,
                                isHolidayOrShabbat = isHoliday,
                                km = kmVal,
                                engineCc = engineCc,
                                callouts = callouts,
                                stolenFound = stolen
                            )
                        )
                    }) {
                        Text("שמור")
                    }
                }
            }
        }
    }
}
