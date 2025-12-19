package com.example.budgettrackerku.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.budgettrackerku.viewmodel.BudgetViewModel
import java.text.NumberFormat
import java.util.*

// Updated HomeScreen to reflect the new design
@Composable
fun HomeScreen(viewModel: BudgetViewModel) {
    val transactions by viewModel.transactions.collectAsState()
    val totalIncome by viewModel.totalIncome.collectAsState()
    val totalExpense by viewModel.totalExpense.collectAsState()

    val balance = (totalIncome ?: 0.0) - (totalExpense ?: 0.0)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            BalanceCard(
                balance = balance,
                income = totalIncome ?: 0.0,
                expense = totalExpense ?: 0.0
            )
        }

        item {
            Text(
                text = "Recent Transactions",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        if (transactions.isEmpty()) {
            item {
                Text(
                    text = "No transactions yet.",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }
        } else {
            items(transactions.take(5)) { transaction ->
                TransactionItem(
                    title = transaction.title,
                    amount = transaction.amount,
                    type = transaction.type,
                    category = transaction.category
                )
            }
        }
    }
}

@Composable
fun BalanceCard(balance: Double, income: Double, expense: Double) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("Total Balance", style = MaterialTheme.typography.titleMedium, color = Color.Gray)
            Text(
                text = formatCurrency(balance),
                style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
                color = if (balance >= 0) MaterialTheme.colorScheme.primary else Color.Red
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Income", style = MaterialTheme.typography.labelLarge)
                    Text(
                        text = formatCurrency(income),
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color(0xFF008000) // Green
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Expense", style = MaterialTheme.typography.labelLarge)
                    Text(
                        text = formatCurrency(expense),
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.Red
                    )
                }
            }
        }
    }
}

@Composable
fun TransactionItem(title: String, amount: Double, type: String, category: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold))
                Spacer(modifier = Modifier.height(4.dp))
                Text(category, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
            Text(
                text = "${if (type == "EXPENSE") "-" else "+"}${formatCurrency(amount)}",
                color = if (type == "EXPENSE") Color.Red else Color(0xFF008000),
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold)
            )
        }
    }
}

fun formatCurrency(amount: Double): String {
    val format = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
    format.maximumFractionDigits = 0
    return format.format(amount)
}
