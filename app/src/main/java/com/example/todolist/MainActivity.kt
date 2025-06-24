package com.example.todolist

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.remember
import androidx.compose.runtime.*
import java.util.*
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.core.content.ContextCompat
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.platform.LocalFocusManager

enum class SortOption(val label: String) {
    DATE_ASC("Data rosnąco"),
    DATE_DESC("Data malejąco"),
    TITLE_ASC("Tytuł A-Z"),
    TITLE_DESC("Tytuł Z-A")
}

class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.S)
    @RequiresPermission(Manifest.permission.SCHEDULE_EXACT_ALARM)
    private fun scheduleNotification(context: Context, taskId: Int, taskTitle: String, triggerTime: Long) {
        val alarmManager = ContextCompat.getSystemService(context, AlarmManager::class.java)

        if (alarmManager?.canScheduleExactAlarms() == true) {
            val intent = Intent(context, NotificationReceiver::class.java).apply {
                putExtra("task_id", taskId)
                putExtra("task_title", taskTitle)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }

            val pendingIntent = PendingIntent.getBroadcast(
                context,
                taskId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
        } else {
            Toast.makeText(context, "Brak uprawnień do ustawiania dokładnych alarmów. Włącz je w ustawieniach.", Toast.LENGTH_LONG).show()

            val intent = Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
            context.startActivity(intent)
        }
    }

    private fun cancelNotification(context: Context, taskId: Int) {
        val alarmManager = ContextCompat.getSystemService(context, AlarmManager::class.java)
        val intent = Intent(context, NotificationReceiver::class.java)

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            taskId,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )

        if (pendingIntent != null) {
            alarmManager?.cancel(pendingIntent)
            pendingIntent.cancel()
        }
    }

    @RequiresApi(Build.VERSION_CODES.S)
    @OptIn(ExperimentalMaterial3Api::class)
    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1)
        }

        super.onCreate(savedInstanceState)
        val dbHelper = TaskDatabase(this)

        setContent {
            var tasks by remember { mutableStateOf(emptyList<Task>()) }
            var showDialog by remember { mutableStateOf(false) }
            var hideCompleted by remember {
                mutableStateOf(SettingsManager.getHideCompleted(this))
            }

            var showMainMenu by remember { mutableStateOf(false) }

            var showCategoryMenu by remember { mutableStateOf(false) }
            var selectedCategory by remember {
                mutableStateOf(SettingsManager.getCategory(this))
            }

            var showSortMenu by remember { mutableStateOf(false) }
            var selectedSortOption by remember {
                mutableStateOf(SortOption.valueOf(SettingsManager.getSortOption(this)))
            }

            val scrollState = rememberScrollState()

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

            val allCategories = remember { mutableStateListOf<String>() }

            var selectedTask by remember { mutableStateOf<Task?>(null) }
            var showTaskDetails by remember { mutableStateOf(false) }

            var searchQuery by remember { mutableStateOf("") }

            val focusManager = LocalFocusManager.current

            LaunchedEffect(tasks) {
                val categories = listOf("Wszystkie") + dbHelper.getAllTasks()
                    .map { it.category }
                    .distinct()
                    .filter { it.isNotBlank() }
                allCategories.clear()
                allCategories.addAll(categories)
            }

            fun refreshTasks() {
                tasks = dbHelper.getAllTasks()
                    .filter {
                        (!hideCompleted || !it.isCompleted) &&
                                (selectedCategory == "Wszystkie" || it.category == selectedCategory) &&
                                (searchQuery.isBlank() || it.title.contains(searchQuery, ignoreCase = true) || it.description.contains(searchQuery, ignoreCase = true))
                    }
                    .sortedWith(
                        when (selectedSortOption) {
                            SortOption.DATE_ASC -> compareBy { it.dueTime }
                            SortOption.DATE_DESC -> compareByDescending { it.dueTime }
                            SortOption.TITLE_ASC -> compareBy { it.title.lowercase() }
                            SortOption.TITLE_DESC -> compareByDescending { it.title.lowercase() }
                        }
                    )

                val categories = listOf("Wszystkie") + dbHelper.getAllTasks()
                    .map { it.category }
                    .distinct()
                    .filter { it.isNotBlank() }
                allCategories.clear()
                allCategories.addAll(categories)
            }

            LaunchedEffect(Unit) {
                refreshTasks()

                val taskIdFromNotification = intent?.getIntExtra("task_id", -1) ?: -1
                if (taskIdFromNotification != -1) {
                    val task = dbHelper.getTaskById(taskIdFromNotification)
                    if (task != null) {
                        selectedTask = task
                        showTaskDetails = true
                    }
                }
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
                        val id = dbHelper.insertTask(task)
                        showDialog = false
                        refreshTasks()

                        if (notificationEnabled) {
                            val minutesBefore = SettingsManager.getNotificationMinutes(this)
                            val triggerTime = dueTime.time - minutesBefore * 60 * 1000

                            if (triggerTime > System.currentTimeMillis()) {
                                scheduleNotification(this, id.toInt(), title, triggerTime)
                            }
                        }
                    }

                )
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
                                            SettingsManager.setHideCompleted(context, hideCompleted)
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
                                        text = { Text("Sortuj") },
                                        onClick = {
                                            showSortMenu = true
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
                                    onDismissRequest = { showCategoryMenu = false },
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .heightIn(max = 300.dp)
                                            .verticalScroll(scrollState)
                                    ) {
                                        allCategories.forEach { category ->
                                            DropdownMenuItem(
                                                text = { Text(category) },
                                                onClick = {
                                                    selectedCategory = category
                                                    SettingsManager.setCategory(context, category)
                                                    refreshTasks()
                                                    showCategoryMenu = false
                                                }
                                            )
                                        }
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
                                DropdownMenu(
                                    expanded = showSortMenu,
                                    onDismissRequest = { showSortMenu = false }
                                ) {
                                    SortOption.entries.forEach { option ->
                                        DropdownMenuItem(
                                            text = { Text(option.label) },
                                            onClick = {
                                                selectedSortOption = option
                                                SettingsManager.setSortOption(context, option.name)
                                                refreshTasks()
                                                showSortMenu = false
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
                Column (
                    modifier = Modifier
                        .pointerInput(Unit) {
                            detectTapGestures(onTap = {
                                focusManager.clearFocus()
                            })
                        }
                ){
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = {
                            searchQuery = it
                            refreshTasks()
                        },
                        label = { Text("Szukaj zadań") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp, top = 88.dp, end = 18.dp),
                        singleLine = true
                    )
                    if (showTaskDetails && selectedTask != null) {
                        TaskDetailsScreen(
                            task = selectedTask!!,
                            onDismiss = { showTaskDetails = false },
                            onSave = { updatedTask ->
                                dbHelper.updateTask(updatedTask)
                                refreshTasks()
                                showTaskDetails = false

                                cancelNotification(context, updatedTask.id)

                                if (updatedTask.notificationEnabled) {
                                    val minutesBefore = SettingsManager.getNotificationMinutes(context)
                                    val triggerTime = updatedTask.dueTime.time - minutesBefore * 60 * 1000

                                    if (triggerTime > System.currentTimeMillis()) {
                                        scheduleNotification(
                                            context,
                                            updatedTask.id.toInt(),
                                            updatedTask.title,
                                            triggerTime
                                        )
                                    }
                                }
                            },
                            onCancelNotification = { cancelNotification(context, it) },
                            onRequestSchedule = { id, title, triggerTime ->
                                val minutesBefore = SettingsManager.getNotificationMinutes(context)
                                val adjustedTriggerTime = triggerTime - minutesBefore * 60 * 1000

                                if (adjustedTriggerTime > System.currentTimeMillis()) {
                                    scheduleNotification(context, id, title, adjustedTriggerTime)
                                }
                            }
                        )
                    }
                    else {
                        TaskScreen(
                            tasks = tasks,
                            onDelete = { task ->
                                cancelNotification(context, task.id)
                                dbHelper.deleteTask(task.id)
                                refreshTasks()
                            },
                            onToggleComplete = { task ->
                                dbHelper.updateTaskCompletion(task.id, !task.isCompleted)
                                refreshTasks()
                            },
                            onTaskClick = { task ->
                                selectedTask = task
                                showTaskDetails = true
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
    }
}

