package com.example.budgettrackerku.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.budgettrackerku.data.local.TransactionEntity
import com.example.budgettrackerku.data.repository.BudgetRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class BudgetViewModel(
    private val repository: BudgetRepository,
    context: Context
) : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val prefs = context.getSharedPreferences("budget_prefs", Context.MODE_PRIVATE)

    // --- State Auth ---
    val currentUser = auth.currentUser
    val userId: String get() = currentUser?.uid ?: ""

    // --- State Data ---
    private val _refreshTrigger = MutableStateFlow(userId)

    val transactions = _refreshTrigger.flatMapLatest { uid ->
        if (uid.isNotEmpty()) repository.getAllTransactions(uid) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalIncome = _refreshTrigger.flatMapLatest { uid ->
        if (uid.isNotEmpty()) repository.getTotalIncome(uid) else flowOf(0.0)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val totalExpense = _refreshTrigger.flatMapLatest { uid ->
        if (uid.isNotEmpty()) repository.getTotalExpense(uid) else flowOf(0.0)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val dailyExpense = _refreshTrigger.flatMapLatest { uid ->
        if (uid.isNotEmpty()) repository.getDailyExpense(uid) else flowOf(0.0)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    // --- State Limit (Disimpan di SharedPreferences sederhana) ---
    private val _dailyLimit = MutableStateFlow(prefs.getFloat("daily_limit_$userId", 100000f).toDouble())
    val dailyLimit = _dailyLimit.asStateFlow()

    fun setDailyLimit(amount: Double) {
        prefs.edit().putFloat("daily_limit_$userId", amount.toFloat()).apply()
        _dailyLimit.value = amount
    }

    // --- Actions ---
    fun addTransaction(title: String, amount: Double, type: String, category: String) {
        if (userId.isEmpty()) return
        viewModelScope.launch {
            try {
                repository.addTransaction(
                    TransactionEntity(
                        userId = userId,
                        title = title,
                        amount = amount,
                        type = type,
                        category = category
                    )
                )
                _refreshTrigger.value = userId // Trigger a refresh
            } catch (e: Exception) {
                Log.e("BudgetViewModel", "Error adding transaction", e)
            }
        }
    }

    fun deleteTransaction(transaction: TransactionEntity) {
        viewModelScope.launch { 
            repository.deleteTransaction(transaction)
            _refreshTrigger.value = userId // Trigger a refresh
        }
    }

    fun logout() {
        auth.signOut()
        _refreshTrigger.value = "" // Clear data view
    }

    fun refreshUserData() {
        _refreshTrigger.value = userId
    }
}

// Factory untuk membuat ViewModel dengan parameter
class BudgetViewModelFactory(private val repo: BudgetRepository, private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BudgetViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return BudgetViewModel(repo, context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
