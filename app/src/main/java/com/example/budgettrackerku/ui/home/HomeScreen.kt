package com.example.budgettrackerku.ui.home

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.budgettrackerku.data.model.Transaction
import com.example.budgettrackerku.ui.common.*
import com.example.budgettrackerku.ui.main.BudgetTrackerTopBar
import com.example.budgettrackerku.ui.main.formatCurrency
import com.example.budgettrackerku.ui.theme.*
import com.example.budgettrackerku.viewmodel.BudgetViewModel
import java.text.SimpleDateFormat
import com.example.budgettrackerku.R
import com.example.budgettrackerku.util.LanguageUtils
import androidx.compose.ui.res.stringResource
import java.util.*
import kotlin.math.atan2

@Composable
fun HomeScreen(
    viewModel: BudgetViewModel,
    onNavigateToReports: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToNotifications: () -> Unit,
    onNavigateToSettings: () -> Unit // New callback
) {
    val totalExpense by viewModel.totalExpense.collectAsState()
    val totalIncome by viewModel.totalIncome.collectAsState() 
    val transactions by viewModel.transactions.collectAsState()
    val userName by viewModel.userName.collectAsState() 
    val totalBalance by viewModel.totalBalance.collectAsState()
    val expenseBreakdown by viewModel.expenseBreakdown.collectAsState()
    val incomeBreakdown by viewModel.incomeBreakdown.collectAsState()
    
    // New States for Balance Trend
    val balanceTrendPercentage by viewModel.balanceTrendPercentage.collectAsState()
    val balanceHistory by viewModel.balanceHistory.collectAsState()

    // Locked Language Dialog removed, logic moved to SettingsScreen


    // Theme Colors matching ReportScreen
    // val BackgroundBlack = Color(0xFF121212) // Removed: Use MaterialTheme.colorScheme.background
    // val SurfaceDark = Color(0xFF1E1E1E) // Removed: Use MaterialTheme.colorScheme.surface

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            BudgetTrackerTopBar(
                title = stringResource(R.string.home_title),
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
                    .verticalScroll(rememberScrollState())
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    AccountsSection(userName, totalBalance, onSettingsClick = onNavigateToSettings)
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    BalanceStructureSection(
                        totalBalance = totalBalance,
                        totalExpense = totalExpense,
                        totalIncome = totalIncome,
                        expenseBreakdown = expenseBreakdown,
                        incomeBreakdown = incomeBreakdown,
                        onShowMoreClick = onNavigateToReports
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                        HistorySection(transactions, onNavigateToHistory)
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    BalanceTrendSection(totalBalance, balanceTrendPercentage, balanceHistory)
                    
                    Spacer(modifier = Modifier.height(80.dp)) 
                }
            }
    }
}
//...
@Composable
fun BalanceTrendSection(balance: Double, trendPercentage: Double, history: List<Double>) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(stringResource(R.string.balance_trend), color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                Icon(Icons.Default.MoreVert, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            
            Text(stringResource(R.string.today), color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 10.sp)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("IDR " + formatCurrency(balance), color = MaterialTheme.colorScheme.onSurface, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Column(horizontalAlignment = Alignment.End) {
                    Text(stringResource(R.string.vs_past_period), color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 10.sp)
                    val percentageString = String.format("%.0f%%", trendPercentage) // No decimals
                    val trendColor = if (trendPercentage >= 0) GreenIncome else RedExpense
                    Text(percentageString, color = trendColor, fontWeight = FontWeight.Bold)
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Dynamic Line Chart
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
            ) {
                if (history.isNotEmpty() && history.maxOrNull() != history.minOrNull()) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val path = androidx.compose.ui.graphics.Path()
                        val width = size.width
                        val height = size.height
                        
                        val minVal = history.minOrNull() ?: 0.0
                        val maxVal = history.maxOrNull() ?: 1.0
                        val range = maxVal - minVal
                        
                        // Map points to canvas coordinates
                        history.forEachIndexed { index, value ->
                            val x = (index.toFloat() / (history.size - 1)) * width
                            // Y is inverted (0 is top), so we do 1 - normalizedValue
                            val normalizedValue = ((value - minVal) / range).toFloat()
                            val y = height * (1 - normalizedValue)
                            
                            if (index == 0) {
                                path.moveTo(x, y)
                            } else {
                                // Simple line for now, could use cubicTo for smoothing if desired
                                val prevX = ((index - 1).toFloat() / (history.size - 1)) * width
                                val prevVal = history[index - 1]
                                val prevNormalized = ((prevVal - minVal) / range).toFloat()
                                val prevY = height * (1 - prevNormalized)
                                
                                // Cubic smoothing
                                val controlPoint1X = prevX + (x - prevX) / 2
                                val controlPoint1Y = prevY
                                val controlPoint2X = prevX + (x - prevX) / 2
                                val controlPoint2Y = y
                                
                                path.cubicTo(controlPoint1X, controlPoint1Y, controlPoint2X, controlPoint2Y, x, y)
                            }
                        }
                        
                        drawPath(
                            path = path,
                            color = BlueButton,
                            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 5f)
                        )
                        
                        // Filled area
                        path.lineTo(width, height)
                        path.lineTo(0f, height)
                        path.close()
                        
                        drawPath(
                            path = path,
                            brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                                colors = listOf(BlueButton.copy(alpha = 0.3f), Color.Transparent)
                            )
                        )
                    }
                } else {
                     // Fallback if not enough data or flat line
                     Text(
                         stringResource(R.string.not_enough_data), 
                         color = MaterialTheme.colorScheme.onSurfaceVariant, 
                         fontSize = 12.sp, 
                         modifier = Modifier.align(Alignment.Center)
                     )
                }
            }
        }
    }
}

