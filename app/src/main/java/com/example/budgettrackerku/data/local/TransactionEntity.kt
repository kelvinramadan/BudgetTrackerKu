package com.example.budgettrackerku.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: String, // Penting: Data user A tidak muncul di user B
    val title: String,
    val amount: Double,
    val type: String, // "INCOME" atau "EXPENSE"
    val category: String,
    val date: Long = System.currentTimeMillis()
)
