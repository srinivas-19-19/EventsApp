package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.OperationsRepository
import com.example.model.CollegeEvent
import com.example.model.EventLifecycleStatus
import com.example.ui.theme.OpsPrimaryIndigo
import java.util.UUID

@Composable
fun CreateEventScreen(
    onBack: () -> Unit,
    onCreated: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    var eventName by remember { mutableStateOf("") }
    var eventType by remember { mutableStateOf("Technical Fest") }
    var description by remember { mutableStateOf("") }
    var department by remember { mutableStateOf("Computer Science & Engineering") }
    var club by remember { mutableStateOf("Coding Society") }
    var eventHead by remember { mutableStateOf("Rahul Sharma") }
    var facultyCoordinator by remember { mutableStateOf("Prof. Marcus Sterling") }
    var startDate by remember { mutableStateOf("Nov 15, 2026") }
    var endDate by remember { mutableStateOf("Nov 17, 2026") }
    var startTime by remember { mutableStateOf("09:00 AM") }
    var endTime by remember { mutableStateOf("05:00 PM") }
    var venue by remember { mutableStateOf("Turing Auditorium") }
    var expectedParticipants by remember { mutableStateOf("500") }
    var estimatedBudget by remember { mutableStateOf("50000") }
    var priority by remember { mutableStateOf("High") }
    var objectives by remember { mutableStateOf("Promote technical excellence and collaborative problem solving.") }
    var notes by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .testTag("create_event_flow")
    ) {
        // Top Nav
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            IconButton(onClick = onBack, modifier = Modifier.testTag("create_event_back")) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
            Column {
                Text(
                    text = "Create Event Workspace",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Set up a unified operational workspace for your organizing committee",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = eventName,
                    onValueChange = { eventName = it },
                    label = { Text("Event Name *") },
                    placeholder = { Text("e.g. National Robotics & Drone Expo 2026") },
                    modifier = Modifier.fillMaxWidth().testTag("input_event_name"),
                    shape = RoundedCornerShape(8.dp),
                    singleLine = true
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = eventType,
                        onValueChange = { eventType = it },
                        label = { Text("Event Type *") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = priority,
                        onValueChange = { priority = it },
                        label = { Text("Priority") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true
                    )
                }

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description & Scope *") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    maxLines = 3
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = department,
                        onValueChange = { department = it },
                        label = { Text("Department") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = club,
                        onValueChange = { club = it },
                        label = { Text("Host Club / Society") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true
                    )
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = eventHead,
                        onValueChange = { eventHead = it },
                        label = { Text("Event Head Lead *") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = facultyCoordinator,
                        onValueChange = { facultyCoordinator = it },
                        label = { Text("Faculty Coordinator") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true
                    )
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = startDate,
                        onValueChange = { startDate = it },
                        label = { Text("Start Date") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = endDate,
                        onValueChange = { endDate = it },
                        label = { Text("End Date") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true
                    )
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = startTime,
                        onValueChange = { startTime = it },
                        label = { Text("Start Time") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = endTime,
                        onValueChange = { endTime = it },
                        label = { Text("End Time") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true
                    )
                }

                OutlinedTextField(
                    value = venue,
                    onValueChange = { venue = it },
                    label = { Text("Venue / Location *") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    singleLine = true
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = expectedParticipants,
                        onValueChange = { expectedParticipants = it },
                        label = { Text("Expected Attendees") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = estimatedBudget,
                        onValueChange = { estimatedBudget = it },
                        label = { Text("Budget (₹)") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true
                    )
                }

                OutlinedTextField(
                    value = objectives,
                    onValueChange = { objectives = it },
                    label = { Text("Objectives & Deliverables") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    maxLines = 2
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Action Buttons: Save Draft & Create Event
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedButton(
                onClick = {
                    val id = "evt_${UUID.randomUUID().toString().take(8)}"
                    val newEvent = CollegeEvent(
                        id = id,
                        name = eventName.ifBlank { "Untitled Draft Event" },
                        eventType = eventType,
                        description = description,
                        department = department,
                        club = club,
                        eventHeadId = "usr_rahul",
                        eventHeadName = eventHead,
                        facultyCoordinatorId = "usr_marcus",
                        facultyCoordinatorName = facultyCoordinator,
                        startDate = startDate,
                        endDate = endDate,
                        startTime = startTime,
                        endTime = endTime,
                        venue = venue,
                        expectedParticipants = expectedParticipants.toIntOrNull() ?: 200,
                        estimatedBudget = estimatedBudget.toDoubleOrNull() ?: 20000.0,
                        spentBudget = 0.0,
                        objectives = objectives,
                        status = EventLifecycleStatus.DRAFT
                    )
                    OperationsRepository.addEvent(newEvent)
                    Toast.makeText(context, "Event saved as Draft", Toast.LENGTH_SHORT).show()
                    onCreated(id)
                },
                modifier = Modifier.weight(1f).testTag("save_draft_action"),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Save Draft")
            }

            Button(
                onClick = {
                    val id = "evt_${UUID.randomUUID().toString().take(8)}"
                    val newEvent = CollegeEvent(
                        id = id,
                        name = eventName.ifBlank { "New College Event" },
                        eventType = eventType,
                        description = description,
                        department = department,
                        club = club,
                        eventHeadId = "usr_rahul",
                        eventHeadName = eventHead,
                        facultyCoordinatorId = "usr_marcus",
                        facultyCoordinatorName = facultyCoordinator,
                        startDate = startDate,
                        endDate = endDate,
                        startTime = startTime,
                        endTime = endTime,
                        venue = venue,
                        expectedParticipants = expectedParticipants.toIntOrNull() ?: 500,
                        estimatedBudget = estimatedBudget.toDoubleOrNull() ?: 50000.0,
                        spentBudget = 0.0,
                        objectives = objectives,
                        status = EventLifecycleStatus.PLANNING
                    )
                    OperationsRepository.addEvent(newEvent)
                    Toast.makeText(context, "Event workspace created successfully!", Toast.LENGTH_SHORT).show()
                    onCreated(id)
                },
                modifier = Modifier.weight(1f).testTag("create_event_action"),
                colors = ButtonDefaults.buttonColors(containerColor = OpsPrimaryIndigo),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Create Event", fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(30.dp))
    }
}
