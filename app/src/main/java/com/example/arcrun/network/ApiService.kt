package com.example.arcrun.network

import retrofit2.http.GET
import retrofit2.http.Path

interface ApiService {
    @GET("transaction-status/{orderId}")
    suspend fun getTransactionStatus(@Path("orderId") orderId: String): TransactionStatusResponse

    @GET("user-orders/{userId}")
    suspend fun getUserOrders(@Path("userId") userId: String): List<String>

}