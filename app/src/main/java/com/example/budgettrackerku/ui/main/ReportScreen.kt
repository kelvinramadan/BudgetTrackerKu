package com.example.budgettrackerku.ui.main

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.budgettrackerku.viewmodel.BudgetViewModel
import com.example.budgettrackerku.ui.common.getCategoryColor
import com.example.budgettrackerku.ui.common.getCategoryNameResId
import com.example.budgettrackerku.ui.theme.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import kotlin.math.atan2
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.res.stringResource
import com.example.budgettrackerku.R

@Composable
fun ReportScreen(
    viewModel: BudgetViewModel, 
    onNavigateToProfile: () -> Unit,
    onNavigateToNotifications: () -> Unit = {}
) {
    val totalExpense by viewModel.totalExpense.collectAsState()
    val totalIncome by viewModel.totalIncome.collectAsState()
    val totalBalance by viewModel.totalBalance.collectAsState()
    val expenseBreakdown by viewModel.expenseBreakdown.collectAsState()
    val incomeBreakdown by viewModel.incomeBreakdown.collectAsState()

    // Force Dark Theme Colors Removed - Use MaterialTheme.colorScheme directly
    
    // Shared Chart Data Logic
    val totalVolume = totalExpense + totalIncome
    val chartData = remember(expenseBreakdown, incomeBreakdown) {
         expenseBreakdown.entries.map { it.key to it.value } + 
         incomeBreakdown.entries.map { it.key to it.value }
    }
    
    // Interaction State
    var selectedCategory by remember { mutableStateOf<Pair<String, Double>?>(null) }
    var touchPosition by remember { mutableStateOf(Offset.Zero) }

    // MaterialTheme wrapper removed
    Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
               BudgetTrackerTopBar(
                   title = stringResource(R.string.reports_title),
                   navigationIcon = Icons.Default.AccountCircle, // Profile Icon
                   onMenuClick = onNavigateToProfile,
                   onNotificationClick = onNavigateToNotifications
               )
            },
            containerColor = MaterialTheme.colorScheme.background
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
            ) {
                // Balance Structures Card
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        // Header
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stringResource(R.string.balance_structures),
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "Menu",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = stringResource(R.string.last_30_days),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 12.sp
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "IDR " + formatCurrency(totalBalance),
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(32.dp))

                        // Donut Chart Container
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Canvas(
                                modifier = Modifier
                                    .size(220.dp)
                                    .pointerInput(chartData) {
                                        detectTapGestures { offset ->
                                            val center = Offset(size.width / 2f, size.height / 2f)
                                            val x = offset.x - center.x
                                            val y = offset.y - center.y
                                            
                                            // Calculate Angle (0 to 360, starting from North/top)
                                            var angle = Math.toDegrees(atan2(y.toDouble(), x.toDouble())).toFloat()
                                            if (angle < 0) angle += 360f
                                            
                                            // Normalize so 0 is North (-90 in Canvas)
                                            // Canvas 0 is East. We draw startAngle = -90.
                                            // So North is -90 (or 270). 
                                            // angle 0 (East) -> should correspond to 90 degrees progress if we start at North.
                                            // angle -90 (North) -> 0 progress.
                                            // Formula: (angle + 90) % 360.
                                            val touchAngle = (angle + 90f) % 360f
                                            
                                            var currentSweep = 0f
                                            if (totalVolume > 0) {
                                                for (item in chartData) {
                                                    val sweep = ((item.second / totalVolume) * 360).toFloat()
                                                    if (touchAngle >= currentSweep && touchAngle < currentSweep + sweep) {
                                                        selectedCategory = item
                                                        touchPosition = offset
                                                        return@detectTapGestures
                                                    }
                                                    currentSweep += sweep
                                                }
                                            }
                                            selectedCategory = null // Clicked outside? or keep last?
                                            // Actually with detectTapGestures, it's a tap.
                                            // If we want to toggle off:
                                            // selectedCategory = null
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
                                        style = Stroke(width = strokeWidth)
                                    )
                                } else {
                                    chartData.forEach { (category, amount) ->
                                        val sweepAngle = ((amount / totalVolume) * 360).toFloat()
                                        val isIncome = incomeBreakdown.containsKey(category)
                                        val color = if (isIncome && !expenseBreakdown.containsKey(category)) Color(0xFF4CAF50) else getCategoryColor(category)
                                        
                                        // Draw Arc
                                        drawArc(
                                            color = color,
                                            startAngle = startAngle,
                                            sweepAngle = sweepAngle,
                                            useCenter = false,
                                            style = Stroke(width = strokeWidth)
                                        )
                                        startAngle += sweepAngle
                                    }
                                }
                            }
                            
                            // Center Text (Default State)
                            if (selectedCategory == null) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                     Text(text = stringResource(R.string.balance), color = MaterialTheme.colorScheme.onSurface, fontSize = 16.sp)
                                     Text(text = "IDR " + formatCurrency(totalBalance), color = MaterialTheme.colorScheme.onSurface, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                                }
                            }
                            
                            // Tooltip Overlay (When Selected)
                            selectedCategory?.let { (category, amount) ->
                                // Calculate color for the selected category
                                val isIncome = incomeBreakdown.containsKey(category)
                                val color = if (isIncome && !expenseBreakdown.containsKey(category)) Color(0xFF4CAF50) else getCategoryColor(category)
                                val percentage = if (totalVolume > 0) (amount / totalVolume * 100).toInt() else 0

                                // Render centered info replacing "Balance" (Cleanest)
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                     Text(text = stringResource(getCategoryNameResId(category)), color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
                                     Spacer(modifier = Modifier.height(4.dp))
                                     Text(text = "${percentage}%", color = color, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                                     Text(text = formatCurrency(amount), color = MaterialTheme.colorScheme.onSurface, fontSize = 14.sp)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(32.dp))
                        
                        // Accumulation List (Expenses)
                        if (expenseBreakdown.isNotEmpty()) {
                            Text(stringResource(R.string.expenses_breakdown), color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(8.dp))
                            expenseBreakdown.entries.sortedByDescending { it.value }.forEach { (category, amount) ->
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier.size(8.dp).clip(CircleShape).background(getCategoryColor(category))
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(stringResource(getCategoryNameResId(category)), color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f))
                                    Text("Rp ${formatCurrency(amount)}", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold)
                                }
                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                            }
                        }

                         Spacer(modifier = Modifier.height(24.dp))

                        // Accumulation List (Income)
                        if (incomeBreakdown.isNotEmpty()) {
                            Text(stringResource(R.string.income_breakdown), color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                             Spacer(modifier = Modifier.height(8.dp))
                            incomeBreakdown.entries.sortedByDescending { it.value }.forEach { (category, amount) ->
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFF4CAF50)) // Green for Income
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(stringResource(getCategoryNameResId(category)), color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f))
                                    Text("Rp ${formatCurrency(amount)}", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold)
                                }
                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                            }
                        }
                    }
                }
            
                Spacer(modifier = Modifier.height(100.dp))
            }
        }
    //} // Removed closing brace for MaterialTheme
}


