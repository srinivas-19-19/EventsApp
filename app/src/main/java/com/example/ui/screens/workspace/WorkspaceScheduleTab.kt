package com.example.ui.screens.workspace

import android.widget.Toast
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
import com.example.model.ScheduleItem
import com.example.ui.theme.OpsPrimaryIndigo
import java.util.UUID

@Composable
fun WorkspaceScheduleTab(
    event: CollegeEvent,
    scheduleItems: List<ScheduleItem>,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showAddItemModal by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("workspace_schedule_tab")
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
                    text = "Event Day Timeline & Flow",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "${scheduleItems.size} scheduled agenda milestones",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Button(
                onClick = { showAddItemModal = true },
                colors = ButtonDefaults.buttonColors(containerColor = OpsPrimaryIndigo),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("add_schedule_item_btn")
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Add Milestone")
            }
        }

        // Timeline List
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(scheduleItems.sortedBy { it.orderIndex }) { item ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Time Pill
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = OpsPrimaryIndigo.copy(alpha = 0.12f),
                            modifier = Modifier.width(80.dp)
                        ) {
                            Box(modifier = Modifier.padding(vertical = 8.dp), contentAlignment = Alignment.Center) {
                                Text(
                                    text = item.time,
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = OpsPrimaryIndigo
                                )
                            }
                        }

                        // Content
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = item.title,
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            if (item.description.isNotBlank()) {
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = item.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            if (item.responsiblePerson.isNotBlank()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "In-charge: ${item.responsiblePerson}",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                    color = OpsPrimaryIndigo
                                )
                            }
                        }

                        IconButton(
                            onClick = {
                                OperationsRepository.deleteScheduleItem(item.id)
                                Toast.makeText(context, "Item deleted from schedule", Toast.LENGTH_SHORT).show()
                            }
                        ) {
                            Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }

    // Modal: Add Schedule Item
    if (showAddItemModal) {
        var time by remember { mutableStateOf("01:30 PM") }
        var title by remember { mutableStateOf("") }
        var description by remember { mutableStateOf("") }
        var responsiblePerson by remember { mutableStateOf("Rahul Sharma") }

        Dialog(onDismissRequest = { showAddItemModal = false }) {
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
                        Text("Add Schedule Item", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
                        IconButton(onClick = { showAddItemModal = false }) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    }

                    OutlinedTextField(
                        value = time,
                        onValueChange = { time = it },
                        label = { Text("Time (e.g. 10:00 AM) *") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Milestone / Session Title *") },
                        placeholder = { Text("e.g. Keynote Address & Panel") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Details & Venue Notes") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        maxLines = 2
                    )

                    OutlinedTextField(
                        value = responsiblePerson,
                        onValueChange = { responsiblePerson = it },
                        label = { Text("Responsible Person / MC") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = {
                            if (title.isNotBlank() && time.isNotBlank()) {
                                val newItem = ScheduleItem(
                                    id = "sch_${UUID.randomUUID().toString().take(8)}",
                                    eventId = event.id,
                                    time = time,
                                    title = title,
                                    description = description,
                                    responsiblePerson = responsiblePerson,
                                    orderIndex = scheduleItems.size + 1
                                )
                                OperationsRepository.addScheduleItem(newItem)
                                showAddItemModal = false
                                Toast.makeText(context, "Schedule milestone added!", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = OpsPrimaryIndigo),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Add to Schedule", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
