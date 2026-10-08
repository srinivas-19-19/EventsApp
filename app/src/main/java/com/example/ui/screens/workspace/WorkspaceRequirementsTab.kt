package com.example.ui.screens.workspace

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.data.OperationsRepository
import com.example.model.CollegeEvent
import com.example.model.RequirementItem
import com.example.model.RequirementStatus
import com.example.ui.components.StatusPill
import com.example.ui.theme.OpsPrimaryIndigo
import java.util.UUID

@Composable
fun WorkspaceRequirementsTab(
    event: CollegeEvent,
    requirements: List<RequirementItem>,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showAddModal by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("workspace_requirements_tab")
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
                    text = "Logistics & Hardware Requisitions",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "${requirements.size} critical items tracked",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Button(
                onClick = { showAddModal = true },
                colors = ButtonDefaults.buttonColors(containerColor = OpsPrimaryIndigo),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("add_requirement_btn")
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Add Item")
            }
        }

        // Requirements List
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(requirements) { req ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
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
                                    text = req.item,
                                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant
                                ) {
                                    Text(
                                        text = req.quantity,
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            StatusPill(text = req.status.displayName, color = Color(req.status.colorHex))
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Responsible: ${req.responsiblePerson} • Needed by ${req.requiredBy}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        if (req.notes.isNotBlank()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Notes: ${req.notes}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Quick status toggles
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            RequirementStatus.values().forEach { st ->
                                if (st != req.status) {
                                    SuggestionChip(
                                        onClick = { OperationsRepository.updateRequirementStatus(req.id, st) },
                                        label = { Text(st.displayName, style = MaterialTheme.typography.labelSmall) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal: Add Requirement
    if (showAddModal) {
        var item by remember { mutableStateOf("") }
        var quantity by remember { mutableStateOf("1 Unit") }
        var responsiblePerson by remember { mutableStateOf("Arjun Das") }
        var requiredBy by remember { mutableStateOf("Oct 23, 2026") }
        var notes by remember { mutableStateOf("") }
        var status by remember { mutableStateOf(RequirementStatus.PENDING) }

        Dialog(onDismissRequest = { showAddModal = false }) {
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
                        Text("Add Logistic Requirement", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
                        IconButton(onClick = { showAddModal = false }) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    }

                    OutlinedTextField(
                        value = item,
                        onValueChange = { item = it },
                        label = { Text("Item Name *") },
                        placeholder = { Text("e.g. Heavy Duty Extension Boards") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = quantity,
                        onValueChange = { quantity = it },
                        label = { Text("Quantity *") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true
                    )

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = responsiblePerson,
                            onValueChange = { responsiblePerson = it },
                            label = { Text("Responsible Person") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = requiredBy,
                            onValueChange = { requiredBy = it },
                            label = { Text("Required By Date") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            singleLine = true
                        )
                    }

                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Procurement Notes") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        maxLines = 2
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = {
                            if (item.isNotBlank()) {
                                val newReq = RequirementItem(
                                    id = "req_${UUID.randomUUID().toString().take(8)}",
                                    eventId = event.id,
                                    item = item,
                                    quantity = quantity,
                                    responsiblePerson = responsiblePerson,
                                    requiredBy = requiredBy,
                                    status = status,
                                    notes = notes
                                )
                                OperationsRepository.addRequirement(newReq)
                                showAddModal = false
                                Toast.makeText(context, "Requirement added to checklist!", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = OpsPrimaryIndigo),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Save Requirement", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
