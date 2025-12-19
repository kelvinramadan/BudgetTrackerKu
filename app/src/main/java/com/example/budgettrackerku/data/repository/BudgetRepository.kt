package com.example.budgettrackerku.data.repository

import com.example.budgettrackerku.data.local.TransactionDao
import com.example.budgettrackerku.data.local.TransactionEntity
import kotlinx.coroutines.flow.Flow
import java.util.Calendar

class BudgetRepository(private val dao: TransactionDao) {

    suspend fun addTransaction(transaction: TransactionEntity) = dao.insert(transaction)
    suspend fun deleteTransaction(transaction: TransactionEntity) = dao.delete(transaction)

    fun getAllTransactions(userId: String): Flow<List<TransactionEntity>> = dao.getAllTransactions(userId)

    fun getTotalIncome(userId: String): Flow<Double?> = dao.getTotalIncome(userId)
    fun getTotalExpense(userId: String): Flow<Double?> = dao.getTotalExpense(userId)

    fun getDailyExpense(userId: String): Flow<Double?> {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val startOfDay = calendar.timeInMillis
        val endOfDay = startOfDay + 86399999 // +23:59:59
        return dao.getDailyExpense(userId, startOfDay, endOfDay)
    }
}
