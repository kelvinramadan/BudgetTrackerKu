package com.example.budgettrackerku.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.budgettrackerku.data.model.Transaction
import com.example.budgettrackerku.data.repository.BudgetRepository
import com.google.firebase.auth.FirebaseAuth
import com.example.budgettrackerku.data.model.BudgetNotification
import com.example.budgettrackerku.ui.common.NotificationHelper
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class BudgetViewModel(
    private val repository: BudgetRepository,
    context: Context
) : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val prefs = context.getSharedPreferences("budget_prefs", Context.MODE_PRIVATE)
    private val notificationHelper = NotificationHelper(context)

    val userId: String get() {
        return auth.currentUser?.uid ?: ""
    }

    val userEmail: String get() {
        return auth.currentUser?.email ?: ""
    }

    private val _refreshTrigger = MutableStateFlow(userId)
    
    // Status koneksi Firebase
    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

    init {
        // Monitor koneksi (gunakan URL yang sama dengan Repository)
        val connectedRef = com.google.firebase.database.FirebaseDatabase.getInstance("https://budgettrackerk-default-rtdb.asia-southeast1.firebasedatabase.app").getReference(".info/connected")
        connectedRef.addValueEventListener(object : com.google.firebase.database.ValueEventListener {
            override fun onDataChange(snapshot: com.google.firebase.database.DataSnapshot) {
                val connected = snapshot.getValue(Boolean::class.java) ?: false
                _isConnected.value = connected
            }
            override fun onCancelled(error: com.google.firebase.database.DatabaseError) {}
        })
    }

    val transactions: StateFlow<List<Transaction>> = _refreshTrigger.flatMapLatest { uid ->
        if (uid.isNotEmpty()) repository.getAllTransactions(uid) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val expenseBreakdown: StateFlow<Map<String, Double>> = transactions.map { list ->
        list.filter { it.type == "EXPENSE" }
            .groupBy { it.category }
            .mapValues { entry -> entry.value.sumOf { it.amount } }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    val incomeBreakdown: StateFlow<Map<String, Double>> = transactions.map { list ->
        list.filter { it.type == "INCOME" }
            .groupBy { it.category }
            .mapValues { entry -> entry.value.sumOf { it.amount } }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    val totalIncome: StateFlow<Double> = _refreshTrigger.flatMapLatest { uid ->
        if (uid.isNotEmpty()) repository.getTotalIncome(uid) else flowOf(0.0)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val totalExpense: StateFlow<Double> = _refreshTrigger.flatMapLatest { uid ->
        if (uid.isNotEmpty()) repository.getTotalExpense(uid) else flowOf(0.0)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val dailyExpense: StateFlow<Double> = _refreshTrigger.flatMapLatest { uid ->
        if (uid.isNotEmpty()) repository.getDailyExpense(uid) else flowOf(0.0)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val userName: StateFlow<String> = _refreshTrigger.flatMapLatest { uid ->
        if (uid.isNotEmpty()) repository.getUserName(uid) else flowOf("User")
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "User")

    val totalBalance: StateFlow<Double> = combine(totalIncome, totalExpense) { income, expense ->
        income - expense
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    // Calculate percent change vs 30 days ago (approximation of "past period")
    val balanceTrendPercentage: StateFlow<Double> = transactions.map { list ->
        if (list.isEmpty()) return@map 0.0
        
        val now = System.currentTimeMillis()
        val thirtyDaysAgo = now - (30L * 24 * 60 * 60 * 1000)
        
        // Balance 30 days ago is sum of all transactions before that date
        val pastBalance = list.filter { it.date < thirtyDaysAgo }
            .sumOf { if (it.type == "INCOME") it.amount else -it.amount }
            
        // Current balance (Total)
        val currentBalance = list.sumOf { if (it.type == "INCOME") it.amount else -it.amount }
        
        if (pastBalance == 0.0) {
            if (currentBalance > 0) 100.0 else 0.0
        } else {
            ((currentBalance - pastBalance) / pastBalance) * 100.0
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    // Generate running balance history for every transaction for chart (per-transaction trend)
    val balanceHistory: StateFlow<List<Double>> = transactions.map { list ->
        if (list.isEmpty()) return@map emptyList<Double>()

        // 1. Sort all transactions by date (Oldest -> Newest)
        val sorted = list.sortedBy { it.date }

        // 2. Calculate running balance for the entire history
        val allRunningBalances = mutableListOf<Double>()
        var currentBalance = 0.0
        
        // Also keep track of indices that fall within the last 30 days
        val thirtyDaysAgo = System.currentTimeMillis() - (30L * 24 * 60 * 60 * 1000)
        val recentReviewIndices = mutableListOf<Int>()

        sorted.forEachIndexed { index, transaction ->
            val amount = if (transaction.type == "INCOME") transaction.amount else -transaction.amount
            currentBalance += amount
            allRunningBalances.add(currentBalance)
            
            if (transaction.date >= thirtyDaysAgo) {
                recentReviewIndices.add(index)
            }
        }

        // 3. Extract the relevant portion for the chart (Last 30 Days)
        if (recentReviewIndices.isEmpty()) {
            // No recent transactions, return the last known balance as a flat line or empty?
            // If we have history but nothing recent, returning the last balance implies a flat line from the past.
            if (allRunningBalances.isNotEmpty()) listOf(allRunningBalances.last()) else emptyList()
        } else {
            // We want to show the line starting from the balance *before* the first recent transaction
            val firstRecentIndex = recentReviewIndices.first()
            val startIndex = if (firstRecentIndex > 0) firstRecentIndex - 1 else 0
            
            // Return sublist from startIndex to end
            allRunningBalances.subList(startIndex, allRunningBalances.size)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _dailyLimit = MutableStateFlow(prefs.getFloat("daily_limit_$userId", 100000f).toDouble())
    val dailyLimit = _dailyLimit.asStateFlow()

    fun setDailyLimit(amount: Double) {
        prefs.edit().putFloat("daily_limit_$userId", amount.toFloat()).apply()
        _dailyLimit.value = amount
    }

    fun addTransaction(title: String, amount: Double, type: String, category: String) {
        if (userId.isEmpty()) {
            Log.e("BudgetViewModel", "User is not logged in, cannot add transaction.")
            return
        }
        viewModelScope.launch {
            val transaction = Transaction().apply {
                this.userId = this@BudgetViewModel.userId
                this.title = title
                this.amount = amount
                this.type = type
                this.category = category
                this.date = System.currentTimeMillis()
            }
            Log.d("BudgetViewModel", "Attempting to add transaction: $transaction")
            repository.addTransaction(transaction)
            
            // Check budget after adding transaction
            checkBudgetAndNotify(category, amount)
        }
    }

    private fun checkBudgetAndNotify(category: String, addedAmount: Double) {
        val currentBudget = budgets.value[category] ?: 0.0
        if (currentBudget > 0) {
            val currentSpent = expenseBreakdown.value[category] ?: 0.0
            val newTotal = currentSpent + addedAmount
            
            // Notification logic: If we just exceeded or are already exceeding? 
            // Simple approach: strict notify if > budget.
            // Improve: Only notify if we weren't already over budget? Or maybe just notify every time they add to an over-budget category.
            // Let's do: If newTotal > budget, notify.
            
            if (newTotal > currentBudget) {
                val message = "Your spending in $category has exceeded the budget of ${formatCurrency(currentBudget)}!"
                
                // 1. System Notification
                notificationHelper.showNotification("Budget Alert: $category", message)
                
                // 2. Persist to Firebase
                val notification = BudgetNotification(
                    userId = userId,
                    title = "Budget Alert: $category",
                    message = message,
                    date = System.currentTimeMillis()
                )
                repository.saveNotification(notification)
            }
        }
    }
    
    // Helper for formatting (since we are in VM, maybe just simple int format or duplicate logic, 
    // but better to keep it simple or use NumberFormat)
    private fun formatCurrency(amount: Double): String {
        val format = java.text.NumberFormat.getNumberInstance(java.util.Locale("id", "ID"))
        return "IDR ${format.format(amount)}"
    }

    fun deleteTransaction(transaction: Transaction) {
        viewModelScope.launch { 
            repository.deleteTransaction(transaction) 
        }
    }

    fun logout() {
        auth.signOut()
        _refreshTrigger.value = "" // Clear data
    }

    fun refreshUserData() {
        _refreshTrigger.value = userId
    }
    
    fun updateUserName(name: String) {
        viewModelScope.launch {
            repository.updateUserName(userId, name)
        }
    }

    fun updateUserEmail(email: String, onSuccess: () -> Unit, onError: (Exception) -> Unit) {
        val user = auth.currentUser
        if (user != null) {
            user.updateEmail(email)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.d("BudgetViewModel", "User email updated. Logging out.")
                        logout()
                        onSuccess()
                    } else {
                        Log.e("BudgetViewModel", "Failed to update email.", task.exception)
                        onError(task.exception ?: Exception("Unknown error"))
                    }
                }
        }
    }
    val budgets: StateFlow<Map<String, Double>> = _refreshTrigger.flatMapLatest { uid ->
        if (uid.isNotEmpty()) repository.getBudgets(uid) else flowOf(emptyMap())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    val notifications: StateFlow<List<BudgetNotification>> = _refreshTrigger.flatMapLatest { uid ->
        if (uid.isNotEmpty()) repository.getNotifications(uid) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setBudget(category: String, amount: Double) {
        viewModelScope.launch {
            repository.setBudget(userId, category, amount)
        }
    }
}


class BudgetViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BudgetViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return BudgetViewModel(BudgetRepository(), context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
