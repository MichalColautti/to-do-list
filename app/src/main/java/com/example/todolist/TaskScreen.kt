package com.example.todolist

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.*
import android.content.Intent
import android.content.ActivityNotFoundException
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.ui.platform.LocalContext

fun openAttachment(context: Context, attachment: TaskAttachment) {
    val intent = Intent(Intent.ACTION_VIEW).apply {
        setDataAndType(attachment.uri, context.contentResolver.getType(attachment.uri))
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }

    try {
        context.startActivity(intent)
    } catch (_: ActivityNotFoundException) {
        Toast.makeText(context, "Brak aplikacji do otwarcia pliku", Toast.LENGTH_SHORT).show()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskScreen(
    tasks: List<Task>,
    onDelete: (Int) -> Unit,
    onToggleComplete: (Task) -> Unit,
    showCompleted: Boolean,
    onToggleShowCompleted: () -> Unit,
    selectedCategory: String,
    onCategorySelected: (String) -> Unit,
    onTaskClick: (Task) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    var taskAttachments by remember { mutableStateOf<Task?>(null) }
    val context = LocalContext.current

    val dateFormat = remember { SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()) }
    val availableCategories = listOf("Wszystkie") + tasks.map { it.category }.distinct().filter { it.isNotBlank() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Moje Zadania") },
            )
        }
    ) { paddingValues ->
        val filteredTasks = tasks
            .filter { showCompleted || !it.isCompleted }
            .filter { selectedCategory == "Wszystkie" || it.category == selectedCategory }

        if (filteredTasks.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text("Brak zadań do wyświetlenia", style = MaterialTheme.typography.bodyLarge)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                items(filteredTasks) { task ->
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .clickable { onTaskClick(task) }
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = task.title,
                                    style = MaterialTheme.typography.titleLarge,
                                    modifier = Modifier.weight(1f)
                                )
                                if (task.attachments.isNotEmpty()) {
                                    IconButton(
                                        onClick = { taskAttachments = task },
                                        modifier = Modifier.padding(start = 8.dp)
                                    ) {
                                        Icon(
                                            painter = painterResource(id = R.drawable.attachment),
                                            contentDescription = "Załączniki"
                                        )
                                    }
                                }
                                IconButton(onClick = { onDelete(task.id) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Usuń")
                                }
                            }

                            if (task.description.isNotBlank()) {
                                Text(task.description, style = MaterialTheme.typography.bodyMedium)
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Text("Utworzone: ${dateFormat.format(task.creationTime)}", style = MaterialTheme.typography.labelMedium)
                            Text("Termin: ${dateFormat.format(task.dueTime)}", style = MaterialTheme.typography.labelMedium)
                            task.category.takeIf { it.isNotEmpty() }?.let { category ->
                                Text(
                                    text = "Kategoria: $category",
                                    style = MaterialTheme.typography.labelMedium
                                )
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(top = 8.dp)
                            ) {
                                Checkbox(
                                    checked = task.isCompleted,
                                    onCheckedChange = { onToggleComplete(task) }
                                )
                                Text("Zakończone")
                            }
                        }
                    }
                }
            }
        }
    }

    if (taskAttachments != null) {
        val task = taskAttachments!!
        AlertDialog(
            onDismissRequest = { taskAttachments = null },
            title = { Text("Załączniki zadania \"${task.title}\"") },
            text = {
                Column {
                    if (task.attachments.isEmpty()) {
                        Text("Brak załączników")
                    } else {
                        LazyColumn (
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 380.dp)
                        ) {
                            items(task.attachments) { attachment ->
                                Column(modifier = Modifier.padding(vertical = 4.dp)) {
                                    Text("Plik: ${attachment.name}")
                                    Text("URI: ${attachment.uri}", style = MaterialTheme.typography.labelSmall)
                                }
                                TextButton(
                                    modifier = Modifier.fillMaxWidth(),
                                    onClick = {
                                        openAttachment(context,attachment)
                                        taskAttachments = null
                                    }
                                ) {
                                    Text(attachment.name)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = { taskAttachments = null }) {
                    Text("Zamknij")
                }
            }
        )
    }
}
