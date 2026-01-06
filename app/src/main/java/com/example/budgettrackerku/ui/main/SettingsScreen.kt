package com.example.budgettrackerku.ui.main

import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.SettingsBrightness
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.budgettrackerku.R
import com.example.budgettrackerku.ui.theme.BluePrimary
import com.example.budgettrackerku.util.LanguageUtils
import androidx.core.os.LocaleListCompat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController
) {
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    
    // Get current mode
    val currentMode = AppCompatDelegate.getDefaultNightMode()
    var selectedMode by remember { 
        mutableIntStateOf(if (currentMode == AppCompatDelegate.MODE_NIGHT_UNSPECIFIED) AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM else currentMode) 
    }

    var showLanguageDialog by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }

    if (showLanguageDialog) {
        LanguageSelectionDialog(
            onDismiss = { showLanguageDialog = false },
            onLanguageSelected = { langCode ->
                LanguageUtils.setAppLocale(langCode)
                showLanguageDialog = false
            }
        )
    }

    if (showThemeDialog) {
        AlertDialog(
            onDismissRequest = { showThemeDialog = false },
            title = { Text(stringResource(R.string.select_theme)) },
            text = {
                Column {
                    ThemeOption(
                        text = stringResource(R.string.system_default),
                        selected = selectedMode == AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM,
                        onClick = {
                            selectedMode = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
                            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
                            showThemeDialog = false
                        }
                    )
                    ThemeOption(
                        text = stringResource(R.string.light_mode),
                        selected = selectedMode == AppCompatDelegate.MODE_NIGHT_NO,
                        onClick = {
                            selectedMode = AppCompatDelegate.MODE_NIGHT_NO
                            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                            showThemeDialog = false
                        }
                    )
                    ThemeOption(
                        text = stringResource(R.string.dark_mode),
                        selected = selectedMode == AppCompatDelegate.MODE_NIGHT_YES,
                        onClick = {
                            selectedMode = AppCompatDelegate.MODE_NIGHT_YES
                            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                            showThemeDialog = false
                        }
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showThemeDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings_title), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(16.dp)
        ) {
            // General Section
            SettingsSectionHeader(stringResource(R.string.general))
            
            SettingsItem(
                icon = Icons.Default.Language,
                title = stringResource(R.string.language),
                subtitle = if (Locale.getDefault().language == "id") stringResource(R.string.language_id) else stringResource(R.string.language_en),
                onClick = { showLanguageDialog = true }
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

            // Appearance Section
            SettingsSectionHeader(stringResource(R.string.appearance))

            SettingsItem(
                icon = Icons.Default.SettingsBrightness, // Or DarkMode/LightMode icon based on state
                title = stringResource(R.string.theme),
                subtitle = when (selectedMode) {
                    AppCompatDelegate.MODE_NIGHT_NO -> stringResource(R.string.light_mode)
                    AppCompatDelegate.MODE_NIGHT_YES -> stringResource(R.string.dark_mode)
                    else -> stringResource(R.string.system_default)
                },
                onClick = { showThemeDialog = true }
            )
        }
    }
}

@Composable
fun SettingsSectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelMedium,
        color = BluePrimary,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(vertical = 8.dp)
    )
}

@Composable
fun SettingsItem(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }
    }
}

@Composable
fun ThemeOption(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = null // Handled by Row clickable
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

// Reusing LanguageSelectionDialog from HomeScreen but making it more standalone if needed, 
// or I can import it if it's public. Assuming I need to copy or move it.
// Ideally, LanguageSelectionDialog should be in a common file. 
// For now, I'll copy the logic inside SettingsScreen or reuse if I move it.
// Checking HomeScreen I see it is defined there. I will move it to common or redefine here to avoid circular dependencies if any.
// Actually, I can just redefine a simple one here or use the one from HomeScreen if I make it accessible.
// To be safe and clean, I will redefine a generic one or assume I can access it.
// Let's copy it here for now to ensure it works within this file context without import issues for now.

@Composable
fun LanguageSelectionDialog(
    onDismiss: () -> Unit,
    onLanguageSelected: (String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.choose_language)) },
        text = {
            Column {
                LanguageOption(
                    text = stringResource(R.string.language_en),
                    code = "en",
                    onSelect = onLanguageSelected
                )
                HorizontalDivider()
                LanguageOption(
                    text = stringResource(R.string.language_id),
                    code = "id",
                    onSelect = onLanguageSelected
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}

@Composable
fun LanguageOption(text: String, code: String, onSelect: (String) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect(code) }
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text, color = MaterialTheme.colorScheme.onSurface)
        Spacer(modifier = Modifier.weight(1f))
        if (Locale.getDefault().language == code) {
             // Check icon
             // Icon(Icons.Default.Check, ...)
        }
    }
}
