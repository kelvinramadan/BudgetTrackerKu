package com.example.budgettrackerku.ui.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.budgettrackerku.data.model.Transaction
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

import androidx.compose.ui.res.stringResource
import com.example.budgettrackerku.ui.common.getCategoryNameResId

@Composable
fun TransactionItem(
    transaction: Transaction,
    onDelete: ((Transaction) -> Unit)? = null
) {
    val isExpense = transaction.type == "EXPENSE"
    val amountColor = if (isExpense) Color.Red else Color(0xFF008000)
    val sign = if (isExpense) "- " else "+ "
    val categoryName = stringResource(getCategoryNameResId(transaction.category))

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(transaction.title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "$categoryName • ${SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(transaction.date))}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = "$sign${formatCurrency(transaction.amount)}",
                color = amountColor,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold
            )
            if (onDelete != null) {
                IconButton(onClick = { onDelete(transaction) }) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}


fun formatCurrency(amount: Double): String {
    val format = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
    format.maximumFractionDigits = 0
    // Remove "Rp" prefix if present to avoid duplication
    return format.format(amount).replace("Rp", "").trim()
}

fun formatDateTime(timestamp: Long): String {
    val date = Date(timestamp)
    val format = SimpleDateFormat("dd MMM yyyy · hh:mm a", Locale.getDefault())
    return format.format(date)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetTrackerTopBar(
    title: String,
    navigationIcon: androidx.compose.ui.graphics.vector.ImageVector? = null, // Default to null (no icon)
    onMenuClick: () -> Unit = {},
    onNotificationClick: () -> Unit = {},
    showNotificationIcon: Boolean = true
) {
    CenterAlignedTopAppBar(
        title = {
            Text(
                text = title,
                color = androidx.compose.ui.graphics.Color.White, // White Text
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold
            )
        },
        navigationIcon = {
            if (navigationIcon != null) {
                IconButton(
                    onClick = onMenuClick,
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(if (androidx.compose.foundation.isSystemInDarkTheme()) MaterialTheme.colorScheme.surfaceVariant else androidx.compose.ui.graphics.Color.White) // Standard in Dark, White in Light
                ) {
                    Icon(
                        imageVector = navigationIcon,
                        contentDescription = "Menu",
                        tint = if (androidx.compose.foundation.isSystemInDarkTheme()) androidx.compose.ui.graphics.Color.White else com.example.budgettrackerku.ui.theme.TextPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        },
        actions = {
            if (showNotificationIcon) {
                IconButton(
                    onClick = onNotificationClick,
                    modifier = Modifier
                        .padding(end = 8.dp)
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(if (androidx.compose.foundation.isSystemInDarkTheme()) MaterialTheme.colorScheme.surfaceVariant else androidx.compose.ui.graphics.Color.White) // Standard in Dark, White in Light
                ) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = "Notifications",
                        tint = if (androidx.compose.foundation.isSystemInDarkTheme()) androidx.compose.ui.graphics.Color.White else com.example.budgettrackerku.ui.theme.TextPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        },

        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = if (androidx.compose.foundation.isSystemInDarkTheme()) MaterialTheme.colorScheme.background else com.example.budgettrackerku.ui.theme.BottomBarBackground, // Dark Slate in Light Mode, Seamless Background in Dark Mode
            titleContentColor = androidx.compose.ui.graphics.Color.White,
            navigationIconContentColor = androidx.compose.ui.graphics.Color.White,
            actionIconContentColor = androidx.compose.ui.graphics.Color.White
        )
    )
}
