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

