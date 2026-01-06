package com.example.budgettrackerku.data.model

data class BudgetNotification(
    var id: String = "",
    var userId: String = "",
    var title: String = "",
    var message: String = "",
    var date: Long = 0L,
    var isRead: Boolean = false
)
