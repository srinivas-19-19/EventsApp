package com.example.ui.screens.workspace

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
import com.example.model.CollegeEvent
import com.example.model.EventMeeting
import com.example.model.MeetingActionItem
import com.example.ui.components.StatusPill
import com.example.ui.theme.*
import java.util.UUID

@Composable
fun WorkspaceMeetingsTab(
    event: CollegeEvent,
    meetings: List<EventMeeting>,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showCreateMeetingModal by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("workspace_meetings_tab")
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
                    text = "Event Coordination Meetings",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "Sync agendas, record minutes, and convert action items to tasks",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Button(
                onClick = { showCreateMeetingModal = true },
                colors = ButtonDefaults.buttonColors(containerColor = OpsPrimaryIndigo),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("create_meeting_btn")
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Schedule")
            }
        }

        // Meetings List
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            items(meetings) { mtg ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
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
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = mtg.title,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "${mtg.date} at ${mtg.time} • ${mtg.location}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = OpsPrimaryIndigo
                                )
                            }
                            StatusPill(text = mtg.meetingType, color = OpsAccentSky)
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text("Attendees: ${mtg.participants}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

                        Spacer(modifier = Modifier.height(6.dp))

                        Text("Agenda:", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                        Text(mtg.agenda, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

                        if (mtg.notes.isNotBlank()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("Minutes / Notes: ${mtg.notes}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface)
                        }

                        // Action Items
                        if (mtg.actionItems.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = "Action Items (${mtg.actionItems.size})",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            mtg.actionItems.forEach { item ->
                                Card(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(8.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(item.text, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold))
                                            Text("Assignee: ${item.assignedTo}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }

                                        if (item.isConvertedToTask) {
                                            Surface(shape = RoundedCornerShape(4.dp), color = OpsAccentEmerald.copy(alpha = 0.15f)) {
                                                Text("CONVERTED TO TASK ✓", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = OpsAccentEmerald, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                            }
                                        } else {
                                            Button(
                                                onClick = {
                                                    OperationsRepository.convertActionItemToTask(mtg.id, item.id)
                                                    Toast.makeText(context, "Action item converted to Kanban Task!", Toast.LENGTH_SHORT).show()
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = OpsPrimaryIndigo),
                                                shape = RoundedCornerShape(6.dp),
                                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                                modifier = Modifier.testTag("convert_action_${item.id}")
                                            ) {
                                                Icon(Icons.Default.ArrowForward, contentDescription = null, modifier = Modifier.size(12.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("Convert to Task", style = MaterialTheme.typography.labelSmall)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal: Schedule Meeting
    if (showCreateMeetingModal) {
        var title by remember { mutableStateOf("") }
        var type by remember { mutableStateOf("Team Meeting") }
        var date by remember { mutableStateOf("Oct 22, 2026") }
        var time by remember { mutableStateOf("04:00 PM") }
        var location by remember { mutableStateOf("Conference Room 102") }
        var participants by remember { mutableStateOf("Rahul, Sreenu, Kiran, Priya") }
        var agenda by remember { mutableStateOf("") }
        var actionItemText by remember { mutableStateOf("") }

        Dialog(onDismissRequest = { showCreateMeetingModal = false }) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
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
                        Text("Schedule Coordination Meeting", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
                        IconButton(onClick = { showCreateMeetingModal = false }) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    }

                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Meeting Title *") },
                        placeholder = { Text("e.g. Sponsor Final Logistics & Catering Review") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true
                    )

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = date,
                            onValueChange = { date = it },
                            label = { Text("Date") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = time,
                            onValueChange = { time = it },
                            label = { Text("Time") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            singleLine = true
                        )
                    }

                    OutlinedTextField(
                        value = location,
                        onValueChange = { location = it },
                        label = { Text("Location") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = participants,
                        onValueChange = { participants = it },
                        label = { Text("Participants") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = agenda,
                        onValueChange = { agenda = it },
                        label = { Text("Agenda & Points") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        maxLines = 2
                    )

                    OutlinedTextField(
                        value = actionItemText,
                        onValueChange = { actionItemText = it },
                        label = { Text("Initial Action Item (Optional)") },
                        placeholder = { Text("e.g. Confirm generator diesel delivery") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = {
                            if (title.isNotBlank()) {
                                val actionItems = mutableListOf<MeetingActionItem>()
                                if (actionItemText.isNotBlank()) {
                                    actionItems.add(MeetingActionItem("act_${UUID.randomUUID().toString().take(6)}", actionItemText, "Rahul Sharma"))
                                }
                                val newMeeting = EventMeeting(
                                    id = "mtg_${UUID.randomUUID().toString().take(8)}",
                                    eventId = event.id,
                                    title = title,
                                    meetingType = type,
                                    date = date,
                                    time = time,
                                    location = location,
                                    participants = participants,
                                    agenda = agenda,
                                    actionItems = actionItems
                                )
                                OperationsRepository.addMeeting(newMeeting)
                                showCreateMeetingModal = false
                                Toast.makeText(context, "Meeting scheduled with action items!", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = OpsPrimaryIndigo),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Save & Notify Participants", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
