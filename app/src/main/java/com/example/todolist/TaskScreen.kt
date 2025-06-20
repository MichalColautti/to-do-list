package com.example.todolist

import ads_mobile_sdk.h6
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.CardDefaults
import java.util.Date

@Composable
fun TaskScreen(tasks: List<Task>) {
    LazyColumn(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp, bottom = 60.dp))
        {
        items(tasks) { task ->
            Card(
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(task.title, style = MaterialTheme.typography.headlineSmall)
                    Text(task.description)
                    Text("Utworzone: ${Date(task.creationTime)}")
                }
            }
        }
    }
}
