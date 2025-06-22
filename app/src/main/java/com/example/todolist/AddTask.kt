package com.example.todolist

import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AddTask(
    onDismiss: () -> Unit,
    onSave: (
        title: String,
        description: String,
        dueTime: Date,
        isCompleted: Boolean,
        notificationEnabled: Boolean,
        category: String,
        attachments: List<TaskAttachment>
    ) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var dueTime by remember { mutableStateOf("") }
    var isCompleted by remember { mutableStateOf(false) }
    var notificationEnabled by remember { mutableStateOf(false) }
    var category by remember { mutableStateOf("") }
    var attachments by remember { mutableStateOf(listOf<TaskAttachment>()) }

    val dateFormat = remember { SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()) }

    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri: Uri? ->
            uri?.let {
                Log.d("AddTask", "Dodano załącznik URI = $uri")

                try {
                    context.contentResolver.takePersistableUriPermission(
                        it,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )

                    val name = context.contentResolver.query(it, null, null, null, null)?.use { cursor ->
                        val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                        if (cursor.moveToFirst() && nameIndex >= 0) {
                            cursor.getString(nameIndex)
                        } else {
                            it.lastPathSegment ?: "Załącznik"
                        }
                    } ?: it.lastPathSegment ?: "Załącznik"

                    if (attachments.none { att -> att.uri == it }) {
                        attachments = attachments + TaskAttachment(uri = it, name = name)
                    }
                } catch (e: SecurityException) {
                    e.printStackTrace()
                }
            }
        }
    )

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

                Spacer(modifier = Modifier.height(8.dp))

                Text("Załączniki (${attachments.size})")
                Log.d("UI", "attachments w UI = ${attachments.size}")

                attachments.forEach { attachment ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Text(attachment.name, modifier = Modifier.weight(1f))
                        IconButton(onClick = {
                            attachments = attachments - attachment
                        }) {
                            Icon(Icons.Default.Delete, contentDescription = "Usuń załącznik")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(onClick = {
                    launcher.launch(arrayOf("*/*"))
                }) {
                    Text("Dodaj załącznik")
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
                    onSave(
                        title.trim(),
                        description.trim(),
                        dueDate,
                        isCompleted,
                        notificationEnabled,
                        category.trim(),
                        attachments
                    )
                },
                enabled = title.isNotBlank() && description.isNotBlank() && dueTime.isNotBlank()
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
