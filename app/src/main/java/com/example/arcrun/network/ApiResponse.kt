package com.example.arcrun.network

data class ApiResponse<T>(
    val statusCode: Int,
    val message: String,
    val data: T
)
