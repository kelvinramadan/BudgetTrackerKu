package com.example.budgettrackerku.ui.auth

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.example.budgettrackerku.repository.AuthRepository

class AuthViewModel : ViewModel() {

    private val repository = AuthRepository()

    val isLoading = mutableStateOf(false)
    val errorMessage = mutableStateOf<String?>(null)

    fun login(
        email: String,
        password: String,
        onSuccess: () -> Unit
    ) {
        isLoading.value = true
        errorMessage.value = null

        repository.login(
            email,
            password,
            onSuccess = {
                isLoading.value = false
                onSuccess()
            },
            onError = {
                isLoading.value = false
                errorMessage.value = it
            }
        )
    }

    fun register(
        name: String,
        email: String,
        password: String,
        onSuccess: () -> Unit
    ) {
        isLoading.value = true
        errorMessage.value = null

        repository.register(
            name,
            email,
            password,
            onSuccess = {
                isLoading.value = false
                onSuccess()
            },
            onError = {
                isLoading.value = false
                errorMessage.value = it
            }
        )
    }
}
