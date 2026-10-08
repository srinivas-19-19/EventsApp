package com.example.ui.screens.workspace

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.*
import com.example.ui.components.EventProgressBar
import com.example.ui.components.StatusPill
import com.example.ui.theme.*

@Composable
fun WorkspaceReportsTab(
    event: CollegeEvent,
    tasks: List<OpsTask>,
    teamMembers: List<EventTeamMember>,
    expenses: List<ExpenseItem>,
    requirements: List<RequirementItem>,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val completedTasks = tasks.count { it.status == TaskStatus.COMPLETED }
    val pendingTasks = tasks.size - completedTasks
    val totalExpenseAmount = expenses.sumOf { it.amount }
    val remainingBudget = event.estimatedBudget - totalExpenseAmount

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("workspace_reports_tab"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Toolbar with Export Actions
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Event Operational Audit Report",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "Comprehensive summary for college management & academic dean",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    OutlinedButton(
                        onClick = { Toast.makeText(context, "Exporting CSV report...", Toast.LENGTH_SHORT).show() },
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("export_csv_btn")
                    ) {
                        Text("CSV", style = MaterialTheme.typography.labelSmall)
                    }

                    Button(
                        onClick = { Toast.makeText(context, "Generating official PDF audit report...", Toast.LENGTH_SHORT).show() },
                        colors = ButtonDefaults.buttonColors(containerColor = OpsPrimaryIndigo),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("export_pdf_btn")
                    ) {
                        Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Export PDF", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }

        // Section 1: Executive Summary
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(event.name, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                        StatusPill(text = event.status.displayName, color = androidx.compose.ui.graphics.Color(event.status.colorHex))
                    }
                    Text("Department: ${event.department} • Club: ${event.club}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("Dates: ${event.startDate} - ${event.endDate} (${event.startTime} to ${event.endTime})", style = MaterialTheme.typography.bodySmall)
                    Text("Venue: ${event.venue} • Expected Turnout: ${event.expectedParticipants} students", style = MaterialTheme.typography.bodySmall)
                    Text("Event Head: ${event.eventHeadName} • Faculty In-Charge: ${event.facultyCoordinatorName}", style = MaterialTheme.typography.bodySmall, color = OpsPrimaryIndigo)
                }
            }
        }

        // Section 2: Task Execution Progress
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Task Execution Status", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                    Spacer(modifier = Modifier.height(10.dp))
                    EventProgressBar(completed = completedTasks, total = tasks.size)
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Total Work Items: ${tasks.size}", style = MaterialTheme.typography.bodySmall)
                        Text("Pending Items: $pendingTasks", style = MaterialTheme.typography.bodySmall, color = OpsAccentAmber)
                    }
                }
            }
        }

        // Section 3: Financial Statement & Budget Audit
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Financial Statement", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                    Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Sanctioned College Budget:", style = MaterialTheme.typography.bodySmall)
                        Text("₹${String.format("%,.0f", event.estimatedBudget)}", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold))
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Total Disbursed Expenses:", style = MaterialTheme.typography.bodySmall)
                        Text("₹${String.format("%,.0f", totalExpenseAmount)}", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = OpsAccentTeal)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Remaining Unspent Balance:", style = MaterialTheme.typography.bodySmall)
                        Text("₹${String.format("%,.0f", remainingBudget)}", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = OpsPrimaryIndigo)
                    }
                }
            }
        }

        // Section 4: Committee Roster
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Organizing Committee Leads (${teamMembers.size})", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                    Spacer(modifier = Modifier.height(8.dp))
                    teamMembers.forEach { mem ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("${mem.name} — ${mem.role}", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold))
                            Text(mem.responsibility, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}
