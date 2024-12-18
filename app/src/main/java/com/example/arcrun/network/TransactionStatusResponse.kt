package com.example.arcrun.network

data class TransactionStatusResponse(
    val transaction_id: String?,
    val order_id: String?,
    val transaction_status: String?,
    val gross_amount: String?,
    val transaction_time: String?
)
