package com.example.ui.screens.workspace

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Send
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
import com.example.model.InternalAnnouncement
import com.example.ui.components.StatusPill
import com.example.ui.theme.OpsAccentRose
import com.example.ui.theme.OpsPrimaryIndigo
import java.util.UUID

@Composable
fun WorkspaceAnnouncementsTab(
    event: CollegeEvent,
    announcements: List<InternalAnnouncement>,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showBroadcastModal by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("workspace_announcements_tab")
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
                    text = "Internal Team Broadcasts",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "Operational directives & alerts for event committee leads",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Button(
                onClick = { showBroadcastModal = true },
                colors = ButtonDefaults.buttonColors(containerColor = OpsPrimaryIndigo),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("broadcast_announcement_btn")
            ) {
                Icon(Icons.Default.Campaign, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Broadcast")
            }
        }

        // List
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(announcements) { ann ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
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
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    text = ann.title,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                if (ann.priority == "Urgent") {
                                    StatusPill(text = "URGENT", color = OpsAccentRose)
                                }
                            }
                            Text(
                                text = ann.postedAt,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = ann.message,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(10.dp))
                        Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Posted by ${ann.postedBy}",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                color = OpsPrimaryIndigo
                            )
                            Text(
                                text = "Audience: ${ann.targetAudience}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }

    // Modal
    if (showBroadcastModal) {
        var title by remember { mutableStateOf("") }
        var message by remember { mutableStateOf("") }
        var isUrgent by remember { mutableStateOf(false) }
        var targetAudience by remember { mutableStateOf("Entire Team") }

        Dialog(onDismissRequest = { showBroadcastModal = false }) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .clip(RoundedCornerShape(16.dp)),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 8.dp
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Post Broadcast", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
                        IconButton(onClick = { showBroadcastModal = false }) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    }

                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Announcement Title *") },
                        placeholder = { Text("e.g. Stage setup moved to 4 PM") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = targetAudience,
                        onValueChange = { targetAudience = it },
                        label = { Text("Target Audience (e.g. Entire Team, Tech Leads)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = message,
                        onValueChange = { message = it },
                        label = { Text("Message Body *") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        maxLines = 4
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = isUrgent, onCheckedChange = { isUrgent = it })
                        Text("Mark as High Priority / Urgent Alert", style = MaterialTheme.typography.bodySmall)
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Button(
                        onClick = {
                            if (title.isNotBlank() && message.isNotBlank()) {
                                val newAnn = InternalAnnouncement(
                                    id = "ann_${UUID.randomUUID().toString().take(8)}",
                                    eventId = event.id,
                                    title = title,
                                    message = message,
                                    priority = if (isUrgent) "Urgent" else "Normal",
                                    targetAudience = targetAudience,
                                    postedBy = OperationsRepository.currentUser.value.name,
                                    postedAt = "Just now"
                                )
                                OperationsRepository.addAnnouncement(newAnn)
                                showBroadcastModal = false
                                Toast.makeText(context, "Announcement sent to team!", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = OpsPrimaryIndigo),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Send Broadcast", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
