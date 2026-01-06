package com.example.budgettrackerku.data.model

import com.google.firebase.database.IgnoreExtraProperties

// DIUBAH MENJADI CLASS BIASA - INI SOLUSI PALING KUAT & SEDERHANA UNTUK FIREBASE
@IgnoreExtraProperties
class Transaction {
    var id: String = ""
    var userId: String = ""
    var title: String = ""
    var amount: Double = 0.0
    var type: String = ""
    var category: String = ""
    var date: Long = 0L
}
