package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.OperationsRepository
import com.example.model.*
import com.example.ui.components.*
import com.example.ui.theme.*

@Composable
fun OperationsDashboardScreen(
    currentUser: OpsUser,
    onOpenWorkspace: (String) -> Unit,
    onNavigateTasks: () -> Unit,
    onNavigateEvents: () -> Unit,
    modifier: Modifier = Modifier
) {
    val events by OperationsRepository.events.collectAsState()
    val tasks by OperationsRepository.tasks.collectAsState()
    val teamMembers by OperationsRepository.teamMembers.collectAsState()
    val activities by OperationsRepository.activities.collectAsState()

    val totalEvents = events.size
    val activeEvents = events.count { it.status == EventLifecycleStatus.PLANNING || it.status == EventLifecycleStatus.ONGOING }
    val upcomingEventsCount = events.count { it.status == EventLifecycleStatus.UPCOMING }
    val completedEventsCount = events.count { it.status == EventLifecycleStatus.COMPLETED }

    val myTasks = remember(tasks, currentUser) {
        tasks.filter { it.assignedMemberName.contains(currentUser.name.split(" ").first(), ignoreCase = true) }
    }
    val myPendingTasks = myTasks.filter { it.status != TaskStatus.COMPLETED }
    val overdueTasksCount = tasks.count { it.status == TaskStatus.OVERDUE || it.isOverdue }

    val totalBudget = events.sumOf { it.estimatedBudget }
    val totalBudgetUsed = events.sumOf { it.spentBudget }

    // Primary demo event for progress
    val primaryEvent = events.find { it.id == "evt_techfest_2026" } ?: events.first()
    val primaryEventTasks = tasks.filter { it.eventId == primaryEvent.id }
    val primaryCompleted = primaryEventTasks.count { it.status == TaskStatus.COMPLETED }
    val primaryTotal = primaryEventTasks.size

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("ops_dashboard_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Welcome Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Operations Overview",
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "College Event Operations & Planning Workspace",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                StatusPill(text = currentUser.role.displayName, color = Color(currentUser.role.badgeColor))
            }
        }

        // Metrics Grid 1: Events & Tasks
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatWidget(
                    title = "Total Events",
                    value = "$totalEvents",
                    icon = Icons.Default.Event,
                    accentColor = OpsPrimaryIndigo,
                    subtext = "$activeEvents Active • $upcomingEventsCount Upcoming • $completedEventsCount Completed",
                    modifier = Modifier.weight(1f)
                )
                StatWidget(
                    title = "My Tasks",
                    value = "${myPendingTasks.size}",
                    icon = Icons.Default.Checklist,
                    accentColor = OpsAccentTeal,
                    subtext = "${myTasks.size - myPendingTasks.size} completed",
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Metrics Grid 2: Active & Upcoming breakdown pills
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                ) {
                    Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Active Events", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("$activeEvents", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = OpsAccentTeal)
                    }
                }
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                ) {
                    Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Upcoming", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("$upcomingEventsCount", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = OpsPrimaryIndigo)
                    }
                }
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                ) {
                    Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Completed", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("$completedEventsCount", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = OpsAccentSky)
                    }
                }
            }
        }

        // Metrics Grid 3: Overdue & Team
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatWidget(
                    title = "Overdue Tasks",
                    value = "$overdueTasksCount",
                    icon = Icons.Default.Warning,
                    accentColor = OpsAccentRose,
                    subtext = "Require immediate follow-up",
                    modifier = Modifier.weight(1f)
                )
                StatWidget(
                    title = "Team Members",
                    value = "${teamMembers.size}",
                    icon = Icons.Default.Groups,
                    accentColor = OpsAccentSky,
                    subtext = "Across 12 categories",
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Metrics Grid 3: Budget KPI
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("TOTAL COLLEGE EVENT BUDGET", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp), color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("₹${String.format("%,.0f", totalBudget)}", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black))
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("BUDGET SPENT", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp), color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("₹${String.format("%,.0f", totalBudgetUsed)}", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black), color = OpsAccentTeal)
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    val budgetPct = (totalBudgetUsed / totalBudget).toFloat().coerceIn(0f, 1f)
                    LinearProgressIndicator(
                        progress = { budgetPct },
                        modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                        color = OpsAccentTeal,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "₹${String.format("%,.0f", totalBudget - totalBudgetUsed)} remaining across all events",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Event Progress (Featured Tech Fest Workspace)
        item {
            Text(
                text = "Featured Event Workspace Progress",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        }

        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("featured_workspace_card")
                    .clickable { onOpenWorkspace(primaryEvent.id) },
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, OpsPrimaryIndigo.copy(alpha = 0.4f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    text = primaryEvent.name,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                StatusPill(text = primaryEvent.status.displayName, color = Color(primaryEvent.status.colorHex))
                            }
                            Text(
                                text = "Head: ${primaryEvent.eventHeadName} • Venue: ${primaryEvent.venue}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Button(
                            onClick = { onOpenWorkspace(primaryEvent.id) },
                            colors = ButtonDefaults.buttonColors(containerColor = OpsPrimaryIndigo),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("open_workspace_cta")
                        ) {
                            Text("Open Workspace", style = MaterialTheme.typography.labelSmall)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    EventProgressBar(completed = primaryCompleted, total = primaryTotal)
                }
            }
        }

        // Upcoming Events Section
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Upcoming College Events",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                TextButton(onClick = onNavigateEvents, modifier = Modifier.testTag("view_all_events_btn")) {
                    Text("View All (${events.size})")
                }
            }
        }

        val upcomingList = events.filter { it.status == EventLifecycleStatus.UPCOMING || it.status == EventLifecycleStatus.PLANNING }
        items(upcomingList.take(2)) { evt ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onOpenWorkspace(evt.id) },
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(evt.name, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                            StatusPill(text = evt.status.displayName, color = Color(evt.status.colorHex))
                        }
                        Text("${evt.eventType} • ${evt.startDate} • ${evt.venue}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("Head: ${evt.eventHeadName} • Budget: ₹${String.format("%,.0f", evt.estimatedBudget)}", style = MaterialTheme.typography.labelSmall, color = OpsPrimaryIndigo)
                    }

                    OutlinedButton(
                        onClick = { onOpenWorkspace(evt.id) },
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp)
                    ) {
                        Text("Workspace", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }

        // Upcoming Deadlines Section
        item {
            Text(
                text = "Upcoming Operations Deadlines",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        }

        val deadlineTasks = tasks.filter { it.status != TaskStatus.COMPLETED }.sortedBy { it.deadline }
        items(deadlineTasks.take(3)) { dlTask ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onOpenWorkspace(dlTask.eventId) },
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, if (dlTask.isOverdue) OpsAccentRose.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        Icons.Default.Alarm,
                        contentDescription = null,
                        tint = if (dlTask.isOverdue) OpsAccentRose else OpsAccentTeal,
                        modifier = Modifier.size(20.dp)
                    )

                    Column(modifier = Modifier.weight(1f)) {
                        Text(dlTask.name, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold))
                        Text(
                            text = "Due: ${dlTask.deadline} • Assigned to ${dlTask.assignedMemberName} (${dlTask.eventName})",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    PriorityBadge(priority = dlTask.priority)
                }
            }
        }

        // My Pending Tasks Section
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "My Pending Tasks (${myPendingTasks.size})",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                TextButton(onClick = onNavigateTasks, modifier = Modifier.testTag("view_all_tasks_btn")) {
                    Text("View All")
                }
            }
        }

        if (myPendingTasks.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Box(modifier = Modifier.padding(16.dp), contentAlignment = Alignment.Center) {
                        Text("No pending tasks assigned to you right now.", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        } else {
            items(myPendingTasks.take(3)) { task ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onOpenWorkspace(task.eventId) },
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        IconButton(
                            onClick = { OperationsRepository.updateTaskStatus(task.id, TaskStatus.COMPLETED) },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(Icons.Default.RadioButtonUnchecked, contentDescription = "Complete", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(task.name, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
                            Text("${task.eventName} • Due ${task.deadline}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }

                        PriorityBadge(priority = task.priority)
                    }
                }
            }
        }

        // Recent Activity Feed
        item {
            Text(
                text = "Recent Operations Activity",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        }

        items(activities.take(4)) { act ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(OpsPrimaryIndigo)
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(act.message, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium))
                        Text("By ${act.authorName} • ${act.timestamp}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}
