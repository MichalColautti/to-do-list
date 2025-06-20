package com.example.todolist

import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.foundation.layout.*

@Composable
fun AddTask(onDismiss: () -> Unit, onSave: (String, String) -> Unit) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

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
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(title, description) },
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
