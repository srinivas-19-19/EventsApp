package com.example.ui.screens.workspace

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
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
import com.example.model.EventDocument
import com.example.ui.components.StatusPill
import com.example.ui.theme.OpsPrimaryIndigo
import java.util.UUID

@Composable
fun WorkspaceDocumentsTab(
    event: CollegeEvent,
    documents: List<EventDocument>,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showUploadModal by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("workspace_documents_tab")
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
                    text = "Event Repository & Files",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "${documents.size} official sanction letters, invoices & assets",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Button(
                onClick = { showUploadModal = true },
                colors = ButtonDefaults.buttonColors(containerColor = OpsPrimaryIndigo),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("upload_document_btn")
            ) {
                Icon(Icons.Default.UploadFile, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Upload Document")
            }
        }

        // Documents list
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(documents) { doc ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = OpsPrimaryIndigo.copy(alpha = 0.12f),
                            modifier = Modifier.size(42.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Description, contentDescription = null, tint = OpsPrimaryIndigo, modifier = Modifier.size(22.dp))
                            }
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = doc.title,
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${doc.category} • ${doc.fileType} (${doc.fileSize})",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Uploaded by ${doc.uploadedBy} on ${doc.uploadedAt}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }

                        IconButton(
                            onClick = {
                                Toast.makeText(context, "Downloading ${doc.title}...", Toast.LENGTH_SHORT).show()
                            }
                        ) {
                            Icon(Icons.Default.Download, contentDescription = "Download", tint = OpsPrimaryIndigo)
                        }
                    }
                }
            }
        }
    }

    // Modal: Upload Document
    if (showUploadModal) {
        var title by remember { mutableStateOf("") }
        var category by remember { mutableStateOf("Permission letters") }
        var fileType by remember { mutableStateOf("PDF") }

        Dialog(onDismissRequest = { showUploadModal = false }) {
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
                        Text("Add Event Document", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
                        IconButton(onClick = { showUploadModal = false }) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    }

                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Document Title *") },
                        placeholder = { Text("e.g. Police Department NOC Requisition") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = category,
                        onValueChange = { category = it },
                        label = { Text("Category (Permission letters, Invoices, Posters)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = fileType,
                        onValueChange = { fileType = it },
                        label = { Text("File Format (PDF, PNG, DOCX)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = {
                            if (title.isNotBlank()) {
                                val newDoc = EventDocument(
                                    id = "doc_${UUID.randomUUID().toString().take(8)}",
                                    eventId = event.id,
                                    title = title,
                                    category = category,
                                    fileType = fileType,
                                    uploadedBy = OperationsRepository.currentUser.value.name,
                                    uploadedAt = "Just now",
                                    fileSize = "1.8 MB"
                                )
                                OperationsRepository.addDocument(newDoc)
                                showUploadModal = false
                                Toast.makeText(context, "Document uploaded to workspace!", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = OpsPrimaryIndigo),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Upload File", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
