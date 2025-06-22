package com.example.todolist

import android.text.Layout
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AddTask(onDismiss: () -> Unit,
            onSave: (
                title: String,
                description: String,
                dueTime: Date,
                isCompleted: Boolean,
                notificationEnabled: Boolean,
                category: String
            ) -> Unit) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var dueTime by remember { mutableStateOf("") }
    var isCompleted by remember { mutableStateOf(false) }
    var notificationEnabled by remember { mutableStateOf(false) }
    var category by remember { mutableStateOf("") }

    val dateFormat = remember { SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Dodaj zadanie") },
        text = {
            Column {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Tytuł") }
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Opis") }
                )
                OutlinedTextField(
                    value = dueTime,
                    onValueChange = { dueTime = it },
                    label = { Text("Czas wykonania (dd.MM.yyyy HH:mm)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("Kategoria") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = notificationEnabled,
                        onCheckedChange = { notificationEnabled = it }
                    )
                    Text("Powiadomienie")
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val dueDate: Date = try {
                        dateFormat.parse(dueTime) ?: Date()
                    } catch (e: Exception) {
                        Date()
                    }
                    onSave(title.trim(), description.trim(), dueDate, isCompleted, notificationEnabled, category.trim())
                },
                enabled = title.isNotBlank() && description.isNotBlank()
                ) {
                Text("Zapisz")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Anuluj")
            }
        }
    )
}
