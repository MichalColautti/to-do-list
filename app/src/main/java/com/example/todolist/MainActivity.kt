package com.example.todolist

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.remember
import androidx.compose.runtime.*
import java.util.Date

class MainActivity : ComponentActivity() {
    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val dbHelper = TaskDatabase(this)

        setContent {
            var tasks by remember { mutableStateOf(dbHelper.getAllTasks()) }
            var showDialog by remember { mutableStateOf(false) }
            var hideCompleted by remember { mutableStateOf(false) }
            var selectedCategory by remember { mutableStateOf("Wszystkie") }

            fun refreshTasks() {
                tasks = dbHelper.getAllTasks().filter {
                    (!hideCompleted || !it.isCompleted) &&
                            (selectedCategory == "Wszystkie" || it.category == selectedCategory)
                }
            }

            if (showDialog) {
                AddTask(
                    onDismiss = { showDialog = false },
                    onSave = { title, desc, dueTime, isCompleted, notificationEnabled, category ->
                        val task = Task(
                            title = title,
                            description = desc,
                            creationTime = Date(),
                            dueTime = dueTime,
                            isCompleted = isCompleted,
                            notificationEnabled = notificationEnabled,
                            category = category
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
                floatingActionButton = {
                    FloatingActionButton(onClick = { showDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Add")
                    }
                }
            ) {
                Column {
                    FilterBar(
                        hideCompleted = hideCompleted,
                        onHideCompletedChange = {
                            hideCompleted = it
                            refreshTasks()
                        },
                        selectedCategory = selectedCategory,
                        categories = allCategories,
                        onCategorySelected = {
                            selectedCategory = it
                            refreshTasks()
                        }
                    )

                    TaskScreen(
                        tasks = tasks,
                        onDelete = {
                            dbHelper.deleteTask(it)
                            refreshTasks()
                        },
                        onToggleComplete = {
                            dbHelper.updateTaskCompletion(it.id ?: return@TaskScreen, !it.isCompleted)
                            refreshTasks()
                        }
                    )
                }
            }
        }
    }
}