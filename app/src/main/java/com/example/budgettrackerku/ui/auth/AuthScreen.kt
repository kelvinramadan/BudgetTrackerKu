package com.example.budgettrackerku.ui.auth

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.budgettrackerku.ui.theme.GradientEnd
import com.example.budgettrackerku.ui.theme.GradientStart

@Composable
fun AuthScreen(onLoginSuccess: () -> Unit) {
    var isLoginMode by remember { mutableStateOf(true) }
    val authViewModel: AuthViewModel = viewModel(factory = AuthViewModelFactory())

    Scaffold(
        topBar = { /* Empty for design */ }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.primary)
                .padding(padding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .padding(top = 48.dp, start = 16.dp, end = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("BudgetTrackerKu", style = MaterialTheme.typography.headlineLarge.copy(color = Color.White, fontWeight = FontWeight.Bold))
            }

            Card(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .fillMaxHeight(0.85f),
                shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background)
            ) {
                if (isLoginMode) {
                    LoginContent(
                        viewModel = authViewModel,
                        onRegisterClick = { isLoginMode = false },
                        onLoginSuccess = onLoginSuccess
                    )
                } else {
                    RegisterContent(
                        viewModel = authViewModel,
                        onLoginClick = { isLoginMode = true },
                        onRegisterSuccess = onLoginSuccess
                    )
                }
            }
        }
    }
}

@Composable
fun LoginContent(viewModel: AuthViewModel, onRegisterClick: () -> Unit, onLoginSuccess: () -> Unit) {
    var email by remember { mutableStateOf("nicholas@ergemla.com") }
    var password by remember { mutableStateOf("password") }
    val isLoading by viewModel.isLoading
    val errorMessage by viewModel.errorMessage
    val context = LocalContext.current

    LaunchedEffect(errorMessage) {
        errorMessage?.let { Toast.makeText(context, it, Toast.LENGTH_LONG).show() }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Welcome Back", style = MaterialTheme.typography.headlineMedium)
        Text("Enter your details below", style = MaterialTheme.typography.bodyLarge, color = Color.Gray)
        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email Address") }, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(value = password, onValueChange = { password = it }, label = { Text("Password") }, visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(24.dp))

        if (isLoading) {
            CircularProgressIndicator()
        } else {
            Button(
                onClick = { viewModel.login(email, password, onLoginSuccess) },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                contentPadding = PaddingValues()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Brush.horizontalGradient(colors = listOf(GradientStart, GradientEnd)))
                        .clip(RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Sign In", color = Color.White, fontSize = 16.sp)
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))
        TextButton(onClick = onRegisterClick) {
            Text("Don't have an account? Get Started")
        }
    }
}

@Composable
fun RegisterContent(viewModel: AuthViewModel, onLoginClick: () -> Unit, onRegisterSuccess: () -> Unit) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val isLoading by viewModel.isLoading
    val errorMessage by viewModel.errorMessage
    val context = LocalContext.current

    LaunchedEffect(errorMessage) {
        errorMessage?.let { Toast.makeText(context, it, Toast.LENGTH_LONG).show() }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Get started free.", style = MaterialTheme.typography.headlineMedium)
        Text("Free forever. No credit card needed.", style = MaterialTheme.typography.bodyLarge, color = Color.Gray)
        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Your name") }, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email Address") }, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(value = password, onValueChange = { password = it }, label = { Text("Password") }, visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(24.dp))

        if (isLoading) {
            CircularProgressIndicator()
        } else {
            Button(
                onClick = { viewModel.register(name, email, password, onRegisterSuccess) },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                contentPadding = PaddingValues()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Brush.horizontalGradient(colors = listOf(GradientStart, GradientEnd)))
                        .clip(RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Sign up", color = Color.White, fontSize = 16.sp)
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        TextButton(onClick = onLoginClick) {
            Text("Already have an account? Sign in")
        }
    }
}

class AuthViewModelFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AuthViewModel() as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
