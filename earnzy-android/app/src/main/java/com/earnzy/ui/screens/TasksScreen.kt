package com.earnzy.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.earnzy.api.ApiClient
import com.earnzy.data.Task
import kotlinx.coroutines.launch

@Composable
fun TasksScreen(
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    var tasks by remember { mutableStateOf<List<Task>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        scope.launch {
            try {
                val response = ApiClient.api.getTasks()
                tasks = response.tasks
                loading = false
            } catch (e: Exception) {
                error = e.message
                loading = false
            }
        }
    }

    if (loading) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else if (error != null) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("Error: $error", color = MaterialTheme.colorScheme.error)
        }
    } else {
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    "Daily Tasks",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            items(tasks) { task ->
                TaskCard(task)
            }
        }
    }
}

@Composable
fun TaskCard(task: Task) {
    val scope = rememberCoroutineScope()
    var completing by remember { mutableStateOf(false) }
    var completed by remember { mutableStateOf(task.completedAt != null) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Task Image
            if (task.imageUrl != null) {
                AsyncImage(
                    model = task.imageUrl,
                    contentDescription = task.title,
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
            }

            // Task Info
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    task.title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1
                )
                Text(
                    task.description,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 2
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "+${task.reward} Coins",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        task.category,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }

            // Complete Button
            Button(
                onClick = {
                    scope.launch {
                        completing = true
                        try {
                            ApiClient.api.completeTask(task.id, mapOf("reward" to task.reward))
                            completed = true
                        } catch (e: Exception) {
                            // Show error
                        }
                        completing = false
                    }
                },
                enabled = !completed && !completing,
                modifier = Modifier.height(40.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                if (completed) {
                    Icon(Icons.Default.TaskAlt, contentDescription = "Completed")
                } else if (completing) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp))
                } else {
                    Text("Do")
                }
            }
        }
    }
}
