package com.example.budgettrackerku.ui.main

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.budgettrackerku.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import com.example.budgettrackerku.R
import com.example.budgettrackerku.util.CategoryUtils

// ...

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionScreen(
    navController: NavController,
    viewModel: com.example.budgettrackerku.viewmodel.BudgetViewModel
) {
    var selectedTab by remember { mutableStateOf("Expense") }
    var amount by remember { mutableStateOf("0.00") }
    var note by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("") }
    var showCategorySheet by remember { mutableStateOf(false) }
    
    // Theme colors based on selection
    val primaryColor = when(selectedTab) {
        "Income" -> Color(0xFF2196F3) // Blue
        "Expense" -> Color(0xFFFF5722) // Orange
        else -> Color.Gray
    }

    if (showCategorySheet) {
        ModalBottomSheet(
            onDismissRequest = { showCategorySheet = false },
            containerColor = MaterialTheme.colorScheme.surface,
            dragHandle = { BottomSheetDefaults.DragHandle(color = MaterialTheme.colorScheme.onSurfaceVariant) }
        ) {
            CategorySelectionSheet(
                transactionType = selectedTab,
                onCategorySelected = { category ->
                    selectedCategory = category
                    showCategorySheet = false
                },
                onClose = { showCategorySheet = false }
            )
        }
    }

    Scaffold(
        topBar = {
            BudgetTrackerTopBar(
                title = selectedTab,
                navigationIcon = Icons.AutoMirrored.Filled.ArrowBack,
                onMenuClick = { navController.popBackStack() },
                showNotificationIcon = false
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .background(MaterialTheme.colorScheme.background)
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            
            // Tabs
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TransactionTab(
                    text = stringResource(R.string.income), 
                    isSelected = selectedTab == "Income",
                    activeColor = Color(0xFF2196F3),
                    modifier = Modifier.weight(1f),
                    onClick = { selectedTab = "Income" }
                )
                TransactionTab(
                    text = stringResource(R.string.expense), 
                    isSelected = selectedTab == "Expense",
                    activeColor = Color(0xFFFF5722),
                    modifier = Modifier.weight(1f),
                    onClick = { selectedTab = "Expense" }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Form Fields Container
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .background(MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(12.dp))
                    .padding(16.dp)
            ) {
                // Date
                val dateStr = SimpleDateFormat("M/d/yy (EEE) HH:mm", Locale.getDefault()).format(Date())
                RowItem(label = stringResource(R.string.date_label), value = dateStr)



                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 1.dp)


                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                        Text(stringResource(R.string.amount_label), color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.width(80.dp))
                    Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "IDR ", 
                            color = MaterialTheme.colorScheme.onSurface, 
                            fontSize = 20.sp, 
                            fontWeight = FontWeight.Bold
                        )
                        androidx.compose.foundation.text.BasicTextField(
                            value = amount,
                            onValueChange = { input ->
                                val cleanInput = input.filter { it.isDigit() }
                                if (cleanInput.isNotEmpty()) {
                                    try {
                                        val parsed = cleanInput.toLong()
                                        val formatted = java.text.NumberFormat.getNumberInstance(java.util.Locale("id", "ID")).format(parsed)
                                        amount = formatted
                                    } catch (e: Exception) {
                                        // Handle overflow or errors by keeping old value or doing nothing
                                    }
                                } else {
                                    amount = ""
                                }
                            },
                            textStyle = androidx.compose.ui.text.TextStyle(
                                color = MaterialTheme.colorScheme.onSurface, 
                                fontSize = 20.sp, 
                                fontWeight = FontWeight.Bold
                            ),
                            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                                keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 1.dp)


                // For logic, selectedCategory is English/Key. For Display we need to map it.
                val displayCat = if (selectedCategory.isNotEmpty()) stringResource(CategoryUtils.getCategoryLabel(selectedCategory)) else ""
                RowItem(
                    label = stringResource(R.string.category_label), 
                    value = displayCat, 
                    placeholder = stringResource(R.string.select_category_placeholder),
                    onClick = { showCategorySheet = true }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Description Area
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Text(stringResource(R.string.description_label), color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                   modifier = Modifier.fillMaxWidth(),
                   verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier.weight(1f)
                    ) {
                         BasicTextField(
                            value = description,
                            onValueChange = { description = it },
                            textStyle = LocalTextStyle.current.copy(color = MaterialTheme.colorScheme.onSurface),
                            modifier = Modifier.fillMaxWidth()
                        )
                        if (description.isEmpty()) {
                            HorizontalDivider(color = MaterialTheme.colorScheme.onSurfaceVariant, thickness = 1.dp, modifier = Modifier.padding(top = 20.dp))
                        }
                    }
                    IconButton(onClick = {}) {
                        Icon(Icons.Default.CameraAlt, contentDescription = "Camera", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            
            Spacer(modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.height(32.dp))

            // Buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Button(
                    onClick = {
                        val cleanAmount = amount.filter { it.isDigit() }.toDoubleOrNull() ?: 0.0
                        viewModel.addTransaction(
                            title = description,
                            amount = cleanAmount,
                            type = selectedTab.uppercase(),
                            category = if (selectedCategory.isNotEmpty()) selectedCategory else "Other"
                        )
                        navController.popBackStack()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(stringResource(R.string.save), color = Color.White, fontWeight = FontWeight.Bold)
                }
                
                OutlinedButton(
                    onClick = {
                        amount = "0"
                        description = ""
                        selectedCategory = ""
                    },
                    modifier = Modifier
                        .weight(0.5f)
                        .height(50.dp),
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onSurface)
                ) {
                    Text(stringResource(R.string.reset))
                }
            }
        }
    }
}