@Composable
fun HomeTabs() {
    var selectedIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf(
        androidx.compose.ui.res.stringResource(com.example.budgettrackerku.R.string.tab_accounts),
        androidx.compose.ui.res.stringResource(com.example.budgettrackerku.R.string.tab_budgets_goals)
    )
    
    TabRow(
        selectedTabIndex = selectedIndex,
        containerColor = Color(0xFF121212),
        contentColor = TextWhite,
        indicator = { tabPositions ->
            TabRowDefaults.SecondaryIndicator(
                Modifier.tabIndicatorOffset(tabPositions[selectedIndex]),
                color = TextWhite
            )
        },
        divider = {}
    ) {
        tabs.forEachIndexed { index, title ->
            Tab(
                selected = selectedIndex == index,
                onClick = { selectedIndex = index },
                text = { 
                    Text(
                        title, 
                        color = if (selectedIndex == index) TextWhite else TextGray,
                        fontWeight = if (selectedIndex == index) FontWeight.Bold else FontWeight.Normal
                    ) 
                }
            )
            }
        }

}

@Composable
fun AccountsSection(userName: String, totalBalance: Double, onSettingsClick: () -> Unit) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(stringResource(R.string.welcome_back, userName), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
            IconButton(onClick = onSettingsClick) {
                Icon(Icons.Default.Settings, contentDescription = "Manage", tint = BlueButton)
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Card(
            colors = CardDefaults.cardColors(containerColor = BlueButton),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth().height(70.dp).clickable {}
        ) {
           Column(
               modifier = Modifier.fillMaxSize().padding(12.dp),
               verticalArrangement = Arrangement.SpaceBetween
           ) {
               Text(stringResource(R.string.cash), color = Color.White, style = MaterialTheme.typography.bodySmall)
               Text("IDR " + formatCurrency(totalBalance), color = Color.White, fontWeight = FontWeight.Bold)
           }
            }
        }
    }


