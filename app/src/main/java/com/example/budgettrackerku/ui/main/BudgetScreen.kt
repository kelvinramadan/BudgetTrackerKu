package com.example.budgettrackerku.ui.main

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.budgettrackerku.ui.common.getCategoryColor
import com.example.budgettrackerku.ui.common.getCategoryIcon
import com.example.budgettrackerku.ui.theme.*
import com.example.budgettrackerku.viewmodel.BudgetViewModel
import androidx.compose.ui.res.stringResource
import com.example.budgettrackerku.R
import com.example.budgettrackerku.util.CategoryUtils
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

fun formatInputWithDots(input: String): String {
    if (input.isEmpty()) return ""
    val cleanString = input.replace(".", "").replace(",", "")
    val longVal = cleanString.toLongOrNull() ?: return input
    val symbols = DecimalFormatSymbols(Locale("id", "ID"))
    symbols.groupingSeparator = '.'
    val decimalFormat = DecimalFormat("#,###", symbols)
    return decimalFormat.format(longVal)
}

@Composable
fun BudgetScreen(
    viewModel: BudgetViewModel, 
    onNavigateToProfile: () -> Unit,
    onNavigateToNotifications: () -> Unit = {}
) {
    val expenseBreakdown by viewModel.expenseBreakdown.collectAsState()
    val budgets by viewModel.budgets.collectAsState()
    
    // Combine all known categories (from expenses or existing budgets)
    val allCategories = (expenseBreakdown.keys + budgets.keys).distinct().sorted()

    // Dialog State
    var showSetBudgetDialog by remember { mutableStateOf(false) }
    var selectedCategoryForBudget by remember { mutableStateOf("") }
    var tempBudgetAmount by remember { mutableStateOf("") }
    val context = androidx.compose.ui.platform.LocalContext.current

    if (showSetBudgetDialog) {
        AlertDialog(
            onDismissRequest = { showSetBudgetDialog = false },
            title = { 
                val catLabel = stringResource(CategoryUtils.getCategoryLabel(selectedCategoryForBudget))
                Text(stringResource(R.string.set_budget_for, catLabel), color = MaterialTheme.colorScheme.onSurface) 
            },
            text = {
                OutlinedTextField(
                    value = tempBudgetAmount,
                    onValueChange = { newValue ->
                        if (newValue.all { it.isDigit() || it == '.' }) {
                            val unformatted = newValue.replace(".", "")
                            if (unformatted.all { it.isDigit() }) {
                                tempBudgetAmount = formatInputWithDots(unformatted)
                            }
                        }
                    },
                    label = { Text(stringResource(R.string.limit_amount_label)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                    ),
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    val amount = tempBudgetAmount.replace(".", "").toDoubleOrNull() ?: 0.0
                    viewModel.setBudget(selectedCategoryForBudget, amount)
                    showSetBudgetDialog = false
                    android.widget.Toast.makeText(context, context.getString(R.string.budget_saved_toast, selectedCategoryForBudget), android.widget.Toast.LENGTH_SHORT).show()
                }) {
                    Text(stringResource(R.string.save), color = BluePrimary)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSetBudgetDialog = false }) {
                    Text(stringResource(R.string.cancel), color = Color.Gray)
                }
            },
            containerColor = MaterialTheme.colorScheme.surface
        )
    }

    // Theme Colors Removed - Use MaterialTheme.colorScheme directly

    // MaterialTheme wrapper removed
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            BudgetTrackerTopBar(
                title = stringResource(R.string.monthly_budget_title),
                navigationIcon = Icons.Default.AccountCircle,
                onMenuClick = onNavigateToProfile,
                onNotificationClick = onNavigateToNotifications
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
        ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    if (allCategories.isEmpty()) {
                        Text(
                            stringResource(R.string.no_expenses_budgets), 
                            color = MaterialTheme.colorScheme.onSurfaceVariant, 
                            modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 32.dp)
                        )
                    } else {
                        allCategories.forEach { category ->
                            val spent = expenseBreakdown[category] ?: 0.0
                            val limit = budgets[category] ?: 0.0 // 0 means no limit set
                            
                            BudgetItem(
                                category = category,
                                spent = spent,
                                limit = limit,
                                    onSetLimit = {
                                    selectedCategoryForBudget = category
                                    tempBudgetAmount = if (limit > 0) formatInputWithDots(limit.toInt().toString()) else ""
                                    showSetBudgetDialog = true
                                }
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }
                }
                
                // Bottom Spacer for FAB/Nav bar
                Spacer(modifier = Modifier.height(100.dp))
            }
        }
    }


@Composable
fun BudgetItem(
    category: String, 
    spent: Double, 
    limit: Double, 
    onSetLimit: () -> Unit
) {
    val progress = if (limit > 0) (spent / limit).toFloat().coerceIn(0f, 1f) else 0f
    val isOverBudget = limit > 0 && spent > limit
    val isWarning = limit > 0 && spent > limit * 0.8 && !isOverBudget
    
    val statusColor = when {
        isOverBudget -> Color(0xFFFF5252) // Red
        isWarning -> Color(0xFFFFC107) // Amber
        else -> Color(0xFF4CAF50) // Green
    }

    Card(
        modifier = Modifier.fillMaxWidth().clickable { onSetLimit() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: Icon + Name + Edit
            Row(
                verticalAlignment = Alignment.CenterVertically, 
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(getCategoryColor(category).copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = getCategoryIcon(category),
                        contentDescription = null,
                        tint = getCategoryColor(category),
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    val displayCat = stringResource(CategoryUtils.getCategoryLabel(category))
                    Text(displayCat, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    if (limit > 0) {
                        val remaining = limit - spent
                        if (remaining >= 0) {
                            Text(stringResource(R.string.budget_left, formatCurrency(remaining)), fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        } else {
                            Text(stringResource(R.string.budget_over, formatCurrency(spent - limit)), fontSize = 12.sp, color = Color.Red)
                        }
                    } else {
                        Text(stringResource(R.string.no_limit_set), fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Icon(Icons.Default.Edit, contentDescription = "Edit", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Progress Bar
            if (limit > 0) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(formatCurrency(spent), fontSize = 12.sp, color = statusColor, fontWeight = FontWeight.Bold)
                        Text(formatCurrency(limit), fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                        color = statusColor,
                        trackColor = Color(0xFF333333),
                    )
                    
                    if (isOverBudget) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = Color.Red, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(stringResource(R.string.budget_exceeded_msg), color = Color.Red, fontSize = 12.sp)
                        }
                    }
                }
            } else {
                 Text(stringResource(R.string.limit_set_tap), fontSize = 12.sp, color = BluePrimary)
            }
        }
    }
}
