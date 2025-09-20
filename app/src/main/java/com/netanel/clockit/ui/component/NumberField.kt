package com.netanel.clockit.ui.component

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NumberField(label: String, value: Double, onValueChange: (Double) -> Unit, modifier: Modifier = Modifier) {
    var text by remember(value) { mutableStateOf(if (value == 0.0) "" else value.toString()) }
    OutlinedTextField(
        label = { Text(label) },
        value = text,
        onValueChange = {
            text = it
            onValueChange(it.toDoubleOrNull() ?: 0.0)
        },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        singleLine = true,
        modifier = modifier.fillMaxWidth()
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IntField(label: String, value: Int, onValueChange: (Int) -> Unit, modifier: Modifier = Modifier) {
    var text by remember(value) { mutableStateOf(if (value == 0) "" else value.toString()) }
    OutlinedTextField(
        label = { Text(label) },
        value = text,
        onValueChange = {
            text = it
            onValueChange(it.toIntOrNull() ?: 0)
        },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        singleLine = true,
        modifier = modifier.fillMaxWidth()
    )
}
