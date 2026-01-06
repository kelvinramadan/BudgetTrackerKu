package com.example.budgettrackerku.ui.main

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import com.example.budgettrackerku.R
import com.example.budgettrackerku.data.model.Transaction
import com.example.budgettrackerku.ui.common.getCategoryColor
import com.example.budgettrackerku.ui.common.getCategoryIcon
import com.example.budgettrackerku.ui.common.getCategoryNameResId
import com.example.budgettrackerku.ui.theme.*
import com.example.budgettrackerku.viewmodel.BudgetViewModel
import java.text.SimpleDateFormat
import java.util.*

val IconOrange = Color(0xFFFF5722) // Top-level definition

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionScreen(viewModel: BudgetViewModel, onNavigateToProfile: () -> Unit) {
    val transactions by viewModel.transactions.collectAsState()
    // IconOrange removed from here
    
    // String resources for filters
    val strToday = stringResource(R.string.filter_today)
    val str7Days = stringResource(R.string.filter_7_days)
    val str30Days = stringResource(R.string.filter_30_days)
    val strAllTime = stringResource(R.string.filter_all_time)
    
    val strAllTypes = stringResource(R.string.filter_type_all)
    val strIncome = stringResource(R.string.filter_type_income)
    val strExpense = stringResource(R.string.filter_type_expense)

    // Filter States (Use resource strings as default)
    var selectedTimeFilter by remember { mutableStateOf(str30Days) }
    var selectedTypeFilter by remember { mutableStateOf(strAllTypes) }
    
    // Process Data
    val processedData by remember(transactions, selectedTimeFilter, selectedTypeFilter, strToday, str7Days, str30Days, strAllTime, strAllTypes, strIncome, strExpense) {
        derivedStateOf {
            val now = System.currentTimeMillis()
            val oneDayAgo = now - (24 * 60 * 60 * 1000)
            val sevenDaysAgo = now - (7L * 24 * 60 * 60 * 1000)
            val thirtyDaysAgo = now - (30L * 24 * 60 * 60 * 1000)
            
            val filtered = transactions.filter { transaction ->
                // Time Filter
                val matchesTime = when (selectedTimeFilter) {
                    strToday -> transaction.date >= oneDayAgo
                    str7Days -> transaction.date >= sevenDaysAgo
                    str30Days -> transaction.date >= thirtyDaysAgo
                    else -> true // strAllTime or others
                }
                
                // Type Filter
                val matchesType = when (selectedTypeFilter) {
                    strIncome -> transaction.type.equals("INCOME", ignoreCase = true)
                    strExpense -> transaction.type.equals("EXPENSE", ignoreCase = true)
                    else -> true
                }
                
                matchesTime && matchesType
            }.sortedByDescending { it.date }
            
            // Group by Month Year
            filtered.groupBy { 
                SimpleDateFormat("MMM yyyy", Locale.getDefault()).format(Date(it.date)) 
            }
        }
    }

    // MaterialTheme wrapper removed to allow global theme
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            BudgetTrackerTopBar(
                title = stringResource(R.string.transaction_history_title),
                navigationIcon = Icons.Default.AccountCircle,
                onMenuClick = onNavigateToProfile
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
                // Filters
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    FilterDropdown(
                        options = listOf(strToday, str7Days, str30Days, strAllTime),
                        selectedOption = selectedTimeFilter,
                        onOptionSelected = { selectedTimeFilter = it },
                        modifier = Modifier.weight(1f)
                    )
                    FilterDropdown(
                        options = listOf(strAllTypes, strIncome, strExpense),
                        selectedOption = selectedTypeFilter,
                        onOptionSelected = { selectedTypeFilter = it },
                        modifier = Modifier.weight(1f)
                    )
                }



                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 1.dp)

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    processedData.forEach { (month, monthTransactions) ->
                        // Calculate Totals per month
                        val totalIncome = monthTransactions.filter { it.type.equals("INCOME", ignoreCase = true) }.sumOf { it.amount }
                        val totalExpense = monthTransactions.filter { it.type.equals("EXPENSE", ignoreCase = true) }.sumOf { it.amount }
                        
                        item {
                            MonthHeader(month, totalExpense, totalIncome)
                        }
                        
                        items(monthTransactions) { transaction ->
                            DetailedTransactionItem(transaction)
                        }
                    }
                }
            }
        }
    //} // Removed closing brace for MaterialTheme
}

