package com.example.budgettrackerku.ui.main

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.budgettrackerku.viewmodel.BudgetViewModel

@Composable
fun ReportScreen(viewModel: BudgetViewModel) {
    val transactions by viewModel.transactions.collectAsState()
    val categoryStats = transactions
        .filter { it.type == "EXPENSE" }
        .groupBy { it.category }
        .mapValues { entry -> entry.value.sumOf { it.amount } }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Text(
                text = "Expense by Category",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        if (categoryStats.isEmpty()) {
            item {
                Text("No expense data available.")
            }
        } else {
            items(categoryStats.toList()) { (category, total) ->
                CategorySummaryItem(category = category, amount = total)
            }
        }
    }
}

@Composable
fun CategorySummaryItem(category: String, amount: Double) {
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(category, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
            Text(formatCurrency(amount), style = MaterialTheme.typography.bodyLarge)
        }
    }
}
