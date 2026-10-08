package com.example.ui.screens.workspace

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
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.window.DialogProperties
import com.example.data.OperationsRepository
import com.example.model.*
import com.example.ui.components.KanbanTaskCard
import com.example.ui.components.PriorityBadge
import com.example.ui.components.StatusPill
import com.example.ui.theme.*
import java.util.UUID

@Composable
fun WorkspaceTasksTab(
    event: CollegeEvent,
    tasks: List<OpsTask>,
    teamMembers: List<EventTeamMember>,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isKanbanMode by remember { mutableStateOf(true) } // true = Kanban Columns, false = List View
    var selectedCategoryFilter by remember { mutableStateOf(TeamCategory.ALL) }
    var selectedPriorityFilter by remember { mutableStateOf<TaskPriority?>(null) }
    var searchQuery by remember { mutableStateOf("") }

    var showCreateTaskModal by remember { mutableStateOf(false) }
    var activeDetailTask by remember { mutableStateOf<OpsTask?>(null) }

    val filteredTasks = remember(tasks, selectedCategoryFilter, selectedPriorityFilter, searchQuery) {
        tasks.filter { t ->
            val matchCat = selectedCategoryFilter == TeamCategory.ALL || t.category == selectedCategoryFilter
            val matchPri = selectedPriorityFilter == null || t.priority == selectedPriorityFilter
            val matchQ = searchQuery.isBlank() || t.name.contains(searchQuery, ignoreCase = true) || t.assignedMemberName.contains(searchQuery, ignoreCase = true)
            matchCat && matchPri && matchQ
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("workspace_tasks_tab")
    ) {
        // Toolbar: Search, Filters, View Mode Toggle, "+ Add Task"
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // View toggle: Board vs List
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(2.dp)
                ) {
                    IconButton(
                        onClick = { isKanbanMode = true },
                        modifier = Modifier
                            .size(36.dp)
                            .background(if (isKanbanMode) MaterialTheme.colorScheme.surface else Color.Transparent)
                            .testTag("toggle_kanban_view")
                    ) {
                        Icon(Icons.Default.ViewKanban, contentDescription = "Board", tint = if (isKanbanMode) OpsPrimaryIndigo else MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    IconButton(
                        onClick = { isKanbanMode = false },
                        modifier = Modifier
                            .size(36.dp)
                            .background(if (!isKanbanMode) MaterialTheme.colorScheme.surface else Color.Transparent)
                            .testTag("toggle_list_view")
                    ) {
                        Icon(Icons.Default.ViewList, contentDescription = "List", tint = if (!isKanbanMode) OpsPrimaryIndigo else MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                Button(
                    onClick = { showCreateTaskModal = true },
                    colors = ButtonDefaults.buttonColors(containerColor = OpsPrimaryIndigo),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("add_task_btn")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Task", style = MaterialTheme.typography.labelMedium)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search tasks or assigned members...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = OpsPrimaryIndigo) },
                modifier = Modifier.fillMaxWidth().testTag("search_tasks_input"),
                shape = RoundedCornerShape(8.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Category Filter Chips Carousel
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                TeamCategory.values().forEach { cat ->
                    FilterChip(
                        selected = selectedCategoryFilter == cat,
                        onClick = { selectedCategoryFilter = cat },
                        label = { Text(cat.displayName) }
                    )
                }
            }
        }

        // Main Board or List Content
        if (isKanbanMode) {
            // Horizontal Kanban Columns
            val columns = listOf(
                Pair("TO DO", TaskStatus.TO_DO),
                Pair("IN PROGRESS", TaskStatus.IN_PROGRESS),
                Pair("BLOCKED", TaskStatus.BLOCKED),
                Pair("COMPLETED", TaskStatus.COMPLETED)
            )

            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                columns.forEach { (colTitle, colStatus) ->
                    val colTasks = filteredTasks.filter {
                        if (colStatus == TaskStatus.TO_DO) it.status == TaskStatus.TO_DO || it.status == TaskStatus.OVERDUE
                        else it.status == colStatus
                    }

                    Card(
                        modifier = Modifier
                            .width(280.dp)
                            .fillMaxHeight(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                    ) {
                        Column(modifier = Modifier.fillMaxSize().padding(12.dp)) {
                            // Column Header
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(Color(colStatus.colorHex))
                                    )
                                    Text(
                                        text = colTitle,
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
                                    )
                                }
                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.surface,
                                    modifier = Modifier.size(22.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text("${colTasks.size}", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Scrollable Task Cards in this Column
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(colTasks) { t ->
                                    KanbanTaskCard(
                                        task = t,
                                        onClick = { activeDetailTask = t },
                                        onStatusChange = { newSt -> OperationsRepository.updateTaskStatus(t.id, newSt) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // Detailed Table / List View
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredTasks) { t ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { activeDetailTask = t },
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            IconButton(
                                onClick = {
                                    val nextStatus = if (t.status == TaskStatus.COMPLETED) TaskStatus.TO_DO else TaskStatus.COMPLETED
                                    OperationsRepository.updateTaskStatus(t.id, nextStatus)
                                },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = if (t.status == TaskStatus.COMPLETED) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                    contentDescription = null,
                                    tint = if (t.status == TaskStatus.COMPLETED) OpsAccentEmerald else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = t.name,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                                )
                                Text(
                                    text = "${t.category.displayName} • Assigned to ${t.assignedMemberName} • Due ${t.deadline}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            StatusPill(text = t.status.displayName, color = Color(t.status.colorHex))
                            PriorityBadge(priority = t.priority)
                        }
                    }
                }
            }
        }
    }

    // Modal: Detailed Task View & Comments
    if (activeDetailTask != null) {
        val t = activeDetailTask!!
        var newCommentText by remember { mutableStateOf("") }

        Dialog(
            onDismissRequest = { activeDetailTask = null },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.95f)
                    .fillMaxHeight(0.85f)
                    .clip(RoundedCornerShape(16.dp))
                    .testTag("task_detail_modal"),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    // Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        PriorityBadge(priority = t.priority)
                        IconButton(onClick = { activeDetailTask = null }) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = t.name,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Status Dropdown / Quick Selector
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(TaskStatus.TO_DO, TaskStatus.IN_PROGRESS, TaskStatus.BLOCKED, TaskStatus.COMPLETED).forEach { st ->
                            FilterChip(
                                selected = t.status == st,
                                onClick = {
                                    OperationsRepository.updateTaskStatus(t.id, st)
                                    activeDetailTask = t.copy(status = st)
                                },
                                label = { Text(st.displayName) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                    Spacer(modifier = Modifier.height(12.dp))

                    Text("Description & Instructions", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold))
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(t.description.ifEmpty { "No additional description specified for this task." }, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

                    if (t.notes.isNotBlank()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text("Operational Notes", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold))
                        Text(t.notes, style = MaterialTheme.typography.bodySmall, color = OpsAccentRose)
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Metadata
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text("Assigned Lead", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(t.assignedMemberName, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold))
                        }
                        Column {
                            Text("Category", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(t.category.displayName, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold))
                        }
                        Column {
                            Text("Deadline", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(t.deadline, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold))
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))
                    Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                    Spacer(modifier = Modifier.height(14.dp))

                    // Comments Section
                    Text("Team Discussion (${t.comments.size})", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                    Spacer(modifier = Modifier.height(8.dp))

                    if (t.comments.isEmpty()) {
                        Text("No comments yet. Leave a message for the team below.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    } else {
                        t.comments.forEach { c ->
                            Card(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                shape = RoundedCornerShape(8.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("${c.authorName} (${c.authorRole})", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = OpsPrimaryIndigo)
                                        Text(c.timestamp, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(c.text, style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Post comment input
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = newCommentText,
                            onValueChange = { newCommentText = it },
                            placeholder = { Text("Write a comment or status update...") },
                            modifier = Modifier.weight(1f).testTag("task_comment_input"),
                            shape = RoundedCornerShape(8.dp),
                            singleLine = true
                        )
                        IconButton(
                            onClick = {
                                if (newCommentText.isNotBlank()) {
                                    OperationsRepository.addTaskComment(t.id, newCommentText)
                                    activeDetailTask = OperationsRepository.tasks.value.find { it.id == t.id }
                                    newCommentText = ""
                                }
                            },
                            modifier = Modifier.testTag("send_comment_btn")
                        ) {
                            Icon(Icons.Default.Send, contentDescription = "Send", tint = OpsPrimaryIndigo)
                        }
                    }
                }
            }
        }
    }

    // Modal: Create New Task
    if (showCreateTaskModal) {
        var taskName by remember { mutableStateOf("") }
        var taskDesc by remember { mutableStateOf("") }
        var taskCategory by remember { mutableStateOf(TeamCategory.TECHNICAL) }
        var taskPriority by remember { mutableStateOf(TaskPriority.HIGH) }
        var assignedPerson by remember { mutableStateOf(teamMembers.firstOrNull()?.name ?: "Rahul Sharma") }
        var deadline by remember { mutableStateOf("Oct 22, 2026") }

        Dialog(onDismissRequest = { showCreateTaskModal = false }) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .clip(RoundedCornerShape(16.dp))
                    .testTag("create_task_modal"),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 8.dp
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Create Event Task", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
                        IconButton(onClick = { showCreateTaskModal = false }) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    }

                    OutlinedTextField(
                        value = taskName,
                        onValueChange = { taskName = it },
                        label = { Text("Task Title *") },
                        placeholder = { Text("e.g. Confirm audio mixer technician") },
                        modifier = Modifier.fillMaxWidth().testTag("new_task_title"),
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = taskDesc,
                        onValueChange = { taskDesc = it },
                        label = { Text("Description & Notes") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        maxLines = 3
                    )

                    // Assignee dropdown
                    Column {
                        Text("Assign Team Member *", style = MaterialTheme.typography.labelSmall)
                        var expanded by remember { mutableStateOf(false) }
                        Box {
                            OutlinedButton(
                                onClick = { expanded = true },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(assignedPerson)
                            }
                            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                                teamMembers.forEach { mem ->
                                    DropdownMenuItem(
                                        text = { Text("${mem.name} (${mem.role})") },
                                        onClick = {
                                            assignedPerson = mem.name
                                            expanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = deadline,
                            onValueChange = { deadline = it },
                            label = { Text("Deadline") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            singleLine = true
                        )

                        // Priority chip picker
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Priority", style = MaterialTheme.typography.labelSmall)
                            var priExpanded by remember { mutableStateOf(false) }
                            Box {
                                OutlinedButton(
                                    onClick = { priExpanded = true },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(taskPriority.displayName)
                                }
                                DropdownMenu(expanded = priExpanded, onDismissRequest = { priExpanded = false }) {
                                    TaskPriority.values().forEach { p ->
                                        DropdownMenuItem(
                                            text = { Text(p.displayName) },
                                            onClick = {
                                                taskPriority = p
                                                priExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = {
                            if (taskName.isNotBlank()) {
                                val newTask = OpsTask(
                                    id = "tsk_${UUID.randomUUID().toString().take(8)}",
                                    eventId = event.id,
                                    eventName = event.name,
                                    name = taskName,
                                    description = taskDesc,
                                    assignedMemberId = teamMembers.find { it.name == assignedPerson }?.id ?: "mem_rahul",
                                    assignedMemberName = assignedPerson,
                                    category = taskCategory,
                                    priority = taskPriority,
                                    startDate = "Today",
                                    deadline = deadline,
                                    status = TaskStatus.TO_DO
                                )
                                OperationsRepository.addTask(newTask)
                                showCreateTaskModal = false
                                Toast.makeText(context, "Task created and added to board!", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.fillMaxWidth().testTag("save_new_task_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = OpsPrimaryIndigo),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Add to Task Board", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
