package com.example.budgettrackerku.data.repository

import android.util.Log
import com.example.budgettrackerku.data.model.Transaction
import com.example.budgettrackerku.data.model.BudgetNotification
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.getValue
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import java.util.Calendar


class BudgetRepository {

    // Menggunakan URL eksplisit dari screenshot user (budgettrackerk)
    private val dbRoot = FirebaseDatabase.getInstance("https://budgettrackerk-default-rtdb.asia-southeast1.firebasedatabase.app").reference

    fun addTransaction(transaction: Transaction) {
        if (transaction.userId.isEmpty()) {
            Log.e("BudgetRepository", "Cannot add transaction with empty userId.")
            return
        }

        // Generate ID baru dari Firebase
        val dbRef = dbRoot.child("transactions").child(transaction.userId)
        val newId = dbRef.push().key
        if (newId == null) {
            Log.e("BudgetRepository", "Couldn't get push key for transactions")
            return
        }

        transaction.id = newId

        Log.d("BudgetRepository", "Writing to path: ${dbRef.child(newId).path}")
        dbRef.child(newId).setValue(transaction)
            .addOnSuccessListener {
                Log.i("BudgetRepository", "SUCCESS - Data saved to Firebase for transaction ID: $newId")
            }
            .addOnFailureListener { e ->
                Log.e("BudgetRepository", "FAILURE - Failed to save data: ", e)
            }
    }

    fun deleteTransaction(transaction: Transaction) {
        if (transaction.userId.isEmpty() || transaction.id.isEmpty()) return
        dbRoot.child("transactions").child(transaction.userId).child(transaction.id).removeValue()
    }

    fun getAllTransactions(userId: String): Flow<List<Transaction>> = callbackFlow {
        val dbRef = dbRoot.child("transactions").child(userId)
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val transactions = snapshot.children.mapNotNull { it.getValue(Transaction::class.java) }
                Log.d("BudgetRepository", "Data received for user $userId: ${transactions.size} items")
                trySend(transactions).isSuccess
            }
            override fun onCancelled(error: DatabaseError) {
                Log.e("BudgetRepository", "Data listener cancelled for user $userId", error.toException())
                close(error.toException())
            }
        }
        dbRef.addValueEventListener(listener)
        awaitClose { dbRef.removeEventListener(listener) }
    }

    fun getTotalIncome(userId: String): Flow<Double> = callbackFlow {
        val dbRef = dbRoot.child("transactions").child(userId)
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val total = snapshot.children
                    .mapNotNull { it.getValue(Transaction::class.java) }
                    .filter { it.type == "INCOME" }
                    .sumOf { it.amount }
                trySend(total).isSuccess
            }
            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        dbRef.addValueEventListener(listener)
        awaitClose { dbRef.removeEventListener(listener) }
    }

    fun getTotalExpense(userId: String): Flow<Double> = callbackFlow {
        val dbRef = dbRoot.child("transactions").child(userId)
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val total = snapshot.children
                    .mapNotNull { it.getValue(Transaction::class.java) }
                    .filter { it.type == "EXPENSE" }
                    .sumOf { it.amount }
                trySend(total).isSuccess
            }
            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        dbRef.addValueEventListener(listener)
        awaitClose { dbRef.removeEventListener(listener) }
    }

    fun getDailyExpense(userId: String): Flow<Double> = callbackFlow {
        val dbRef = dbRoot.child("transactions").child(userId)
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val startOfDay = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }.timeInMillis

                val total = snapshot.children
                    .mapNotNull { it.getValue(Transaction::class.java) }
                    .filter { it.type == "EXPENSE" && it.date >= startOfDay }
                    .sumOf { it.amount }
                trySend(total).isSuccess
            }
            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        dbRef.addValueEventListener(listener)
        awaitClose { dbRef.removeEventListener(listener) }
    }
    fun getUserName(userId: String): Flow<String> = callbackFlow {
        val dbRef = dbRoot.child("users").child(userId).child("name")
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val name = snapshot.getValue(String::class.java) ?: "User"
                trySend(name).isSuccess
            }
            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        dbRef.addValueEventListener(listener)
        awaitClose { dbRef.removeEventListener(listener) }
    }

    fun updateUserName(userId: String, name: String) {
        if (userId.isEmpty()) return
        val dbRef = dbRoot.child("users").child(userId).child("name")
        dbRef.setValue(name)
            .addOnFailureListener { e ->
                Log.e("BudgetRepository", "Failed to update user name", e)
            }
    }

    fun setBudget(userId: String, category: String, amount: Double) {
        if (userId.isEmpty()) return
        val dbRef = dbRoot.child("budgets").child(userId).child(category)
        dbRef.setValue(amount)
            .addOnFailureListener { e ->
                Log.e("BudgetRepository", "Failed to set budget for $category", e)
            }
    }

    fun getBudgets(userId: String): Flow<Map<String, Double>> = callbackFlow {
        val dbRef = dbRoot.child("budgets").child(userId)
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val budgets = mutableMapOf<String, Double>()
                for (child in snapshot.children) {
                    val category = child.key
                    val amount = child.getValue(Double::class.java)
                    if (category != null && amount != null) {
                        budgets[category] = amount
                    }
                }
                trySend(budgets).isSuccess
            }
            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        dbRef.addValueEventListener(listener)
        awaitClose { dbRef.removeEventListener(listener) }
    }

    fun saveNotification(notification: BudgetNotification) {
         if (notification.userId.isEmpty()) return
         val dbRef = dbRoot.child("notifications").child(notification.userId)
         val newId = dbRef.push().key ?: return
         notification.id = newId
         dbRef.child(newId).setValue(notification)
    }

    fun getNotifications(userId: String): Flow<List<BudgetNotification>> = callbackFlow {
        val dbRef = dbRoot.child("notifications").child(userId)
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                 val list = snapshot.children.mapNotNull { it.getValue(BudgetNotification::class.java) }
                 trySend(list).isSuccess
            }
            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        dbRef.addValueEventListener(listener)
        awaitClose { dbRef.removeEventListener(listener) }
    }
}
