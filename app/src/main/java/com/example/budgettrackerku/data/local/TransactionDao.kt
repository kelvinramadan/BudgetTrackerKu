package com.example.budgettrackerku.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(transaction: TransactionEntity)

    @Delete
    suspend fun delete(transaction: TransactionEntity)

    @Query("SELECT * FROM transactions WHERE userId = :userId ORDER BY date DESC")
    fun getAllTransactions(userId: String): Flow<List<TransactionEntity>>

    // Untuk menghitung pengeluaran hari ini (Logic Limit)
    @Query("SELECT SUM(amount) FROM transactions WHERE userId = :userId AND type = 'EXPENSE' AND date >= :startOfDay AND date <= :endOfDay")
    fun getDailyExpense(userId: String, startOfDay: Long, endOfDay: Long): Flow<Double?>

    @Query("SELECT SUM(amount) FROM transactions WHERE userId = :userId AND type = 'INCOME'")
    fun getTotalIncome(userId: String): Flow<Double?>

    @Query("SELECT SUM(amount) FROM transactions WHERE userId = :userId AND type = 'EXPENSE'")
    fun getTotalExpense(userId: String): Flow<Double?>
}
