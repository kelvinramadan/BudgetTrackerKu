package com.example.budgettrackerku.ui.main

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.filled.AccountCircle
import com.example.budgettrackerku.viewmodel.BudgetViewModel

@Composable
fun SettingScreen(viewModel: BudgetViewModel, onLogout: () -> Unit) {
    val currentLimit by viewModel.dailyLimit.collectAsState()
    var limitInput by remember { mutableStateOf(currentLimit.toString()) }

    // Force Dark Theme Colors locally
    val BackgroundBlack = androidx.compose.ui.graphics.Color(0xFF121212)
    val SurfaceDark = androidx.compose.ui.graphics.Color(0xFF1E1E1E)
    val TextWhite = androidx.compose.ui.graphics.Color(0xFFFFFFFF)

    MaterialTheme(
        colorScheme = darkColorScheme(
            background = BackgroundBlack,
            surface = SurfaceDark,
            onSurface = TextWhite
        )
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                BudgetTrackerTopBar(
                    title = "Settings",
                    navigationIcon = androidx.compose.material.icons.Icons.Default.AccountCircle
                )
            },
            containerColor = BackgroundBlack
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(androidx.compose.foundation.rememberScrollState())
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Spacer(modifier = Modifier.height(16.dp))
    
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
        }
    }
}
