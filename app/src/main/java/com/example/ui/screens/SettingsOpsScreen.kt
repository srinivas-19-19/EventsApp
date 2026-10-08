package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.OperationsRepository
import com.example.model.OpsUser
import com.example.model.OpsUserRole
import com.example.ui.components.StatusPill
import com.example.ui.theme.*

@Composable
fun SettingsOpsScreen(
    currentUser: OpsUser,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .testTag("settings_ops_screen")
    ) {
        Text(
            text = "Settings & Roles",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = "Configure operational permissions and switch user personas",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Current Account Profile Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(OpsPrimaryIndigo),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = currentUser.avatarInitials,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = currentUser.name,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = currentUser.email,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${currentUser.department} • ${currentUser.phone}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    StatusPill(text = currentUser.role.displayName, color = Color(currentUser.role.badgeColor))
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Switch Active Operational Role
        Text(
            text = "Switch Operational Persona",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = "Instantly experience the workspace from any team perspective:",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(10.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
        ) {
            Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                RoleSelectionRow(
                    role = OpsUserRole.EVENT_HEAD,
                    name = "Rahul Sharma",
                    desc = "Event Head (Full Tech Fest 2026 workspace controls)",
                    isSelected = currentUser.role == OpsUserRole.EVENT_HEAD,
                    onSelect = {
                        OperationsRepository.switchRole(OpsUserRole.EVENT_HEAD)
                        Toast.makeText(context, "Switched to Event Head: Rahul Sharma", Toast.LENGTH_SHORT).show()
                    }
                )

                RoleSelectionRow(
                    role = OpsUserRole.TEAM_MEMBER,
                    name = "Sreenu Gorkal",
                    desc = "Technical & Web Lead (Assigned tasks, comments & requirements)",
                    isSelected = currentUser.role == OpsUserRole.TEAM_MEMBER,
                    onSelect = {
                        OperationsRepository.switchRole(OpsUserRole.TEAM_MEMBER)
                        Toast.makeText(context, "Switched to Team Member: Sreenu Gorkal", Toast.LENGTH_SHORT).show()
                    }
                )

                RoleSelectionRow(
                    role = OpsUserRole.ADMIN,
                    name = "Dr. Eleanor Vance",
                    desc = "College Admin (All college events & global financial audit)",
                    isSelected = currentUser.role == OpsUserRole.ADMIN,
                    onSelect = {
                        OperationsRepository.switchRole(OpsUserRole.ADMIN)
                        Toast.makeText(context, "Switched to College Admin: Dr. Eleanor Vance", Toast.LENGTH_SHORT).show()
                    }
                )

                RoleSelectionRow(
                    role = OpsUserRole.FACULTY_COORDINATOR,
                    name = "Prof. Marcus Sterling",
                    desc = "Faculty Coordinator (Supervision, clearances, faculty meetings)",
                    isSelected = currentUser.role == OpsUserRole.FACULTY_COORDINATOR,
                    onSelect = {
                        OperationsRepository.switchRole(OpsUserRole.FACULTY_COORDINATOR)
                        Toast.makeText(context, "Switched to Faculty Coordinator: Prof. Marcus Sterling", Toast.LENGTH_SHORT).show()
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // System Actions
        OutlinedButton(
            onClick = onLogout,
            modifier = Modifier.fillMaxWidth().testTag("ops_logout_btn"),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = OpsAccentRose),
            border = BorderStroke(1.dp, OpsAccentRose)
        ) {
            Icon(Icons.Default.Logout, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Sign Out of Workspace")
        }

        Spacer(modifier = Modifier.height(30.dp))
    }
}

@Composable
private fun RoleSelectionRow(
    role: OpsUserRole,
    name: String,
    desc: String,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f) else Color.Transparent
        )
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            RadioButton(selected = isSelected, onClick = onSelect)
            Column(modifier = Modifier.weight(1f)) {
                Text(text = "$name (${role.displayName})", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                Text(text = desc, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
