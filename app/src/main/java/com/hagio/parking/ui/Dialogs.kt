package com.hagio.parking.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.hagio.parking.data.Car

/** 자주 쓰는 층 — 눌러서 바로 선택 */
private val QUICK_FLOORS = listOf("B6", "B5", "B4", "B3", "B2", "B1", "1", "2", "3", "4", "5")

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ParkDialog(
    car: Car,
    onDismiss: () -> Unit,
    onSave: (floor: String, zone: String) -> Unit,
) {
    var floor by remember { mutableStateOf(car.floor) }
    var zone by remember { mutableStateOf(car.zone) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("${car.name} 주차 위치 저장") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("층")
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    QUICK_FLOORS.forEach { f ->
                        FilterChip(
                            selected = floor == f,
                            onClick = { floor = f },
                            label = { Text(if (f.startsWith("B")) f else "${f}층") },
                        )
                    }
                }
                OutlinedTextField(
                    value = floor,
                    onValueChange = { floor = it },
                    label = { Text("층 (직접 입력, 예: B2, 지하3)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Characters,
                        imeAction = ImeAction.Next,
                    ),
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = zone,
                    onValueChange = { zone = it },
                    label = { Text("구역 (예: A-13, 파랑 7)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Characters,
                        imeAction = ImeAction.Done,
                    ),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onSave(floor, zone) },
                enabled = floor.isNotBlank() || zone.isNotBlank(),
            ) { Text("저장") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("취소") } },
    )
}

@Composable
fun NameDialog(
    title: String,
    initial: String = "",
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    var name by remember { mutableStateOf(initial) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("차량 이름 (예: 쏘렌토, 12가3456)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(name) }, enabled = name.isNotBlank()) { Text("확인") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("취소") } },
    )
}

@Composable
fun ConfirmDialog(
    title: String,
    message: String,
    confirmText: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(message) },
        confirmButton = { TextButton(onClick = onConfirm) { Text(confirmText) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("취소") } },
    )
}
