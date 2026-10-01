package com.example.donaka100.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.donaka100.ui.theme.*
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun DonakaDropdownField(
    label: String,
    options: List<String>,
    selected: String,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
    error: String? = null
) {
    var ouvert by remember { mutableStateOf(false) }

    Box(modifier) {
        DonakaTextField(
            value = selected,
            onValueChange = {},
            label = label,
            error = error,
            trailingIcon = Icons.Default.ArrowDropDown,
            enabled = false,
            singleLine = true
        )
        Box(Modifier.matchParentSize().clickable { ouvert = true })

        DropdownMenu(expanded = ouvert, onDismissRequest = { ouvert = false }) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = { onSelect(option); ouvert = false }
                )
            }
        }
    }
}

private val formatDate: DateTimeFormatter =
    DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale.FRENCH)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DonakaDateField(
    label: String,
    date: LocalDate,
    onDateChange: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    var ouvert by remember { mutableStateOf(false) }

    Box(modifier) {
        DonakaTextField(
            value = date.format(formatDate),
            onValueChange = {},
            label = label,
            leadingIcon = Icons.Default.CalendarToday,
            enabled = false
        )
        Box(Modifier.matchParentSize().clickable { ouvert = true })
    }

    if (ouvert) {
        val etat = rememberDatePickerState(
            initialSelectedDateMillis = date.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { ouvert = false },
            confirmButton = {
                TextButton(onClick = {
                    etat.selectedDateMillis?.let {
                        onDateChange(Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate())
                    }
                    ouvert = false
                }) { Text("OK", color = Primary) }
            },
            dismissButton = {
                TextButton(onClick = { ouvert = false }) { Text("Annuler", color = TexteGris) }
            }
        ) {
            DatePicker(state = etat)
        }
    }
}