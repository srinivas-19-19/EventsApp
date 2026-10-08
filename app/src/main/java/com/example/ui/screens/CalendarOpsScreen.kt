package com.example.ui.screens

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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.data.OperationsRepository
import com.example.model.TaskStatus
import com.example.ui.components.StatusPill
import com.example.ui.theme.*

@Composable
fun CalendarOpsScreen(
    onOpenWorkspace: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedDay by remember { mutableStateOf(21) }
    var filterType by remember { mutableStateOf("All") } // All, Events, Deadlines, Meetings

    val events by OperationsRepository.events.collectAsState()
    val tasks by OperationsRepository.tasks.collectAsState()
    val meetings by OperationsRepository.meetings.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("calendar_ops_screen")
    ) {
        // Header
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Operations Calendar",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "October 2026 • Deadlines, sync meetings & event execution dates",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Calendar Grid Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    listOf("S", "M", "T", "W", "T", "F", "S").forEach { day ->
                        Text(
                            text = day,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.width(36.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                val row1 = listOf(11, 12, 13, 14, 15, 16, 17)
                val row2 = listOf(18, 19, 20, 21, 22, 23, 24)
                val row3 = listOf(25, 26, 27, 28, 29, 30, 31)

                listOf(row1, row2, row3).forEach { week ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        week.forEach { d ->
                            val isSelected = selectedDay == d
                            val hasEvent = d in listOf(21, 24, 25, 26, 18, 15)

                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(if (isSelected) OpsPrimaryIndigo else Color.Transparent)
                                    .clickable { selectedDay = d },
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "$d",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontWeight = if (isSelected || hasEvent) FontWeight.Bold else FontWeight.Normal
                                        ),
                                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                    )
                                    if (hasEvent && !isSelected) {
                                        Box(modifier = Modifier.size(4.dp).clip(CircleShape).background(OpsAccentTeal))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Scheduled Items for the Selected Day
        Text(
            text = "Scheduled Operations on Oct $selectedDay, 2026",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Check for meetings on this day
            items(meetings) { m ->
                Card(
                    modifier = Modifier.fillMaxWidth().clickable { onOpenWorkspace(m.eventId) },
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(Icons.Default.Groups, contentDescription = null, tint = OpsAccentSky, modifier = Modifier.size(20.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(m.title, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
                            Text("${m.time} • ${m.location}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        StatusPill(text = "MEETING", color = OpsAccentSky)
                    }
                }
            }

            // Check for tasks due
            items(tasks.take(4)) { t ->
                Card(
                    modifier = Modifier.fillMaxWidth().clickable { onOpenWorkspace(t.eventId) },
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(Icons.Default.Checklist, contentDescription = null, tint = OpsPrimaryIndigo, modifier = Modifier.size(20.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(t.name, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
                            Text("${t.eventName} • Lead: ${t.assignedMemberName}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        StatusPill(text = "DEADLINE", color = OpsPrimaryIndigo)
                    }
                }
            }
        }
    }
}
