package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.OperationsRepository
import com.example.model.EventTeamMember
import com.example.model.TeamCategory
import com.example.ui.components.StatusPill
import com.example.ui.theme.*
import java.util.UUID

@Composable
fun TeamOpsScreen(
    onOpenWorkspace: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val teamMembers by OperationsRepository.teamMembers.collectAsState()
    val events by OperationsRepository.events.collectAsState()
    val tasks by OperationsRepository.tasks.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<TeamCategory?>(null) }
    var selectedEventId by remember { mutableStateOf<String?>(null) } // null = All Events
    var showAddModal by remember { mutableStateOf(false) }

    val filteredMembers = remember(teamMembers, searchQuery, selectedCategory, selectedEventId) {
        teamMembers.filter { m ->
            val matchQuery = searchQuery.isBlank() ||
                    m.name.contains(searchQuery, ignoreCase = true) ||
                    m.responsibility.contains(searchQuery, ignoreCase = true) ||
                    m.department.contains(searchQuery, ignoreCase = true) ||
                    m.role.contains(searchQuery, ignoreCase = true)
            val matchCategory = selectedCategory == null || m.category == selectedCategory
            val matchEvent = selectedEventId == null || m.eventId == selectedEventId
            matchQuery && matchCategory && matchEvent
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("team_ops_screen")
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Organizing Committee",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${teamMembers.size} organizers & heads across college event workspaces",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Button(
                onClick = { showAddModal = true },
                colors = ButtonDefaults.buttonColors(containerColor = OpsPrimaryIndigo),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("add_organizer_btn")
            ) {
                Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Add Member")
            }
        }

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search by organizer, role, or responsibility...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = OpsPrimaryIndigo) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .testTag("team_search_input"),
            shape = RoundedCornerShape(10.dp),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Category filter scroll
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = selectedCategory == null,
                onClick = { selectedCategory = null },
                label = { Text("All Categories (${teamMembers.size})") }
            )
            TeamCategory.values().filter { it != TeamCategory.ALL }.forEach { cat ->
                val count = teamMembers.count { it.category == cat }
                if (count > 0) {
                    FilterChip(
                        selected = selectedCategory == cat,
                        onClick = { selectedCategory = if (selectedCategory == cat) null else cat },
                        label = { Text("${cat.displayName} ($count)") }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Event filter pills
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = selectedEventId == null,
                onClick = { selectedEventId = null },
                label = { Text("All Events") }
            )
            events.forEach { evt ->
                FilterChip(
                    selected = selectedEventId == evt.id,
                    onClick = { selectedEventId = if (selectedEventId == evt.id) null else evt.id },
                    label = { Text(evt.name) }
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Members List
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(filteredMembers) { member ->
                val memberTasks = tasks.filter {
                    it.assignedMemberName.contains(member.name.split(" ").first(), ignoreCase = true)
                }
                val completedCount = memberTasks.count { it.status == com.example.model.TaskStatus.COMPLETED }
                val eventName = events.find { it.id == member.eventId }?.name ?: "Event Workspace"

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("team_member_card_${member.id}"),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                // Avatar Initials Circle
                                val initials = member.name.split(" ").mapNotNull { it.firstOrNull()?.toString() }.take(2).joinToString("")
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(OpsPrimaryIndigo.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = initials,
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = OpsPrimaryIndigo
                                    )
                                }

                                Column {
                                    Text(
                                        text = member.name,
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = member.role,
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                        color = OpsAccentTeal
                                    )
                                }
                            }

                            StatusPill(
                                text = member.category.displayName,
                                color = OpsPrimaryIndigo
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Assigned Responsibility Banner
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    Icons.Default.VerifiedUser,
                                    contentDescription = null,
                                    tint = OpsPrimaryIndigo,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "Responsibility: ${member.responsibility}",
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Details: Department, Email, Event
                        Text(
                            text = "Department: ${member.department} • Event: $eventName",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${member.email} • ${member.phone}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Task Progress
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Tasks: $completedCount / ${memberTasks.size} completed",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            OutlinedButton(
                                onClick = { onOpenWorkspace(member.eventId) },
                                shape = RoundedCornerShape(6.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp)
                            ) {
                                Text("Open Workspace", style = MaterialTheme.typography.labelSmall)
                            }
                        }

                        if (memberTasks.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            val progress = (completedCount.toFloat() / memberTasks.size).coerceIn(0f, 1f)
                            LinearProgressIndicator(
                                progress = { progress },
                                modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)),
                                color = OpsAccentTeal,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }

    // Modal: Add Team Member
    if (showAddModal) {
        var name by remember { mutableStateOf("") }
        var email by remember { mutableStateOf("") }
        var phone by remember { mutableStateOf("") }
        var department by remember { mutableStateOf("Computer Science") }
        var role by remember { mutableStateOf("Committee Lead") }
        var responsibility by remember { mutableStateOf("") }
        var category by remember { mutableStateOf(TeamCategory.TECHNICAL) }
        var targetEventId by remember { mutableStateOf(events.firstOrNull()?.id ?: "evt_techfest_2026") }

        Dialog(onDismissRequest = { showAddModal = false }) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .testTag("add_organizer_modal"),
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
                        Text("Add Organizing Member", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
                        IconButton(onClick = { showAddModal = false }) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    }

                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Full Name *") },
                        placeholder = { Text("e.g. Sreenu Gorkal") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("College Email *") },
                        placeholder = { Text("e.g. sreenu@college.edu") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("Phone Number") },
                        placeholder = { Text("+91 98765-43210") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = responsibility,
                        onValueChange = { responsibility = it },
                        label = { Text("Assigned Responsibility *") },
                        placeholder = { Text("e.g. Website & Registration, Sponsorship, Decorations") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = department,
                        onValueChange = { department = it },
                        label = { Text("Department") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Button(
                        onClick = {
                            if (name.isBlank() || responsibility.isBlank()) {
                                Toast.makeText(context, "Please fill in name and responsibility", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            val newMember = EventTeamMember(
                                id = "mem_${UUID.randomUUID().toString().take(8)}",
                                eventId = targetEventId,
                                name = name,
                                email = email.ifBlank { "${name.lowercase().replace(" ", ".")}@college.edu" },
                                phone = phone.ifBlank { "+91 98000-00000" },
                                department = department,
                                role = role,
                                responsibility = responsibility,
                                category = category
                            )
                            OperationsRepository.addTeamMember(newMember)
                            Toast.makeText(context, "Added $name to organizing committee!", Toast.LENGTH_SHORT).show()
                            showAddModal = false
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = OpsPrimaryIndigo)
                    ) {
                        Text("Save & Assign to Committee")
                    }
                }
            }
        }
    }
}
