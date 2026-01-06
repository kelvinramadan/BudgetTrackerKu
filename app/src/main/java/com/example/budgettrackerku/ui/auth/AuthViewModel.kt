package com.example.budgettrackerku.ui.auth

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class AuthViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    // Ensure we write to the same database instance we read from
    private val database = FirebaseDatabase.getInstance("https://budgettrackerk-default-rtdb.asia-southeast1.firebasedatabase.app").reference

    val isLoading = mutableStateOf(false)
    val errorMessage = mutableStateOf<String?>(null)

    fun login(
        email: String,
        password: String,
        onSuccess: () -> Unit
    ) {
        isLoading.value = true
        errorMessage.value = null

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                isLoading.value = false
                if (task.isSuccessful) {
                    onSuccess()
                } else {
                    errorMessage.value = task.exception?.message
                }
            }
    }

    fun register(
        name: String,
        email: String,
        password: String,
        onSuccess: () -> Unit
    ) {
        isLoading.value = true
        errorMessage.value = null

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val userId = auth.currentUser?.uid
                    if (userId != null) {
                        val user = mapOf("name" to name, "email" to email)
                        database.child("users").child(userId).setValue(user)
                            .addOnCompleteListener { dbTask ->
                                isLoading.value = false
                                if (dbTask.isSuccessful) {
                                    onSuccess()
                                } else {
                                    errorMessage.value = dbTask.exception?.message
                                }
                            }
                    } else {
                        isLoading.value = false
                        errorMessage.value = "Failed to get user ID."
                    }
                } else {
                    isLoading.value = false
                    errorMessage.value = task.exception?.message
                }
            }
    }
}
