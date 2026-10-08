package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.OperationsRepository
import com.example.model.OpsTask
import com.example.model.OpsUser
import com.example.model.TaskStatus
import com.example.ui.components.PriorityBadge
import com.example.ui.components.StatusPill
import com.example.ui.theme.*

@Composable
fun MyTasksScreen(
    currentUser: OpsUser,
    onOpenWorkspace: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val tasks by OperationsRepository.tasks.collectAsState()

    var selectedSection by remember { mutableStateOf(0) } // 0: Pending, 1: Overdue, 2: Completed, 3: All
    val sections = listOf("Pending", "Overdue", "Completed", "All")

    val userTasks = remember(tasks, currentUser) {
        // Find tasks where assigned member matches or current user is head
        val firstName = currentUser.name.split(" ").first()
        tasks.filter { it.assignedMemberName.contains(firstName, ignoreCase = true) }
    }

    val filteredTasks = remember(userTasks, selectedSection) {
        when (selectedSection) {
            0 -> userTasks.filter { it.status == TaskStatus.TO_DO || it.status == TaskStatus.IN_PROGRESS }
            1 -> userTasks.filter { it.status == TaskStatus.OVERDUE || it.isOverdue }
            2 -> userTasks.filter { it.status == TaskStatus.COMPLETED }
            else -> userTasks
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("my_tasks_screen")
    ) {
        // Header
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "My Operational Tasks",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Work items assigned to ${currentUser.name} across all college events",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Section Tabs
        TabRow(
            selectedTabIndex = selectedSection,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = OpsPrimaryIndigo
        ) {
            sections.forEachIndexed { idx, title ->
                Tab(
                    selected = selectedSection == idx,
                    onClick = { selectedSection = idx },
                    text = {
                        Text(
                            text = title,
                            fontWeight = if (selectedSection == idx) FontWeight.Bold else FontWeight.Medium
                        )
                    },
                    modifier = Modifier.testTag("my_tasks_tab_$idx")
                )
            }
        }

        // Task List
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(filteredTasks) { task ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("task_item_${task.id}")
                        .clickable { onOpenWorkspace(task.eventId) },
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        IconButton(
                            onClick = {
                                val next = if (task.status == TaskStatus.COMPLETED) TaskStatus.TO_DO else TaskStatus.COMPLETED
                                OperationsRepository.updateTaskStatus(task.id, next)
                            },
                            modifier = Modifier.size(28.dp).testTag("toggle_task_${task.id}")
                        ) {
                            Icon(
                                imageVector = if (task.status == TaskStatus.COMPLETED) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                contentDescription = "Toggle status",
                                tint = if (task.status == TaskStatus.COMPLETED) OpsAccentEmerald else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = task.name,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    textDecoration = if (task.status == TaskStatus.COMPLETED) androidx.compose.ui.text.style.TextDecoration.LineThrough else null
                                ),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${task.eventName} • ${task.category.displayName}",
                                style = MaterialTheme.typography.labelSmall,
                                color = OpsPrimaryIndigo
                            )
                            Text(
                                text = "Deadline: ${task.deadline}",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (task.isOverdue) OpsAccentRose else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        PriorityBadge(priority = task.priority)
                    }
                }
            }
        }
    }
}
