package com.example.budgettrackerku.util

import com.example.budgettrackerku.R

object CategoryUtils {
    fun getCategoryLabel(categoryName: String): Int {
        return when (categoryName) {
            "Food" -> R.string.cat_food
            "Social Life" -> R.string.cat_social
            "Pets" -> R.string.cat_pets
            "Transport" -> R.string.cat_transport
            "Culture" -> R.string.cat_culture
            "Household" -> R.string.cat_household
            "Apparel" -> R.string.cat_apparel
            "Beauty" -> R.string.cat_beauty
            "Health" -> R.string.cat_health
            "Education" -> R.string.cat_education
            "Gift" -> R.string.cat_gift
            "Other" -> R.string.cat_other
            "Allowance" -> R.string.cat_allowance
            "Salary" -> R.string.cat_salary
            "Petty cash" -> R.string.cat_petty_cash
            "Bonus" -> R.string.cat_bonus
            else -> R.string.cat_other
        }
    }
}
