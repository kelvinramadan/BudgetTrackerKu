package com.example.budgettrackerku.ui.main

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.budgettrackerku.viewmodel.BudgetViewModel

@Composable
fun SettingScreen(viewModel: BudgetViewModel, onLogout: () -> Unit) {
    val currentLimit by viewModel.dailyLimit.collectAsState()
    var limitInput by remember { mutableStateOf(currentLimit.toString()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Settings",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(2.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Daily Spending Limit", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = limitInput,
                    onValueChange = { limitInput = it },
                    label = { Text("Limit Amount") },
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = { Text("IDR", style = MaterialTheme.typography.bodySmall) }
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = {
                        limitInput.toDoubleOrNull()?.let { viewModel.setDailyLimit(it) }
                    },
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Save")
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = {
                viewModel.logout()
                onLogout()
            },
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text("Logout")
        }
    }
}
