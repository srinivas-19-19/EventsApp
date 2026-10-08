package com.example.ui.screens.workspace

import android.widget.Toast
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.data.OperationsRepository
import com.example.model.*
import com.example.ui.components.StatusPill
import com.example.ui.theme.OpsPrimaryIndigo
import java.util.UUID

@Composable
fun WorkspaceTeamTab(
    event: CollegeEvent,
    teamMembers: List<EventTeamMember>,
    tasks: List<OpsTask>,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showAddMemberModal by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("workspace_team_tab")
    ) {
        // Toolbar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Event Organizing Committee",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "${teamMembers.size} assigned leads and volunteers",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Button(
                onClick = { showAddMemberModal = true },
                colors = ButtonDefaults.buttonColors(containerColor = OpsPrimaryIndigo),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("add_team_member_btn")
            ) {
                Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Add Member")
            }
        }

        // Team Members List
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(teamMembers) { member ->
                val memberTasks = tasks.filter { it.assignedMemberId == member.id || it.assignedMemberName == member.name }
                val completed = memberTasks.count { it.status == TaskStatus.COMPLETED }
                val pending = memberTasks.size - completed

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("member_card_${member.id}"),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(CircleShape)
                                        .background(OpsPrimaryIndigo.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = member.name.split(" ").map { it.take(1) }.joinToString(""),
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = OpsPrimaryIndigo
                                    )
                                }

                                Column {
                                    Text(
                                        text = member.name,
                                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "${member.role} • ${member.department}",
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                        color = OpsPrimaryIndigo
                                    )
                                }
                            }

                            StatusPill(text = member.category.displayName, color = Color(0xFF6366F1))
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Responsibilities
                        Text(
                            text = "Responsibility: ${member.responsibility}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(10.dp))
                        Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                        Spacer(modifier = Modifier.height(8.dp))

                        // Workload metrics & Contacts
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Tasks: ${memberTasks.size} assigned ($completed done, $pending pending)",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    text = member.phone,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal: Add Team Member
    if (showAddMemberModal) {
        var name by remember { mutableStateOf("") }
        var email by remember { mutableStateOf("") }
        var phone by remember { mutableStateOf("+91 ") }
        var dept by remember { mutableStateOf("Computer Science") }
        var role by remember { mutableStateOf("Co-Head") }
        var responsibility by remember { mutableStateOf("") }
        var category by remember { mutableStateOf(TeamCategory.TECHNICAL) }

        Dialog(onDismissRequest = { showAddMemberModal = false }) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .testTag("add_member_modal"),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 8.dp
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Add Team Member", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
                        IconButton(onClick = { showAddMemberModal = false }) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    }

                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Full Name *") },
                        modifier = Modifier.fillMaxWidth().testTag("input_member_name"),
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = role,
                        onValueChange = { role = it },
                        label = { Text("Role (e.g. Sponsorship Lead, Co-Head, Volunteer)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = responsibility,
                        onValueChange = { responsibility = it },
                        label = { Text("Specific Responsibility *") },
                        placeholder = { Text("e.g. Venue sound & light coordination") },
                        modifier = Modifier.fillMaxWidth().testTag("input_member_resp"),
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true
                    )

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = { Text("Email") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = phone,
                            onValueChange = { phone = it },
                            label = { Text("Phone") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            singleLine = true
                        )
                    }

                    // Category dropdown
                    Column {
                        Text("Team Category", style = MaterialTheme.typography.labelSmall)
                        var catExpanded by remember { mutableStateOf(false) }
                        Box {
                            OutlinedButton(
                                onClick = { catExpanded = true },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(category.displayName)
                            }
                            DropdownMenu(expanded = catExpanded, onDismissRequest = { catExpanded = false }) {
                                TeamCategory.values().filter { it != TeamCategory.ALL }.forEach { c ->
                                    DropdownMenuItem(
                                        text = { Text(c.displayName) },
                                        onClick = {
                                            category = c
                                            catExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = {
                            if (name.isNotBlank()) {
                                val newMember = EventTeamMember(
                                    id = "mem_${UUID.randomUUID().toString().take(8)}",
                                    eventId = event.id,
                                    name = name,
                                    email = email.ifBlank { "member@college.edu" },
                                    phone = phone,
                                    department = dept,
                                    role = role,
                                    responsibility = responsibility.ifBlank { "General Event Execution" },
                                    category = category
                                )
                                OperationsRepository.addTeamMember(newMember)
                                showAddMemberModal = false
                                Toast.makeText(context, "Team member added successfully!", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.fillMaxWidth().testTag("save_member_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = OpsPrimaryIndigo),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Add to Event Committee", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
