package com.example.budgettrackerku.data.model

data class Transaction(
    val id: String = "",
    val userId: String = "",
    val title: String = "",
    val amount: Double = 0.0,
    val type: String = "", // "INCOME" atau "EXPENSE"
    val category: String = "",
    val date: Long = System.currentTimeMillis()
)
