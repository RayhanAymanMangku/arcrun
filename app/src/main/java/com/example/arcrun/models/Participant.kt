package com.example.arcrun.models

data class Participant(
    val name: String = "",
    val gender: String = "",
    val ttl: String = "",
    val email: String = "",
    val phone: String = "",
    val emergencyContact: String = "",
    val category: String = "",
    val jerseySize: String = "",
    val namaBib: String = "",
    val riwayatPenyakit: String = "",
    val eventId: String = "",
    val userId: String = "",
    val orderId: String = "",
    val eventPrice: Int = 0,
    var eventName: String? = null, // Tambahan
    var locationName: String? = null, // Tambahan
    var waktu_mulai: String? = null // Tambahan
) {
    // No-argument constructor required for Firebase
    constructor() : this("", "", "", "", "", "", "", "", "", "", "", "", "", 0, "","","")
}