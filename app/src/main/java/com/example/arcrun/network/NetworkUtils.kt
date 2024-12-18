package com.example.arcrun.network
// untuk transaksi midtrans di server
import android.util.Log

import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import org.json.JSONObject
import java.io.IOException

object NetworkUtils {

    private val client = OkHttpClient()

    fun generateSnapToken(orderId: String, grossAmount: Int, callback: (String?) -> Unit) {
        val url = "https://possible-flying-mammal.ngrok-free.app/generate-snap-token"  // Replace with your server URL

        val jsonObject = JSONObject().apply {
            put("order_id", orderId)
            put("gross_amount", grossAmount)
        }

        val requestBody = RequestBody.create("application/json".toMediaTypeOrNull(), jsonObject.toString())

        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback(null)  // Kalau gagal, kembalikan null
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val responseBody = response.body?.string()
                    Log.d("NetworkUtils", "Response Body: $responseBody")

                    try {
                        val jsonResponse = JSONObject(responseBody)
                        if (jsonResponse.has("snap_token")) {
                            val snapToken = jsonResponse.getString("snap_token")  // Menggunakan "snap_token"
                            Log.d("NetworkUtils", "Snap Token: $snapToken")
                            callback(snapToken)  // Kembalikan token yang diterima
                        } else {
                            Log.e("NetworkUtils", "Snap token not found in response")
                            callback(null)  // Tidak ada token dalam respons
                        }
                    } catch (e: Exception) {
                        Log.e("NetworkUtils", "Error parsing response: ${e.message}")
                        callback(null)  // Kembalikan null jika ada error parsing
                    }
                } else {
                    Log.e("NetworkUtils", "Response failed with code: ${response.code}")
                    callback(null)  // Kembalikan null jika gagal
                }
            }

        })
    }
}
