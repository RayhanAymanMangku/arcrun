package com.example.arcrun.models

data class Transaction(
    val transactionId: String = "",
    val userId: String = "",
    val amount: Int = 0,
    val date: String = "",
    val status: String = "",
    val eventName: String = "",
    val orderId: String = ""
)