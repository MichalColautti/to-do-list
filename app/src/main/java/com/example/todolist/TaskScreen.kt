package com.example.todolist

import ads_mobile_sdk.h6
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun TaskScreen(
    tasks: List<Task>,
    onDelete: (Int) -> Unit,
    onToggleComplete: (Task) -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp, top = 24.dp, bottom = 80.dp)
    ) {
        items(tasks) { task ->
            Card(
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(task.title, style = MaterialTheme.typography.headlineSmall)
                        IconButton(onClick = { onDelete(task.id) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Usuń")
                        }
                    }

                    if (task.description.isNotBlank()) {
                        Text(task.description)
                    }

                    Text("Utworzone: ${dateFormat.format(task.creationTime)}")
                    Text("Termin: ${dateFormat.format(task.dueTime)}")
                    Text("Kategoria: ${task.category}")

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
                        Checkbox(
                            checked = task.isCompleted,
                            onCheckedChange = {
                                onToggleComplete(task)
                            }
                        )
                        Text("Zakończone")
                    }
                }
            }
        }
    }
}
