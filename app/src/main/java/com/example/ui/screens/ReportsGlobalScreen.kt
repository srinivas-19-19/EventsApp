package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.OperationsRepository
import com.example.model.EventLifecycleStatus
import com.example.model.TaskStatus
import com.example.ui.components.EventProgressBar
import com.example.ui.components.StatusPill
import com.example.ui.theme.*

@Composable
fun ReportsGlobalScreen(
    onOpenWorkspace: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val events by OperationsRepository.events.collectAsState()
    val tasks by OperationsRepository.tasks.collectAsState()
    val teamMembers by OperationsRepository.teamMembers.collectAsState()
    val expenses by OperationsRepository.expenses.collectAsState()

    val totalBudget = events.sumOf { it.estimatedBudget }
    val totalSpent = events.sumOf { it.spentBudget }
    val totalCompletedTasks = tasks.count { it.status == TaskStatus.COMPLETED }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("reports_global_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "College Event Operations Audit",
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Campus-wide activity, budget utilization & committee audit",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Button(
                    onClick = { Toast.makeText(context, "Full college audit exported as PDF", Toast.LENGTH_SHORT).show() },
                    colors = ButtonDefaults.buttonColors(containerColor = OpsPrimaryIndigo),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("export_global_audit_btn")
                ) {
                    Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Export All", style = MaterialTheme.typography.labelSmall)
                }
            }
        }

        // Summary Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("INSTITUTIONAL METRICS SUMMARY", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp), color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Total Tracked Events:", style = MaterialTheme.typography.bodyMedium)
                        Text("${events.size} Workspaces", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Active Committee Members:", style = MaterialTheme.typography.bodyMedium)
                        Text("${teamMembers.size} Organizers", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Tasks Execution Rate:", style = MaterialTheme.typography.bodyMedium)
                        Text("$totalCompletedTasks of ${tasks.size} Completed (${if (tasks.isNotEmpty()) ((totalCompletedTasks.toFloat()/tasks.size)*100).toInt() else 0}%)", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = OpsAccentEmerald)
                    }
                    Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Cumulative Sanctioned Budget:", style = MaterialTheme.typography.bodyMedium)
                        Text("₹${String.format("%,.0f", totalBudget)}", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Cumulative Expenses Disbursed:", style = MaterialTheme.typography.bodyMedium)
                        Text("₹${String.format("%,.0f", totalSpent)}", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = OpsAccentTeal)
                    }
                }
            }
        }

        // Per Event Breakdown
        item {
            Text(
                text = "Individual Event Audits",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        }

        items(events) { evt ->
            val evtTasks = tasks.filter { it.eventId == evt.id }
            val completed = evtTasks.count { it.status == TaskStatus.COMPLETED }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(evt.name, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                        StatusPill(text = evt.status.displayName, color = androidx.compose.ui.graphics.Color(evt.status.colorHex))
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Head: ${evt.eventHeadName} • Dept: ${evt.department}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(8.dp))
                    EventProgressBar(completed = completed, total = evtTasks.size)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Spent ₹${String.format("%,.0f", evt.spentBudget)} of ₹${String.format("%,.0f", evt.estimatedBudget)}", style = MaterialTheme.typography.labelSmall, color = OpsAccentTeal)
                        TextButton(onClick = { onOpenWorkspace(evt.id) }) {
                            Text("Open Workspace", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
        }
    }
}