@Composable
fun BalanceStructureSection(
    totalBalance: Double,
    totalExpense: Double,
    totalIncome: Double,
    expenseBreakdown: Map<String, Double>,
    incomeBreakdown: Map<String, Double>,
    onShowMoreClick: () -> Unit
) {
    var selectedCategory by remember { mutableStateOf<Pair<String, Double>?>(null) }
    
    // Merge data for chart: Income (Green) + Expenses (Colors)
    val chartData = remember(expenseBreakdown, incomeBreakdown) {
         expenseBreakdown.entries.map { it.key to it.value } + 
         incomeBreakdown.entries.map { it.key to it.value }
    }
    val totalVolume = totalExpense + totalIncome

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {

                Text(stringResource(R.string.balance_structures), color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold) // Title changed
                Icon(Icons.Default.MoreVert, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            
            Text("LAST 30 DAYS", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 10.sp)
            Spacer(modifier = Modifier.height(4.dp))
            // Display Total Balance to match ReportScreen
            Text("IDR " + formatCurrency(totalBalance), color = MaterialTheme.colorScheme.onSurface, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp), 
                contentAlignment = Alignment.Center
            ) {
                Canvas(
                    modifier = Modifier
                        .size(220.dp) 
                        .pointerInput(chartData) {
                            val canvasSize = this.size
                            detectTapGestures { offset ->
                                val center = Offset(canvasSize.width / 2f, canvasSize.height / 2f)
                                val x = offset.x - center.x
                                val y = offset.y - center.y
                                
                                var angle = Math.toDegrees(atan2(y.toDouble(), x.toDouble())).toFloat()
                                if (angle < 0) angle += 360f
                                
                                val touchAngle = (angle + 90f) % 360f
                                
                                var currentSweep = 0f
                                if (totalVolume > 0) {
                                    for (item in chartData) {
                                        val sweep = ((item.second / totalVolume) * 360).toFloat()
                                        if (touchAngle >= currentSweep && touchAngle < currentSweep + sweep) {
                                            selectedCategory = item
                                            return@detectTapGestures
                                        }
                                        currentSweep += sweep
                                    }
                                }
                                selectedCategory = null 
                            }
                        }
                ) {
                    val strokeWidth = 50f
                    var startAngle = -90f
                    
                    if (totalVolume == 0.0) {
                         drawArc(
                            color = Color.DarkGray,
                            startAngle = 0f,
                            sweepAngle = 360f,
                            useCenter = false,
                            style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeWidth)
                        )
                    } else {
                        chartData.forEach { (category, amount) ->
                            val sweepAngle = ((amount / totalVolume) * 360).toFloat()
                            // Determine color: Income is Green, Expenses use common logic
                            val isIncome = incomeBreakdown.containsKey(category)
                            val color = if (isIncome && !expenseBreakdown.containsKey(category)) Color(0xFF4CAF50) else getCategoryColor(category)
                            
                            drawArc(
                                color = color,
                                startAngle = startAngle,
                                sweepAngle = sweepAngle,
                                useCenter = false,
                                style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeWidth)
                            )
                            startAngle += sweepAngle
                        }
                    }
                }
                
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    if (selectedCategory == null) {
                        Text(stringResource(R.string.balance), color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 16.sp)
                        Text("IDR " + formatCurrency(totalBalance), color = MaterialTheme.colorScheme.onSurface, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                    } else {
                        selectedCategory?.let { (category, amount) ->
                            val percentage = if (totalVolume > 0) (amount / totalVolume * 100).toInt() else 0
                            val isIncome = incomeBreakdown.containsKey(category)
                            val color = if (isIncome && !expenseBreakdown.containsKey(category)) Color(0xFF4CAF50) else getCategoryColor(category)
                            
                            Text(text = stringResource(getCategoryNameResId(category)), color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = "${percentage}%", color = color, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                            Text(text = formatCurrency(amount), color = MaterialTheme.colorScheme.onSurface, fontSize = 14.sp)
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Legend - Top 3 items overall
            Row(
                modifier = Modifier.fillMaxWidth(), 
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                chartData.sortedByDescending { it.second }.take(3).forEach { (category, _) ->
                     val isIncome = incomeBreakdown.containsKey(category)
                     val color = if (isIncome && !expenseBreakdown.containsKey(category)) Color(0xFF4CAF50) else getCategoryColor(category)
                     Row(verticalAlignment = Alignment.CenterVertically) {
                         Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(color))
                         Spacer(modifier = Modifier.width(6.dp))
                         // Legend text
                         Text(stringResource(getCategoryNameResId(category)), color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                         Spacer(modifier = Modifier.width(12.dp))
                     }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp)) 
            
            Text(
                stringResource(R.string.show_more), 
                color = BlueButton, 
                fontSize = 14.sp, 
                fontWeight = FontWeight.Medium,
                modifier = Modifier.clickable { onShowMoreClick() }
            )
            }
        }

}

@Composable
fun HistorySection(transactions: List<Transaction>, onShowMoreClick: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(stringResource(R.string.history_title), color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold) 
                Icon(Icons.Default.MoreVert, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text(stringResource(R.string.last_30_days), color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 10.sp)
            
            Spacer(modifier = Modifier.height(8.dp)) // Reduced spacing
            
            if (transactions.isEmpty()) {
                Text(stringResource(R.string.no_records_yet), color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(vertical = 12.dp))
            } else {
                transactions.take(5).forEach { transaction ->
                    RecordItem(transaction)
                    // Spacer removed to match TransactionScreen list style
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                stringResource(R.string.show_more), 
                color = BlueButton, 
                fontSize = 14.sp, 
                fontWeight = FontWeight.Medium,
                modifier = Modifier.clickable { onShowMoreClick() }
            )
            }
        }

}

@Composable
fun RecordItem(transaction: Transaction) {
    val isExpense = transaction.type.equals("EXPENSE", ignoreCase = true)
    
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp), 
            verticalAlignment = Alignment.Top
        ) {
            // Icon
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(getCategoryColor(transaction.category).copy(alpha = 0.2f)), 
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
                    color = MaterialTheme.colorScheme.onSurfaceVariant, 
                    fontSize = 12.sp
                )
            }
        }
        // Divider
        HorizontalDivider(
            color = Color(0xFF333333), 
            thickness = 1.dp, 
            modifier = Modifier.padding(start = 56.dp) 
        )
    }
}

@Composable
fun LanguageSelectionDialog(
    onDismiss: () -> Unit,
    onLanguageSelected: (String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.choose_language), color = Color.White) },
        text = {
            Column {
                Text(
                    text = stringResource(R.string.language_en),
                    color = Color.White,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onLanguageSelected("en") }
                        .padding(vertical = 12.dp),
                    fontSize = 16.sp
                )
                HorizontalDivider(color = Color.Gray)
                Text(
                    text = stringResource(R.string.language_id),
                    color = Color.White,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onLanguageSelected("id") }
                        .padding(vertical = 12.dp),
                    fontSize = 16.sp
                )
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel), color = Color.Gray)
            }
        },
        containerColor = Color(0xFF2C2C2C)
    )
}
