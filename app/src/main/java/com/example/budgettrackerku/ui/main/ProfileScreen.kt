package com.example.budgettrackerku.ui.main

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import android.widget.Toast
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.budgettrackerku.ui.theme.*
import com.example.budgettrackerku.viewmodel.BudgetViewModel
import androidx.compose.ui.res.stringResource
import com.example.budgettrackerku.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController,
    viewModel: BudgetViewModel,
    onLogout: () -> Unit
) {
    val userName by viewModel.userName.collectAsState()
    val userId = viewModel.userId
    val context = LocalContext.current
    
    // Manage email state locally for immediate update since it's not a flow
    var userEmail by remember { mutableStateOf(viewModel.userEmail) }
    
    val clipboardManager = LocalClipboardManager.current
    
    // Dialog States
    var showNameDialog by remember { mutableStateOf(false) }
    var showEmailDialog by remember { mutableStateOf(false) }
    var tempName by remember { mutableStateOf("") }
    var tempEmail by remember { mutableStateOf("") }

    if (showNameDialog) {
        EditInfoDialog(
            title = stringResource(R.string.edit_nickname_title),
            initialValue = userName,
            onDismiss = { showNameDialog = false },
            onConfirm = { newName ->
                viewModel.updateUserName(newName)
                Toast.makeText(context, context.getString(R.string.nickname_updated), Toast.LENGTH_SHORT).show()
                showNameDialog = false
            }
        )
    }

    Scaffold(
        topBar = {
            BudgetTrackerTopBar(
                title = "",
                navigationIcon = Icons.AutoMirrored.Filled.ArrowBack,
                onMenuClick = { navController.popBackStack() },
                showNotificationIcon = false
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            // Profile Picture Circle
            Box(contentAlignment = Alignment.BottomEnd) {
                // Placeholder image or first letter of name
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                   Text(
                       text = if (userName.isNotEmpty()) userName.take(1).uppercase() else "U",
                       fontSize = 48.sp,
                       fontWeight = FontWeight.Bold,
                       color = MaterialTheme.colorScheme.onSurfaceVariant
                   )
                }
                
                // Camera Icon overlay
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .offset(x = 0.dp, y = 0.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surface)
                        .border(2.dp, MaterialTheme.colorScheme.background, CircleShape)
                        .clickable { /* Handle photo upload */ },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.CameraAlt,
                        contentDescription = stringResource(R.string.change_photo),
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // "Your profile" Header
            Text(
                text = stringResource(R.string.your_profile),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 14.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )

            // Info Card
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // UID
                    ProfileRowItem(
                        label = stringResource(R.string.uid),
                        value = userId,
                        icon = Icons.Default.ContentCopy,
                        onIconClick = { 
                            clipboardManager.setText(AnnotatedString(userId))
                        }
                    )
                    
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 1.dp)
                    
                    // Nickname
                    ProfileRowItem(
                        label = stringResource(R.string.nickname),
                        value = userName,
                        icon = Icons.Default.Edit,
                        onIconClick = { 
                            tempName = userName
                            showNameDialog = true 
                        }
                    )
                    
                     HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 1.dp)

                    // Email (Read-only)
                    ProfileRowItem(
                        label = stringResource(R.string.email),
                        value = userEmail,
                        icon = null,
                        onIconClick = null
                    )
                }
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Logout Button
            Button(
                onClick = { onLogout() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(25.dp) 
            ) {
                Text(stringResource(R.string.logout), color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun EditInfoDialog(
    title: String,
    initialValue: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var text by remember { mutableStateOf(initialValue) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = title, color = MaterialTheme.colorScheme.onSurface) },
        text = {
            TextField(
                value = text,
                onValueChange = { text = it },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(text) }) {
                Text(stringResource(R.string.save), color = BluePrimary)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel), color = Color.Gray)
            }
        },
        containerColor = MaterialTheme.colorScheme.surface
    )
}

@Composable
fun ProfileRowItem(
    label: String,
    value: String,
    icon: ImageVector? = null,
    onIconClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(modifier = Modifier.weight(1f)) {
            Text(
                text = label, 
                color = MaterialTheme.colorScheme.onSurface, 
                modifier = Modifier.width(100.dp),
                fontSize = 14.sp
            )
            Text(
                text = value, 
                color = MaterialTheme.colorScheme.onSurfaceVariant, 
                fontSize = 14.sp,
                maxLines = 1
            )
        }
        if (icon != null && onIconClick != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .size(20.dp)
                    .clickable { onIconClick() }
            )
        }
    }
}
