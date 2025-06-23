package com.example.todolist

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import android.content.Intent
import android.provider.OpenableColumns
import androidx.compose.foundation.lazy.LazyColumn
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun TaskDetailsScreen(
    task: Task,
    onDismiss: () -> Unit,
    onSave: (Task) -> Unit
) {
    var title by remember { mutableStateOf(task.title) }
    var description by remember { mutableStateOf(task.description) }
    var dueTime by remember { mutableStateOf(SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()).format(task.dueTime)) }
    var isCompleted by remember { mutableStateOf(task.isCompleted) }
    var notificationEnabled by remember { mutableStateOf(task.notificationEnabled) }
    var category by remember { mutableStateOf(task.category) }
    val attachments = remember { mutableStateListOf<TaskAttachment>().apply { addAll(task.attachments) } }

    val dateFormat = remember { SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()) }
    val context = LocalContext.current

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri: Uri? ->
            uri?.let {
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
                        attachments.add(TaskAttachment(uri = it, name = name))
                    }
                } catch (e: SecurityException) {
                    e.printStackTrace()
                }
            }
        }
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Szczegóły zadania") },
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = isCompleted,
                        onCheckedChange = { isCompleted = it }
                    )
                    Text("Zakończone")
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text("Załączniki (${attachments.size})")
                LazyColumn (
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 200.dp)
                ) {
                    items(attachments.size) { index ->
                        val attachment = attachments[index]
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Text(attachment.name, modifier = Modifier.weight(1f))
                            IconButton(onClick = {
                                attachments.remove(attachment)
                            }) {
                                Icon(Icons.Default.Delete, contentDescription = "Usuń załącznik")
                            }
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
                    val parsedDueDate = try {
                        dateFormat.parse(dueTime) ?: task.dueTime
                    } catch (_: Exception) {
                        task.dueTime
                    }

                    onSave(
                        task.copy(
                            title = title.trim(),
                            description = description.trim(),
                            dueTime = parsedDueDate,
                            isCompleted = isCompleted,
                            notificationEnabled = notificationEnabled,
                            category = category.trim(),
                            attachments = attachments
                        )
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
