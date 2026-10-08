package com.example.ui.screens.workspace

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.OperationsRepository
import com.example.model.*
import com.example.ui.components.StatWidget
import com.example.ui.components.StatusPill
import com.example.ui.theme.*
import java.util.UUID

@Composable
fun WorkspaceBudgetTab(
    event: CollegeEvent,
    expenses: List<ExpenseItem>,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showAddExpenseModal by remember { mutableStateOf(false) }

    val totalSpent = expenses.sumOf { it.amount }
    val remaining = event.estimatedBudget - totalSpent
    val pctUsed = (totalSpent / event.estimatedBudget).toFloat().coerceIn(0f, 1f)

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("workspace_budget_tab")
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
                    text = "Event Budget & Expenses Ledger",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "Track itemized expenditures, vendor invoices & approvals",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Button(
                onClick = { showAddExpenseModal = true },
                colors = ButtonDefaults.buttonColors(containerColor = OpsPrimaryIndigo),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("add_expense_btn")
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Add Expense")
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Budget Summary Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("ESTIMATED BUDGET", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp), color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("₹${String.format("%,.0f", event.estimatedBudget)}", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black))
                            }
                            Column {
                                Text("SPENT", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp), color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("₹${String.format("%,.0f", totalSpent)}", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black), color = OpsAccentTeal)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("REMAINING", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp), color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("₹${String.format("%,.0f", remaining)}", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black), color = OpsPrimaryIndigo)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        LinearProgressIndicator(
                            progress = { pctUsed },
                            modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                            color = if (pctUsed > 0.9f) OpsAccentRose else OpsAccentTeal,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )

                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "${(pctUsed * 100).toInt()}% of allocated college funds utilized",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Category Breakdown Title
            item {
                Text(
                    text = "Expense History (${expenses.size} entries)",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }

            // Expense Items List
            items(expenses) { exp ->
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
                            color = OpsAccentTeal.copy(alpha = 0.12f),
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.ReceiptLong, contentDescription = null, tint = OpsAccentTeal, modifier = Modifier.size(20.dp))
                            }
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = exp.description,
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${exp.category.displayName} • Paid by ${exp.paidBy} on ${exp.date}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "₹${String.format("%,.0f", exp.amount)}",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            StatusPill(text = exp.paymentStatus, color = OpsAccentEmerald)
                        }
                    }
                }
            }
        }
    }

    // Modal: Add Expense
    if (showAddExpenseModal) {
        var desc by remember { mutableStateOf("") }
        var amount by remember { mutableStateOf("") }
        var category by remember { mutableStateOf(ExpenseCategory.EQUIPMENT) }
        var paidBy by remember { mutableStateOf("Rahul Sharma") }
        var date by remember { mutableStateOf("Oct 18, 2026") }

        Dialog(onDismissRequest = { showAddExpenseModal = false }) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .clip(RoundedCornerShape(16.dp))
                    .testTag("add_expense_modal"),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 8.dp
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Add Event Expense", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
                        IconButton(onClick = { showAddExpenseModal = false }) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    }

                    OutlinedTextField(
                        value = desc,
                        onValueChange = { desc = it },
                        label = { Text("Expense Description *") },
                        placeholder = { Text("e.g. Wireless collar mic rental") },
                        modifier = Modifier.fillMaxWidth().testTag("input_expense_desc"),
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = amount,
                        onValueChange = { amount = it },
                        label = { Text("Amount (₹) *") },
                        placeholder = { Text("e.g. 3500") },
                        modifier = Modifier.fillMaxWidth().testTag("input_expense_amt"),
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true
                    )

                    // Category dropdown
                    Column {
                        Text("Expense Category", style = MaterialTheme.typography.labelSmall)
                        var catExpanded by remember { mutableStateOf(false) }
                        Box {
                            OutlinedButton(
                                onClick = { catExpanded = true },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(category.displayName)
                            }
                            DropdownMenu(expanded = catExpanded, onDismissRequest = { catExpanded = false }) {
                                ExpenseCategory.values().forEach { c ->
                                    DropdownMenuItem(
                                        text = { Text(c.displayName) },
                                        onClick = {
                                            category = c
                                            catExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = paidBy,
                            onValueChange = { paidBy = it },
                            label = { Text("Paid By") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = date,
                            onValueChange = { date = it },
                            label = { Text("Date") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            singleLine = true
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = {
                            val amt = amount.toDoubleOrNull() ?: 0.0
                            if (desc.isNotBlank() && amt > 0) {
                                val newExp = ExpenseItem(
                                    id = "exp_${UUID.randomUUID().toString().take(8)}",
                                    eventId = event.id,
                                    description = desc,
                                    category = category,
                                    amount = amt,
                                    date = date,
                                    paidBy = paidBy
                                )
                                OperationsRepository.addExpense(newExp)
                                showAddExpenseModal = false
                                Toast.makeText(context, "Expense added and budget updated!", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.fillMaxWidth().testTag("save_expense_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = OpsPrimaryIndigo),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Record Expense", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
