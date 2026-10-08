package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.unit.dp
import com.example.data.OperationsRepository
import com.example.model.CollegeEvent
import com.example.model.EventLifecycleStatus
import com.example.model.TaskStatus
import com.example.ui.components.EventProgressBar
import com.example.ui.components.StatusPill
import com.example.ui.theme.OpsPrimaryIndigo

@Composable
fun EventsListScreen(
    onOpenWorkspace: (String) -> Unit,
    onCreateEvent: () -> Unit,
    modifier: Modifier = Modifier
) {
    val events by OperationsRepository.events.collectAsState()
    val tasks by OperationsRepository.tasks.collectAsState()
    val teamMembers by OperationsRepository.teamMembers.collectAsState()

    var selectedStatus by remember { mutableStateOf<EventLifecycleStatus?>(null) } // null = All
    var searchQuery by remember { mutableStateOf("") }

    val filteredEvents = remember(events, selectedStatus, searchQuery) {
        events.filter { evt ->
            val matchStatus = selectedStatus == null || evt.status == selectedStatus
            val matchQuery = searchQuery.isBlank() ||
                    evt.name.contains(searchQuery, ignoreCase = true) ||
                    evt.department.contains(searchQuery, ignoreCase = true) ||
                    evt.eventHeadName.contains(searchQuery, ignoreCase = true)
            matchStatus && matchQuery
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("events_list_screen")
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
                    text = "College Events Directory",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Select an event to enter its dedicated planning workspace",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Button(
                onClick = onCreateEvent,
                colors = ButtonDefaults.buttonColors(containerColor = OpsPrimaryIndigo),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("create_event_btn")
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("New Event")
            }
        }

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Filter events by name, department, or head...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = OpsPrimaryIndigo) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .testTag("search_events_input"),
            shape = RoundedCornerShape(10.dp),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Status Filter Chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = selectedStatus == null,
                onClick = { selectedStatus = null },
                label = { Text("All (${events.size})") }
            )
            EventLifecycleStatus.values().forEach { st ->
                val count = events.count { it.status == st }
                FilterChip(
                    selected = selectedStatus == st,
                    onClick = { selectedStatus = st },
                    label = { Text("${st.displayName} ($count)") }
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Event Cards List
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            items(filteredEvents) { event ->
                val eventTasks = tasks.filter { it.eventId == event.id }
                val completedCount = eventTasks.count { it.status == TaskStatus.COMPLETED }
                val teamCount = teamMembers.count { it.eventId == event.id }.coerceAtLeast(1)

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("event_card_${event.id}")
                        .clickable { onOpenWorkspace(event.id) },
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = event.name,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "${event.eventType} • ${event.department}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            StatusPill(text = event.status.displayName, color = Color(event.status.colorHex))
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Logistics Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Dates", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("${event.startDate} - ${event.endDate}", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold))
                            }
                            Column {
                                Text("Venue", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(event.venue, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold))
                            }
                            Column {
                                Text("Budget", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("₹${String.format("%,.0f", event.estimatedBudget)}", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold))
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Progress
                        EventProgressBar(completed = completedCount, total = eventTasks.size)

                        Spacer(modifier = Modifier.height(14.dp))

                        // Footer Action
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(16.dp), tint = OpsPrimaryIndigo)
                                Text(
                                    text = "Head: ${event.eventHeadName} ($teamCount on team)",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Button(
                                onClick = { onOpenWorkspace(event.id) },
                                colors = ButtonDefaults.buttonColors(containerColor = OpsPrimaryIndigo),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.testTag("open_ws_${event.id}")
                            ) {
                                Text("Open Workspace", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }
            }
        }
    }
}