@Composable
fun TransactionTab(
    text: String, 
    isSelected: Boolean, 
    activeColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val borderColor = if (isSelected) activeColor else Color.Transparent
    val textColor = if (isSelected) borderColor else MaterialTheme.colorScheme.onSurfaceVariant

    Box(
        modifier = modifier
            .height(40.dp)
            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
            .then(if(isSelected) Modifier.border(1.dp, borderColor, RoundedCornerShape(8.dp)) else Modifier)
            .clickable { onClick() }
            .background(if (isSelected) MaterialTheme.colorScheme.surface else Color.Transparent, RoundedCornerShape(8.dp)), // Slight highlight
        contentAlignment = Alignment.Center
    ) {
        Text(text, color = textColor, fontWeight = if(isSelected) FontWeight.Bold else FontWeight.Normal)
    }
}

@Composable
fun RowItem(label: String, value: String, placeholder: String = "", onClick: () -> Unit = {}) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
            .clickable { onClick() },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.width(80.dp))
        if (value.isNotEmpty()) {
             Text(value, color = MaterialTheme.colorScheme.onSurface)
        } else {
             Text(placeholder, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun BasicTextField(
    value: String,
    onValueChange: (String) -> Unit,
    textStyle: androidx.compose.ui.text.TextStyle,
    modifier: Modifier = Modifier
) {
    androidx.compose.foundation.text.BasicTextField(
        value = value,
        onValueChange = onValueChange,
        textStyle = textStyle,
        modifier = modifier
    )
}

@Composable
fun CategorySelectionSheet(
    transactionType: String,
    onCategorySelected: (String) -> Unit, 
    onClose: () -> Unit
) {
    val expenseCategories = listOf(
        CategoryItem("Food", Icons.Default.Fastfood),
        CategoryItem("Social Life", Icons.Default.Groups),
        CategoryItem("Pets", Icons.Default.Pets),
        CategoryItem("Transport", Icons.Default.DirectionsCar),
        CategoryItem("Culture", Icons.Default.TheaterComedy),
        CategoryItem("Household", Icons.Default.Home),
        CategoryItem("Apparel", Icons.Default.Checkroom),
        CategoryItem("Beauty", Icons.Default.Face),
        CategoryItem("Health", Icons.Default.FitnessCenter),
        CategoryItem("Education", Icons.Default.School),
        CategoryItem("Gift", Icons.Default.CardGiftcard),
        CategoryItem("Other", Icons.Default.MoreHoriz)
    )

    val incomeCategories = listOf(
        CategoryItem("Allowance", Icons.Default.Savings),
        CategoryItem("Salary", Icons.Default.Work),
        CategoryItem("Petty cash", Icons.Default.AttachMoney),
        CategoryItem("Bonus", Icons.Default.EmojiEvents),
        CategoryItem("Other", Icons.Default.MoreHoriz)
    )

    val categories = if (transactionType == "Income") incomeCategories else expenseCategories

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(stringResource(R.string.category_label), color = MaterialTheme.colorScheme.onSurface, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Row {
                IconButton(onClick = { /* Edit mode */ }) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = MaterialTheme.colorScheme.onSurface)
                }
                IconButton(onClick = onClose) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = MaterialTheme.colorScheme.onSurface)
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(4),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(categories) { category ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.clickable { onCategorySelected(category.name) }
                ) {
                    val categoryColor = com.example.budgettrackerku.ui.common.getCategoryColor(category.name)
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .background(categoryColor.copy(alpha = 0.2f), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(category.icon, contentDescription = category.name, tint = categoryColor)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        stringResource(com.example.budgettrackerku.ui.common.getCategoryNameResId(category.name)), 
                        color = MaterialTheme.colorScheme.onSurfaceVariant, 
                        fontSize = 12.sp, 
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(32.dp))
    }
}

data class CategoryItem(val name: String, val icon: ImageVector)
