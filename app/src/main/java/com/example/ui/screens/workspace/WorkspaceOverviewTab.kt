package com.example.ui.screens.workspace

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.*
import com.example.ui.components.EventProgressBar
import com.example.ui.components.PriorityBadge
import com.example.ui.components.StatWidget
import com.example.ui.theme.*

@Composable
fun WorkspaceOverviewTab(
    event: CollegeEvent,
    tasks: List<OpsTask>,
    teamMembers: List<EventTeamMember>,
    requirements: List<RequirementItem>,
    activities: List<ActivityLog>,
    onNavigateTab: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val completedCount = tasks.count { it.status == TaskStatus.COMPLETED }
    val inProgressCount = tasks.count { it.status == TaskStatus.IN_PROGRESS }
    val blockedCount = tasks.count { it.status == TaskStatus.BLOCKED }
    val overdueCount = tasks.count { it.status == TaskStatus.OVERDUE || it.isOverdue }
    val pendingCount = tasks.count { it.status == TaskStatus.TO_DO }

    val remainingBudget = event.estimatedBudget - event.spentBudget

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Visual Event Progress Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    EventProgressBar(completed = completedCount, total = tasks.size)

                    Spacer(modifier = Modifier.height(14.dp))

                    // Task status breakdown pill row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Completed", style = MaterialTheme.typography.labelSmall, color = OpsAccentEmerald)
                            Text("$completedCount", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = OpsAccentEmerald)
                        }
                        Column {
                            Text("In Progress", style = MaterialTheme.typography.labelSmall, color = OpsAccentSky)
                            Text("$inProgressCount", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = OpsAccentSky)
                        }
                        Column {
                            Text("Pending", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("$pendingCount", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                        }
                        Column {
                            Text("Blocked", style = MaterialTheme.typography.labelSmall, color = OpsAccentRose)
                            Text("$blockedCount", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = OpsAccentRose)
                        }
                        Column {
                            Text("Overdue", style = MaterialTheme.typography.labelSmall, color = Color(0xFFDC2626))
                            Text("$overdueCount", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = Color(0xFFDC2626))
                        }
                    }
                }
            }
        }

        // Budget & Team Snapshot
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatWidget(
                    title = "Spent Budget",
                    value = "₹${String.format("%,.0f", event.spentBudget)}",
                    icon = Icons.Default.AccountBalanceWallet,
                    accentColor = OpsAccentTeal,
                    subtext = "₹${String.format("%,.0f", remainingBudget)} remaining",
                    modifier = Modifier.weight(1f)
                )
                StatWidget(
                    title = "Organizing Team",
                    value = "${teamMembers.size}",
                    icon = Icons.Default.People,
                    accentColor = OpsPrimaryIndigo,
                    subtext = "${event.expectedParticipants} expected delegates",
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Event Description & Objectives
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Event Scope & Description",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = event.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 20.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                    Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Key Operational Objectives",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = event.objectives,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Upcoming Deadlines (Critical & High priority tasks due soon)
        item {
            Text(
                text = "Upcoming Deadlines & Urgent Tasks",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        val urgentTasks = tasks.filter { it.status != TaskStatus.COMPLETED }.sortedByDescending { it.priority.ordinal }.take(3)
        items(urgentTasks) { t ->
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
                    PriorityBadge(priority = t.priority)
                    Column(modifier = Modifier.weight(1f)) {
                        Text(t.name, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
                        Text("Assigned: ${t.assignedMemberName} • Due: ${t.deadline}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    if (t.isOverdue) {
                        Surface(shape = RoundedCornerShape(4.dp), color = OpsAccentRose.copy(alpha = 0.15f)) {
                            Text("OVERDUE", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = OpsAccentRose, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                        }
                    }
                }
            }
        }

        // Recent Activity Feed
        item {
            Text(
                text = "Recent Event Activity",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
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
                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(OpsPrimaryIndigo))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(act.message, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium))
                        Text("${act.authorName} • ${act.timestamp}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}
