package com.example.budgettrackerku.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [TransactionEntity::class], version = 1, exportSchema = false)
abstract class BudgetDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao

    companion object {
        @Volatile
        private var Instance: BudgetDatabase? = null

        fun getDatabase(context: Context): BudgetDatabase {
            return Instance ?: synchronized(this) {
                Room.databaseBuilder(context, BudgetDatabase::class.java, "budget_db")
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { Instance = it }
            }
        }
    }
}
