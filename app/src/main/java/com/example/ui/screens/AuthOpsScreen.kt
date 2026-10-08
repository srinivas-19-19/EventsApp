package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.OperationsRepository
import com.example.model.OpsUser
import com.example.model.OpsUserRole
import com.example.ui.components.OpsLogo
import com.example.ui.theme.*

@Composable
fun AuthOpsScreen(
    onLoginSuccess: (OpsUser) -> Unit,
    modifier: Modifier = Modifier
) {
    var email by remember { mutableStateOf("rahul.sharma@college.edu") }
    var password by remember { mutableStateOf("password123") }
    var rememberMe by remember { mutableStateOf(true) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(28.dp))

        OpsLogo()

        Spacer(modifier = Modifier.height(24.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("ops_auth_card"),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "Event Operations Sign In",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Internal management portal for event leads & college administration",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(18.dp))

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("College Email / Staff ID") },
                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = OpsPrimaryIndigo) },
                    modifier = Modifier.fillMaxWidth().testTag("ops_login_email"),
                    shape = RoundedCornerShape(8.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = OpsPrimaryIndigo) },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth().testTag("ops_login_password"),
                    shape = RoundedCornerShape(8.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { rememberMe = !rememberMe }
                    ) {
                        Checkbox(checked = rememberMe, onCheckedChange = { rememberMe = it })
                        Text("Remember session", style = MaterialTheme.typography.bodySmall)
                    }
                    TextButton(onClick = { /* Demo reset */ }) {
                        Text("Forgot password?", style = MaterialTheme.typography.bodySmall, color = OpsPrimaryIndigo)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        val user = OperationsRepository.eventHeadUser
                        OperationsRepository.setCurrentUser(user)
                        onLoginSuccess(user)
                    },
                    modifier = Modifier.fillMaxWidth().testTag("ops_login_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = OpsPrimaryIndigo),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(vertical = 12.dp)
                ) {
                    Text("Access Event Workspace", fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Quick Role Switcher for seamless evaluation
        Text(
            text = "⚡ Instant Role Demo Access",
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = "Select any operational role to test specific workspace views and permissions",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
        )

        Spacer(modifier = Modifier.height(12.dp))

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            RoleCard(
                title = "Event Head (Rahul Sharma)",
                desc = "Full workspace controls for Annual Tech Fest 2026: Kanban, Budget, Team & Schedule",
                accent = OpsAccentTeal,
                onClick = {
                    OperationsRepository.switchRole(OpsUserRole.EVENT_HEAD)
                    onLoginSuccess(OperationsRepository.eventHeadUser)
                },
                testTag = "login_role_head"
            )

            RoleCard(
                title = "Team Member (Sreenu Gorkal)",
                desc = "Technical & Web Lead: Assigned tasks, task comments, logistics and meetings",
                accent = OpsPrimaryIndigo,
                onClick = {
                    OperationsRepository.switchRole(OpsUserRole.TEAM_MEMBER)
                    onLoginSuccess(OperationsRepository.teamMemberUser)
                },
                testTag = "login_role_member"
            )

            RoleCard(
                title = "College Admin (Dr. Eleanor Vance)",
                desc = "Executive college dashboard: All events, total budgets, cross-department audits",
                accent = Color(0xFF4338CA),
                onClick = {
                    OperationsRepository.switchRole(OpsUserRole.ADMIN)
                    onLoginSuccess(OperationsRepository.adminUser)
                },
                testTag = "login_role_admin"
            )

            RoleCard(
                title = "Faculty Coordinator (Prof. Marcus Sterling)",
                desc = "Supervisory overview: Pending approvals, budget clearances, faculty meetings",
                accent = OpsAccentAmber,
                onClick = {
                    OperationsRepository.switchRole(OpsUserRole.FACULTY_COORDINATOR)
                    onLoginSuccess(OperationsRepository.facultyUser)
                },
                testTag = "login_role_faculty"
            )
        }

        Spacer(modifier = Modifier.height(30.dp))
    }
}

@Composable
private fun RoleCard(
    title: String,
    desc: String,
    accent: Color,
    onClick: () -> Unit,
    testTag: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag(testTag)
            .clickable { onClick() },
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, accent.copy(alpha = 0.35f))
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(accent)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = desc,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                imageVector = Icons.Default.ArrowForwardIos,
                contentDescription = null,
                tint = accent,
                modifier = Modifier.size(14.dp)
            )
        }
    }
}
