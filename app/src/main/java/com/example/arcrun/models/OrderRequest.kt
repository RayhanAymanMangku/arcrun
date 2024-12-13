package com.example.arcrun.models

import Participant

data class  OrderResponse (
    val id: String,
    val midtransToken: Midtrans
)

data class OrderRequest(
    val orderData: Participant,
    val eventPrice: Int
)
data class Midtrans(
    val token:String,
    val redirect_url: String
)