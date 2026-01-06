package com.example.budgettrackerku.ui.common

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

// Shared Color Palette
val CategoryPalette = listOf(
    Color(0xFFEF5350), // Red
    Color(0xFF42A5F5), // Blue
    Color(0xFF66BB6A), // Green
    Color(0xFFFFEE58), // Yellow
    Color(0xFFAB47BC), // Purple
    Color(0xFFFFA726), // Orange
    Color(0xFF8D6E63), // Brown
    Color(0xFF26A69A)  // Teal
)

fun getCategoryIcon(category: String): ImageVector {
    return when(category) {
        "Food & Drinks", "Food" -> Icons.Default.Fastfood
        "Shopping" -> Icons.Default.ShoppingCart
        "Transportation", "Transport" -> Icons.Default.DirectionsCar
        "Health" -> Icons.Default.FitnessCenter
        "Social Life" -> Icons.Default.Groups
        "Pets" -> Icons.Default.Pets
        "Education" -> Icons.Default.School
        "Entertainment", "Culture" -> Icons.Default.TheaterComedy
        "Apparel" -> Icons.Default.Checkroom
        "Beauty" -> Icons.Default.Face
        "Gift" -> Icons.Default.CardGiftcard
        "Household" -> Icons.Default.Home
        "Allowance" -> Icons.Default.Savings
        "Salary" -> Icons.Default.Work
        "Petty cash", "Pocket Money" -> Icons.Default.AttachMoney
        "Bonus" -> Icons.Default.EmojiEvents
        else -> Icons.Default.MoreHoriz
    }
}

fun getCategoryColor(category: String): Color {
    return when(category) {
        "Food & Drinks", "Food" -> Color(0xFFFF5722) // Red/Orange
        "Shopping" -> Color(0xFF4FC3F7)      // Light Blue
        "Transportation" -> Color(0xFF78909C) // Blueish Gray
        "Health" -> Color(0xFF26A69A)
        else -> {
            val index = kotlin.math.abs(category.hashCode()) % CategoryPalette.size
            CategoryPalette[index]
        }
    }
}

fun getCategoryNameResId(category: String): Int {
    return when(category.trim()) {
        "Food & Drinks", "Food" -> com.example.budgettrackerku.R.string.cat_food
        "Shopping" -> com.example.budgettrackerku.R.string.cat_shopping
        "Transportation", "Transport" -> com.example.budgettrackerku.R.string.cat_transport
        "Health" -> com.example.budgettrackerku.R.string.cat_health
        "Social Life" -> com.example.budgettrackerku.R.string.cat_social
        "Pets" -> com.example.budgettrackerku.R.string.cat_pets
        "Education" -> com.example.budgettrackerku.R.string.cat_education
        "Entertainment", "Culture" -> com.example.budgettrackerku.R.string.cat_culture
        "Apparel" -> com.example.budgettrackerku.R.string.cat_apparel
        "Beauty" -> com.example.budgettrackerku.R.string.cat_beauty
        "Gift" -> com.example.budgettrackerku.R.string.cat_gift
        "Household" -> com.example.budgettrackerku.R.string.cat_household
        "Allowance" -> com.example.budgettrackerku.R.string.cat_allowance
        "Salary" -> com.example.budgettrackerku.R.string.cat_salary
        "Petty cash", "Pocket Money" -> com.example.budgettrackerku.R.string.cat_petty_cash
        "Bonus" -> com.example.budgettrackerku.R.string.cat_bonus
        else -> com.example.budgettrackerku.R.string.cat_other
    }
}
