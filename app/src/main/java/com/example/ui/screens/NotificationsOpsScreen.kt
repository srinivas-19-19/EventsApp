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
import androidx.compose.ui.unit.dp
import com.example.data.OperationsRepository
import com.example.model.NotificationType
import com.example.model.OpsNotification
import com.example.ui.components.StatusPill
import com.example.ui.theme.*

@Composable
fun NotificationsOpsScreen(
    onOpenWorkspace: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val notifications by OperationsRepository.notifications.collectAsState()

    var selectedFilter by remember { mutableStateOf("All") } // All, Unread, Urgent, Tasks
    val filters = listOf("All", "Unread", "Urgent", "Tasks")

    val filteredList = remember(notifications, selectedFilter) {
        when (selectedFilter) {
            "Unread" -> notifications.filter { !it.isRead }
            "Urgent" -> notifications.filter { it.isUrgent }
            "Tasks" -> notifications.filter { it.type == NotificationType.TASK }
            else -> notifications
        }
    }

    val unreadCount = notifications.count { !it.isRead }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("notifications_ops_screen")
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Operations Notifications",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = if (unreadCount > 0) "$unreadCount unread alerts & operational updates" else "All caught up",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (unreadCount > 0) {
                TextButton(
                    onClick = { OperationsRepository.markAllNotificationsAsRead() },
                    modifier = Modifier.testTag("mark_all_read_btn")
                ) {
                    Text("Mark all read")
                }
            }
        }

        // Filter chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            filters.forEach { f ->
                val count = when (f) {
                    "Unread" -> notifications.count { !it.isRead }
                    "Urgent" -> notifications.count { it.isUrgent }
                    "Tasks" -> notifications.count { it.type == NotificationType.TASK }
                    else -> notifications.size
                }
                FilterChip(
                    selected = selectedFilter == f,
                    onClick = { selectedFilter = f },
                    label = { Text("$f ($count)") },
                    modifier = Modifier.testTag("notif_filter_${f.lowercase()}")
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Notifications List
        if (filteredList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.NotificationsNone,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "No notifications found in this category",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredList) { notif ->
                    NotificationCard(
                        notification = notif,
                        onOpenWorkspace = { notif.eventId?.let { onOpenWorkspace(it) } },
                        onMarkAsRead = { OperationsRepository.markNotificationAsRead(notif.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun NotificationCard(
    notification: OpsNotification,
    onOpenWorkspace: () -> Unit,
    onMarkAsRead: () -> Unit
) {
    val cardBg = if (notification.isRead) {
        MaterialTheme.colorScheme.surface
    } else {
        OpsPrimaryIndigo.copy(alpha = 0.05f)
    }

    val borderColor = if (!notification.isRead) {
        OpsPrimaryIndigo.copy(alpha = 0.35f)
    } else {
        MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("notif_card_${notification.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        border = BorderStroke(1.dp, borderColor)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Type Icon
                    val icon = when (notification.type) {
                        NotificationType.TASK -> Icons.Default.AssignmentTurnedIn
                        NotificationType.BUDGET -> Icons.Default.Paid
                        NotificationType.DEADLINE -> Icons.Default.Alarm
                        NotificationType.ANNOUNCEMENT -> Icons.Default.Campaign
                        NotificationType.MEETING -> Icons.Default.Groups
                        NotificationType.GENERAL -> Icons.Default.Info
                    }

                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color(notification.type.colorHex).copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            icon,
                            contentDescription = null,
                            tint = Color(notification.type.colorHex),
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Column {
                        Text(
                            text = notification.title,
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = notification.timestamp,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (notification.isUrgent) {
                        StatusPill(text = "URGENT", color = OpsAccentRose)
                    }
                    if (!notification.isRead) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(OpsPrimaryIndigo)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = notification.message,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface
            )

            if (notification.eventName != null) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Event: ${notification.eventName}",
                        style = MaterialTheme.typography.labelSmall,
                        color = OpsPrimaryIndigo,
                        fontWeight = FontWeight.Medium
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (!notification.isRead) {
                            TextButton(
                                onClick = onMarkAsRead,
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text("Mark read", style = MaterialTheme.typography.labelSmall)
                            }
                        }

                        if (notification.eventId != null) {
                            OutlinedButton(
                                onClick = onOpenWorkspace,
                                shape = RoundedCornerShape(6.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp)
                            ) {
                                Text("Workspace", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }
            }
        }
    }
}
