package com.example.todolist

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
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskScreen(
    tasks: List<Task>,
    onDelete: (Int) -> Unit,
    onToggleComplete: (Task) -> Unit,
    showCompleted: Boolean,
    onToggleShowCompleted: () -> Unit,
    selectedCategory: String,
    onCategorySelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    val dateFormat = remember { SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()) }
    val availableCategories = listOf("Wszystkie") + tasks.map { it.category }.distinct().filter { it.isNotBlank() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Moje Zadania") },
                actions = {
                    Box {
                        IconButton(onClick = { expanded = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "Menu")
                        }
                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text(if (showCompleted) "Ukryj zakończone" else "Pokaż zakończone") },
                                onClick = {
                                    onToggleShowCompleted()
                                    expanded = false
                                }
                            )
                            availableCategories.forEach { category ->
                                DropdownMenuItem(
                                    text = { Text(category) },
                                    onClick = {
                                        onCategorySelected(category)
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                }
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
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = task.title,
                                    style = MaterialTheme.typography.titleLarge,
                                    modifier = Modifier.weight(1f)
                                )
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
                            Text("Kategoria: ${task.category}", style = MaterialTheme.typography.labelMedium)

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
}
