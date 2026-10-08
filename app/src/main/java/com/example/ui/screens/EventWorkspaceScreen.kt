package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.OperationsRepository
import com.example.model.*
import com.example.ui.components.EventProgressBar
import com.example.ui.components.StatusPill
import com.example.ui.screens.workspace.*
import com.example.ui.theme.OpsPrimaryIndigo

@Composable
fun EventWorkspaceScreen(
    eventId: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val events by OperationsRepository.events.collectAsState()
    val allTasks by OperationsRepository.tasks.collectAsState()
    val allTeamMembers by OperationsRepository.teamMembers.collectAsState()
    val allExpenses by OperationsRepository.expenses.collectAsState()
    val allRequirements by OperationsRepository.requirements.collectAsState()
    val allAnnouncements by OperationsRepository.announcements.collectAsState()
    val allMeetings by OperationsRepository.meetings.collectAsState()
    val allDocuments by OperationsRepository.documents.collectAsState()
    val allActivities by OperationsRepository.activities.collectAsState()
    val allScheduleItems by OperationsRepository.scheduleItems.collectAsState()

    val currentEvent = events.find { it.id == eventId } ?: events.first()

    val eventTasks = remember(allTasks, currentEvent) { allTasks.filter { it.eventId == currentEvent.id } }
    val eventTeam = remember(allTeamMembers, currentEvent) { allTeamMembers.filter { it.eventId == currentEvent.id } }
    val eventExpenses = remember(allExpenses, currentEvent) { allExpenses.filter { it.eventId == currentEvent.id } }
    val eventRequirements = remember(allRequirements, currentEvent) { allRequirements.filter { it.eventId == currentEvent.id } }
    val eventAnnouncements = remember(allAnnouncements, currentEvent) { allAnnouncements.filter { it.eventId == currentEvent.id } }
    val eventMeetings = remember(allMeetings, currentEvent) { allMeetings.filter { it.eventId == currentEvent.id } }
    val eventDocuments = remember(allDocuments, currentEvent) { allDocuments.filter { it.eventId == currentEvent.id } }
    val eventActivities = remember(allActivities, currentEvent) { allActivities.filter { it.eventId == currentEvent.id } }
    val eventSchedule = remember(allScheduleItems, currentEvent) { allScheduleItems.filter { it.eventId == currentEvent.id } }

    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabTitles = listOf(
        "Overview",
        "Tasks",
        "Team",
        "Schedule",
        "Budget",
        "Requirements",
        "Announcements",
        "Documents",
        "Meetings",
        "Reports"
    )

    val completedTasksCount = eventTasks.count { it.status == TaskStatus.COMPLETED }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("event_workspace_screen")
    ) {
        // Sticky Event Workspace Header
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 2.dp,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Top row with back button, event title, and status pill
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        IconButton(onClick = onBack, modifier = Modifier.testTag("workspace_back_btn")) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                        Column {
                            Text(
                                text = currentEvent.name,
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${currentEvent.eventType} • Head: ${currentEvent.eventHeadName}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Status Dropdown
                    var statusExpanded by remember { mutableStateOf(false) }
                    Box {
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .clickable { statusExpanded = true }
                                .testTag("status_picker_pill"),
                            color = Color(currentEvent.status.colorHex).copy(alpha = 0.15f),
                            border = BorderStroke(1.dp, Color(currentEvent.status.colorHex))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = currentEvent.status.displayName,
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = Color(currentEvent.status.colorHex)
                                )
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color(currentEvent.status.colorHex))
                            }
                        }
                        DropdownMenu(expanded = statusExpanded, onDismissRequest = { statusExpanded = false }) {
                            EventLifecycleStatus.values().forEach { st ->
                                DropdownMenuItem(
                                    text = { Text(st.displayName) },
                                    onClick = {
                                        OperationsRepository.updateEventStatus(currentEvent.id, st)
                                        statusExpanded = false
                                        Toast.makeText(context, "Event status changed to ${st.displayName}", Toast.LENGTH_SHORT).show()
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Logistics row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "📅 ${currentEvent.startDate} - ${currentEvent.endDate}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "📍 ${currentEvent.venue}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "👥 ${eventTeam.size} Organizers",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                        color = OpsPrimaryIndigo
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Progress Indicator
                EventProgressBar(completed = completedTasksCount, total = eventTasks.size)
            }
        }

        // Horizontal Workspace Tabs Row
        ScrollableTabRow(
            selectedTabIndex = selectedTabIndex,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = OpsPrimaryIndigo,
            edgePadding = 12.dp
        ) {
            tabTitles.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    text = {
                        Text(
                            text = title,
                            fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Medium
                        )
                    },
                    modifier = Modifier.testTag("ws_tab_$index")
                )
            }
        }

        // Active Tab Screen Content
        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            when (selectedTabIndex) {
                0 -> WorkspaceOverviewTab(
                    event = currentEvent,
                    tasks = eventTasks,
                    teamMembers = eventTeam,
                    requirements = eventRequirements,
                    activities = eventActivities,
                    onNavigateTab = { selectedTabIndex = it }
                )
                1 -> WorkspaceTasksTab(
                    event = currentEvent,
                    tasks = eventTasks,
                    teamMembers = eventTeam
                )
                2 -> WorkspaceTeamTab(
                    event = currentEvent,
                    teamMembers = eventTeam,
                    tasks = eventTasks
                )
                3 -> WorkspaceScheduleTab(
                    event = currentEvent,
                    scheduleItems = eventSchedule
                )
                4 -> WorkspaceBudgetTab(
                    event = currentEvent,
                    expenses = eventExpenses
                )
                5 -> WorkspaceRequirementsTab(
                    event = currentEvent,
                    requirements = eventRequirements
                )
                6 -> WorkspaceAnnouncementsTab(
                    event = currentEvent,
                    announcements = eventAnnouncements
                )
                7 -> WorkspaceDocumentsTab(
                    event = currentEvent,
                    documents = eventDocuments
                )
                8 -> WorkspaceMeetingsTab(
                    event = currentEvent,
                    meetings = eventMeetings
                )
                9 -> WorkspaceReportsTab(
                    event = currentEvent,
                    tasks = eventTasks,
                    teamMembers = eventTeam,
                    expenses = eventExpenses,
                    requirements = eventRequirements
                )
            }
        }
    }
}
