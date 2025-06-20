package com.example.todolist

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.remember
import androidx.compose.runtime.*

class MainActivity : ComponentActivity() {
    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val dbHelper = TaskDatabase(this)

        setContent {
            var tasks by remember { mutableStateOf(dbHelper.getAllTasks()) }
            var showDialog by remember { mutableStateOf(false) }

            if (showDialog) {
                AddTask(
                    onDismiss = { showDialog = false },
                    onSave = { title, desc ->
                        val task = Task(
                            title = title,
                            description = desc,
                            creationTime = System.currentTimeMillis(),
                            dueTime = System.currentTimeMillis() + 86400000
                        )
                        dbHelper.insertTask(task)
                        tasks = dbHelper.getAllTasks()
                        showDialog = false
                    }
                )
            }

            Scaffold(
                floatingActionButton = {
                    FloatingActionButton(onClick = { showDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Add")
                    }
                }
            ) {
                TaskScreen(tasks)
            }
        }
    }
}