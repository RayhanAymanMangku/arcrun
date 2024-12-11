package com.example.arcrun.payments

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.midtrans.sdk.corekit.core.MidtransSDK
import com.midtrans.sdk.corekit.models.ItemDetails
import com.midtrans.sdk.corekit.core.TransactionRequest
import com.midtrans.sdk.corekit.models.snap.TransactionResult
import com.midtrans.sdk.uikit.api.model.CustomColorTheme
import com.midtrans.sdk.uikit.external.UiKitApi
import com.midtrans.sdk.uikit.internal.util.UiKitConstants
import org.json.JSONObject

class PaymentMidtrans : AppCompatActivity() {

    private lateinit var requestQueue: RequestQueue

    private val launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result?.resultCode == RESULT_OK) {
            result.data?.let {
                val transactionResult = it.getSerializableExtra(UiKitConstants.KEY_TRANSACTION_RESULT) as? TransactionResult
                if (transactionResult?.response?.transactionStatus == "capture") {
                    Toast.makeText(this, "Transaksi berhasil! ID: ${transactionResult.response?.transactionId}", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(this, "Transaksi gagal atau pending!", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inisialisasi Volley Request Queue
        requestQueue = Volley.newRequestQueue(this)

        Log.d("PaymentMidtrans", "PaymentMidtrans activity started")

        // Inisialisasi Midtrans SDK
        UiKitApi.Builder()
            .withContext(applicationContext)
            .withMerchantClientKey("SB-Mid-client-O1xxtK0Y2EGeDKIh") // Ganti dengan Client Key Midtrans Anda
            .withColorTheme(CustomColorTheme("#FFE51255", "#B61548", "#FFE51255"))
            .withMerchantUrl("https://8d90-114-10-150-102.ngrok-free.app/") // Ganti dengan URL server Anda
            .enableLog(true)
            .build()

        // Memulai proses pembayaran dengan data yang ada
        fetchSnapTokenAndStartPayment()
    }

    private fun fetchSnapTokenAndStartPayment() {
        // Ambil data yang diperlukan dari intent
        val eventId = intent.getStringExtra("event_id")
        val eventName = intent.getStringExtra("event_name")
        val eventPrice = intent.getIntExtra("event_price", 0)
        val userId = intent.getStringExtra("user_id")
        val orderId = "ORDER-${System.currentTimeMillis()}" // Generate order ID yang unik

        if (eventId != null && eventName != null && userId != null) {
            // URL untuk mengambil Snap Token dari server
            val url = "https://8d90-114-10-150-102.ngrok-free.app/get-snap-token/$userId/$orderId"

            val jsonObjectRequest = JsonObjectRequest(
                Request.Method.GET, url, null,
                Response.Listener { response ->
                    val snapToken = response.optString("snapToken")
                    if (snapToken.isNullOrEmpty()) {
                        Log.e("PaymentMidtrans", "Snap Token not found, creating a new one...")
                        createSnapToken(orderId, eventPrice, eventName, userId)
                    } else {
                        Log.d("PaymentMidtrans", "Snap Token retrieved: $snapToken")
                        startPaymentWithSnapToken(orderId, snapToken, eventPrice, eventName)
                    }
                },
                Response.ErrorListener { error ->
                    Log.e("PaymentMidtrans", "Failed to fetch Snap Token: ${error.message}")
                    Toast.makeText(this, "Gagal mengambil Snap Token!", Toast.LENGTH_SHORT).show()
                }
            )

            requestQueue.add(jsonObjectRequest)
        } else {
            Toast.makeText(this, "Data transaksi tidak lengkap!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun createSnapToken(orderId: String, price: Int, eventName: String, userId: String) {
        val url = "https://8d90-114-10-150-102.ngrok-free.app/create-snap-token"
        val requestBody = JSONObject().apply {
            put("userId", userId)
            put("orderId", orderId)
            put("price", price)
            put("eventName", eventName)
        }

        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.POST, url, requestBody,
            Response.Listener { response ->
                val snapToken = response.optString("snapToken")
                if (!snapToken.isNullOrEmpty()) {
                    Log.d("PaymentMidtrans", "New Snap Token created: $snapToken")
                    startPaymentWithSnapToken(orderId, snapToken, price, eventName)
                } else {
                    Toast.makeText(this, "Gagal membuat Snap Token!", Toast.LENGTH_SHORT).show()
                }
            },
            Response.ErrorListener { error ->
                Log.e("PaymentMidtrans", "Failed to create Snap Token: ${error.message}")
                Toast.makeText(this, "Gagal membuat Snap Token!", Toast.LENGTH_SHORT).show()
            }
        )

        requestQueue.add(jsonObjectRequest)
    }

    private fun startPaymentWithSnapToken(orderId: String, snapToken: String, price: Int, eventName: String) {
        try {
            // Log Snap Token
            Log.d("PaymentMidtrans", "Starting payment with Snap Token: $snapToken")

            // Buat Transaction Request untuk Midtrans
            val transactionRequest = TransactionRequest(orderId, price.toDouble()).apply {
                itemDetails = arrayListOf(
                    ItemDetails(orderId, price.toDouble(), 1, eventName)
                )
            }

            // Set Transaction Request pada Midtrans SDK
            MidtransSDK.getInstance().transactionRequest = transactionRequest

            // Mulai pembayaran dengan Snap Token
            UiKitApi.getDefaultInstance().startPaymentUiFlow(this, launcher, snapToken)

            Log.d("PaymentMidtrans", "Payment UI flow started successfully")
        } catch (exception: Exception) {
            Log.e("PaymentMidtrans", "Error starting payment flow: ${exception.message}")
            Toast.makeText(this, "Terjadi kesalahan saat memulai pembayaran!", Toast.LENGTH_SHORT).show()
        }
    }
}
