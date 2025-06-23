package com.example.todolist

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.remember
import androidx.compose.runtime.*
import java.util.*
import androidx.core.content.ContextCompat
import androidx.compose.foundation.layout.Box

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val dbHelper = TaskDatabase(this)

        setContent {
            var tasks by remember { mutableStateOf(emptyList<Task>()) }
            var showDialog by remember { mutableStateOf(false) }
            var hideCompleted by remember { mutableStateOf(false) }
            var selectedCategory by remember { mutableStateOf("Wszystkie") }

            var showMainMenu by remember { mutableStateOf(false) }
            var showCategoryMenu by remember { mutableStateOf(false) }

            fun refreshTasks() {
                tasks = dbHelper.getAllTasks().filter {
                    (!hideCompleted || !it.isCompleted) &&
                            (selectedCategory == "Wszystkie" || it.category == selectedCategory)
                }
            }

            LaunchedEffect(Unit) {
                refreshTasks()
            }

            if (showDialog) {
                AddTask(
                    onDismiss = { showDialog = false },
                    onSave = { title, desc, dueTime, isCompleted, notificationEnabled, category, attachments ->
                        val task = Task(
                            title = title,
                            description = desc,
                            creationTime = Date(),
                            dueTime = dueTime,
                            isCompleted = isCompleted,
                            notificationEnabled = notificationEnabled,
                            category = category,
                            attachments = attachments
                        )
                        dbHelper.insertTask(task)
                        showDialog = false
                        refreshTasks()
                    }
                )
            }

            val context = this
            val notificationOptions = listOf(
                "1 minuta przed" to 1,
                "5 minut przed" to 5,
                "10 minut przed" to 10,
                "30 minut przed" to 30,
                "1 godzina przed" to 60,
                "2 godziny przed" to 120,
                "4 godziny przed" to 240,
                "10 godzin przed" to 600,
                "1 dzień przed" to 1440,
                "2 dni przed" to 2880
            )


            val currentNotificationMinutes = remember {
                SettingsManager.getNotificationMinutes(context)
            }
            var selectedNotificationLabel by remember {
                mutableStateOf(
                    notificationOptions.firstOrNull { it.second == currentNotificationMinutes }?.first ?: "10 minut przed"
                )
            }
            var showNotificationMenu by remember { mutableStateOf(false) }

            val allCategories = remember(tasks) {
                listOf("Wszystkie") + tasks.map { it.category }.distinct().filter { it.isNotBlank() }
            }

            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text("Lista toDo") },
                        actions = {
                            Box {
                                IconButton(onClick = { showMainMenu = !showMainMenu }) {
                                    Icon(Icons.Default.MoreVert, contentDescription = "Menu")
                                }

                                DropdownMenu(
                                    expanded = showMainMenu,
                                    onDismissRequest = { showMainMenu = false }
                                ) {
                                    DropdownMenuItem(
                                        text = { Text(if (hideCompleted) "Pokaż zakończone" else "Ukryj zakończone") },
                                        onClick = {
                                            hideCompleted = !hideCompleted
                                            refreshTasks()
                                            showMainMenu = false
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Wybierz kategorię") },
                                        onClick = {
                                            showCategoryMenu = true
                                            showMainMenu = false
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Czas powiadomień") },
                                        onClick = {
                                            showNotificationMenu = true
                                            showMainMenu = false
                                        }
                                    )
                                }

                                DropdownMenu(
                                    expanded = showCategoryMenu,
                                    onDismissRequest = { showCategoryMenu = false }
                                ) {
                                    allCategories.forEach { category ->
                                        DropdownMenuItem(
                                            text = { Text(category) },
                                            onClick = {
                                                selectedCategory = category
                                                refreshTasks()
                                                showCategoryMenu = false
                                            }
                                        )
                                    }
                                }
                                DropdownMenu(
                                    expanded = showNotificationMenu,
                                    onDismissRequest = { showNotificationMenu = false }
                                ) {
                                    notificationOptions.forEach { (label, minutes) ->
                                        DropdownMenuItem(
                                            text = { Text(label) },
                                            onClick = {
                                                selectedNotificationLabel = label
                                                SettingsManager.setNotificationMinutes(context, minutes)
                                                showNotificationMenu = false
                                                Toast.makeText(context, "Ustawiono: $label", Toast.LENGTH_SHORT).show()
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    )
                },
                floatingActionButton = {
                    FloatingActionButton(onClick = { showDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Dodaj")
                    }
                }
            ) {
                TaskScreen(
                    tasks = tasks,
                    onDelete = { taskId ->
                        dbHelper.deleteTask(taskId)
                        refreshTasks()
                    },
                    onToggleComplete = { task ->
                        dbHelper.updateTaskCompletion(task.id, !task.isCompleted)
                        refreshTasks()
                    },
                    showCompleted = !hideCompleted,
                    onToggleShowCompleted = {
                        hideCompleted = !hideCompleted
                        refreshTasks()
                    },
                    selectedCategory = selectedCategory,
                    onCategorySelected = { category ->
                        if (category != null) {
                            selectedCategory = category
                        }
                        refreshTasks()
                    },
                )
            }
        }
    }
}

