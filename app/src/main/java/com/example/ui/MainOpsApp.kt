package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.OperationsRepository
import com.example.model.OpsUserRole
import com.example.ui.components.OpsLogo
import com.example.ui.components.StatusPill
import com.example.ui.screens.*
import com.example.ui.theme.OpsPrimaryIndigo

enum class NavDestination(val label: String, val icon: ImageVector) {
    DASHBOARD("Dashboard", Icons.Default.Dashboard),
    EVENTS("Events", Icons.Default.Folder),
    MY_TASKS("My Tasks", Icons.Default.Checklist),
    CALENDAR("Calendar", Icons.Default.CalendarMonth),
    TEAM("Team", Icons.Default.Groups),
    REPORTS("Reports", Icons.Default.Assessment),
    NOTIFICATIONS("Notifications", Icons.Default.Notifications),
    SETTINGS("Settings", Icons.Default.Settings)
}

@Composable
fun MainOpsApp() {
    val currentUser by OperationsRepository.currentUser.collectAsState()
    val events by OperationsRepository.events.collectAsState()
    val announcements by OperationsRepository.announcements.collectAsState()
    val notifications by OperationsRepository.notifications.collectAsState()
    val unreadCount = notifications.count { !it.isRead }

    var isAuthenticated by remember { mutableStateOf(true) }
    var currentDestination by remember { mutableStateOf(NavDestination.DASHBOARD) }

    // Sub-screen navigation states
    var activeWorkspaceEventId by remember { mutableStateOf<String?>("evt_techfest_2026") } // Defaults directly to Tech Fest workspace for instant access
    var isCreatingEvent by remember { mutableStateOf(false) }

    // Quick role switcher dropdown state
    var roleMenuExpanded by remember { mutableStateOf(false) }

    if (!isAuthenticated) {
        AuthOpsScreen(
            onLoginSuccess = {
                isAuthenticated = true
                activeWorkspaceEventId = "evt_techfest_2026"
            }
        )
        return
    }

    Scaffold(
        topBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 1.dp,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OpsLogo(
                        modifier = Modifier.clickable {
                            activeWorkspaceEventId = null
                            isCreatingEvent = false
                            currentDestination = NavDestination.DASHBOARD
                        }
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Notifications Bell with Badge
                        IconButton(
                            onClick = {
                                activeWorkspaceEventId = null
                                isCreatingEvent = false
                                currentDestination = NavDestination.NOTIFICATIONS
                            },
                            modifier = Modifier.size(36.dp).testTag("top_notifications_btn")
                        ) {
                            BadgedBox(
                                badge = {
                                    if (unreadCount > 0) {
                                        Badge { Text("$unreadCount") }
                                    }
                                }
                            ) {
                                Icon(
                                    Icons.Default.Notifications,
                                    contentDescription = "Notifications",
                                    tint = if (activeWorkspaceEventId == null && !isCreatingEvent && currentDestination == NavDestination.NOTIFICATIONS) OpsPrimaryIndigo else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // Reports Quick Button
                        IconButton(
                            onClick = {
                                activeWorkspaceEventId = null
                                isCreatingEvent = false
                                currentDestination = NavDestination.REPORTS
                            },
                            modifier = Modifier.size(36.dp).testTag("top_reports_btn")
                        ) {
                            Icon(
                                Icons.Default.Assessment,
                                contentDescription = "Reports",
                                tint = if (activeWorkspaceEventId == null && !isCreatingEvent && currentDestination == NavDestination.REPORTS) OpsPrimaryIndigo else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Quick Role Switcher Pill
                        Box {
                            Surface(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .clickable { roleMenuExpanded = true }
                                    .testTag("app_role_switcher"),
                                color = Color(currentUser.role.badgeColor).copy(alpha = 0.15f),
                                border = BorderStroke(1.dp, Color(currentUser.role.badgeColor).copy(alpha = 0.4f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = currentUser.role.displayName,
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = Color(currentUser.role.badgeColor)
                                    )
                                    Icon(Icons.Default.ArrowDropDown, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color(currentUser.role.badgeColor))
                                }
                            }

                            DropdownMenu(expanded = roleMenuExpanded, onDismissRequest = { roleMenuExpanded = false }) {
                                OpsUserRole.values().forEach { r ->
                                    DropdownMenuItem(
                                        text = { Text(r.displayName) },
                                        onClick = {
                                            OperationsRepository.switchRole(r)
                                            roleMenuExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        // Settings Icon
                        IconButton(
                            onClick = {
                                activeWorkspaceEventId = null
                                isCreatingEvent = false
                                currentDestination = NavDestination.SETTINGS
                            },
                            modifier = Modifier.size(36.dp).testTag("top_settings_btn")
                        ) {
                            Icon(Icons.Default.Settings, contentDescription = "Settings", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        },
        bottomBar = {
            val bottomNavItems = listOf(
                NavDestination.DASHBOARD,
                NavDestination.EVENTS,
                NavDestination.MY_TASKS,
                NavDestination.CALENDAR,
                NavDestination.TEAM
            )

            NavigationBar(
                modifier = Modifier.navigationBarsPadding(),
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp
            ) {
                bottomNavItems.forEach { destination ->
                    val isSelected = activeWorkspaceEventId == null && !isCreatingEvent && currentDestination == destination

                    NavigationBarItem(
                        selected = isSelected,
                        onClick = {
                            activeWorkspaceEventId = null
                            isCreatingEvent = false
                            currentDestination = destination
                        },
                        icon = { Icon(destination.icon, contentDescription = destination.label) },
                        label = { Text(destination.label, style = MaterialTheme.typography.labelSmall) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = OpsPrimaryIndigo,
                            selectedTextColor = OpsPrimaryIndigo,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                        ),
                        modifier = Modifier.testTag("nav_${destination.name.lowercase()}")
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            when {
                // If user has opened an event workspace
                activeWorkspaceEventId != null -> {
                    BackHandler {
                        activeWorkspaceEventId = null
                    }
                    EventWorkspaceScreen(
                        eventId = activeWorkspaceEventId!!,
                        onBack = { activeWorkspaceEventId = null }
                    )
                }

                // If user is creating a new event
                isCreatingEvent -> {
                    BackHandler {
                        isCreatingEvent = false
                    }
                    CreateEventScreen(
                        onBack = { isCreatingEvent = false },
                        onCreated = { newEventId ->
                            isCreatingEvent = false
                            activeWorkspaceEventId = newEventId
                        }
                    )
                }

                // Main Root Screens
                currentDestination == NavDestination.DASHBOARD -> {
                    OperationsDashboardScreen(
                        currentUser = currentUser,
                        onOpenWorkspace = { eventId -> activeWorkspaceEventId = eventId },
                        onNavigateTasks = { currentDestination = NavDestination.MY_TASKS },
                        onNavigateEvents = { currentDestination = NavDestination.EVENTS }
                    )
                }

                currentDestination == NavDestination.EVENTS -> {
                    EventsListScreen(
                        onOpenWorkspace = { eventId -> activeWorkspaceEventId = eventId },
                        onCreateEvent = { isCreatingEvent = true }
                    )
                }

                currentDestination == NavDestination.MY_TASKS -> {
                    MyTasksScreen(
                        currentUser = currentUser,
                        onOpenWorkspace = { eventId -> activeWorkspaceEventId = eventId }
                    )
                }

                currentDestination == NavDestination.CALENDAR -> {
                    CalendarOpsScreen(
                        onOpenWorkspace = { eventId -> activeWorkspaceEventId = eventId }
                    )
                }

                currentDestination == NavDestination.TEAM -> {
                    BackHandler { currentDestination = NavDestination.DASHBOARD }
                    TeamOpsScreen(
                        onOpenWorkspace = { eventId -> activeWorkspaceEventId = eventId }
                    )
                }

                currentDestination == NavDestination.NOTIFICATIONS -> {
                    BackHandler { currentDestination = NavDestination.DASHBOARD }
                    NotificationsOpsScreen(
                        onOpenWorkspace = { eventId -> activeWorkspaceEventId = eventId }
                    )
                }

                currentDestination == NavDestination.REPORTS -> {
                    BackHandler { currentDestination = NavDestination.DASHBOARD }
                    ReportsGlobalScreen(
                        onOpenWorkspace = { eventId -> activeWorkspaceEventId = eventId }
                    )
                }

                currentDestination == NavDestination.SETTINGS -> {
                    BackHandler { currentDestination = NavDestination.DASHBOARD }
                    SettingsOpsScreen(
                        currentUser = currentUser,
                        onLogout = { isAuthenticated = false }
                    )
                }
            }
        }
    }
}