@Composable
fun FilterDropdown(
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val rotationState by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        label = "Arrow Rotation"
    )

    Box(modifier = modifier) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(42.dp)
                .clickable { expanded = true },
            shape = RoundedCornerShape(12.dp), // More rounded
            color = MaterialTheme.colorScheme.surface, // Changed to White
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline) // Adaptive outline
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.padding(horizontal = 12.dp)
            ) {
                Text(
                    text = selectedOption, 
                    color = MaterialTheme.colorScheme.onSurfaceVariant, 
                    fontSize = 12.sp, 
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown, 
                    contentDescription = null, 
                    tint = Color(0xFFB0B0B0), 
                    modifier = Modifier
                        .size(18.dp)
                        .graphicsLayer { rotationZ = rotationState } // Animated rotation
                )
            }
        }
        
        MaterialTheme(
            shapes = MaterialTheme.shapes.copy(extraSmall = RoundedCornerShape(12.dp))
        ) {
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.surface) // Slightly darker for menu
                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = { 
                            Text(
                                option, 
                                color = if (option == selectedOption) IconOrange else MaterialTheme.colorScheme.onSurface, // Highlight selected
                                fontWeight = if (option == selectedOption) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 13.sp
                            ) 
                        },
                        onClick = {
                            onOptionSelected(option)
                            expanded = false
                        },
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun MonthHeader(month: String, expense: Double, income: Double) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface) // Adaptive Surface Color
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = month,
            color = MaterialTheme.colorScheme.onSurface, // Adaptive Text Color
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
        Column(horizontalAlignment = Alignment.End) {
             if (expense > 0) Text(text = stringResource(R.string.balance_out_prefix) + "Rp ${formatCurrency(expense)}", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 10.sp)
             if (income > 0) Text(text = stringResource(R.string.balance_in_prefix) + "Rp ${formatCurrency(income)}", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 10.sp)
        }
    }
}

@Composable
fun DetailedTransactionItem(transaction: Transaction) {
    val isExpense = transaction.type.equals("EXPENSE", ignoreCase = true)
    
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .clickable { },
            verticalAlignment = Alignment.Top
        ) {
            // Icon
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(getCategoryColor(transaction.category).copy(alpha = 0.2f)), // Transparent bg for icon
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = getCategoryIcon(transaction.category),
                    contentDescription = null,
                    tint = getCategoryColor(transaction.category),
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

                // Content
                Column(modifier = Modifier.weight(1f)) {
                     Row(
                         modifier = Modifier.fillMaxWidth(),
                         horizontalArrangement = Arrangement.SpaceBetween
                     ) {
                         // Title (Now Category)
                         Text(
                             text = stringResource(getCategoryNameResId(transaction.category)),
                             color = MaterialTheme.colorScheme.onSurface,
                             fontSize = 16.sp,
                             fontWeight = FontWeight.Medium
                         )
                         // Amount
                         val prefix = if (isExpense) "-" else "+"
                         Text(
                             text = "$prefix Rp ${formatCurrency(transaction.amount)}",
                             color = if (isExpense) RedExpense else GreenIncome, 
                             fontSize = 16.sp,
                             fontWeight = FontWeight.Bold
                         )
                     }
                     
                     Spacer(modifier = Modifier.height(4.dp))
                     
                     // Date
                     val dateFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
                     Text(
                         text = dateFormat.format(Date(transaction.date)),
                         color = MaterialTheme.colorScheme.onSurfaceVariant, // Lighter gray for better visibility
                         fontSize = 12.sp
                     )
                }
        }
        // Divider
        HorizontalDivider(
            color = MaterialTheme.colorScheme.outlineVariant, // Visible Dark Grey divider
            thickness = 1.dp, 
            modifier = Modifier.padding(start = 72.dp) // Indented divider
        )
    }
}
