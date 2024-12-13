// ApiService.kt
package com.example.arcrun.network

import com.example.arcrun.models.OrderRequest
import com.example.arcrun.models.OrderResponse
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path

interface ApiService {
    @POST("api/create-order/{userId}")
    suspend fun createOrder(
        @Path("userId") userId: String,
        @Body orderRequest: OrderRequest
    ):ApiResponse<OrderResponse>
}
