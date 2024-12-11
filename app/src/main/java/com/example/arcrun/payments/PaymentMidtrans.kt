package com.example.arcrun.payments

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.arcrun.R
import com.google.firebase.database.FirebaseDatabase
import com.midtrans.sdk.corekit.core.MidtransSDK
import com.midtrans.sdk.corekit.models.ItemDetails
import com.midtrans.sdk.corekit.core.TransactionRequest
import com.midtrans.sdk.corekit.models.snap.TransactionResult
import com.midtrans.sdk.uikit.api.model.CustomColorTheme
import com.midtrans.sdk.uikit.external.UiKitApi
import com.midtrans.sdk.uikit.internal.util.UiKitConstants

class PaymentMidtrans : AppCompatActivity() {

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

        Log.d("PaymentMidtrans", "PaymentMidtrans activity started")

        // Inisialisasi Midtrans SDK
        UiKitApi.Builder()
            .withContext(applicationContext)
            .withMerchantClientKey("SB-Mid-client-O1xxtK0Y2EGeDKIh")
            .withColorTheme(CustomColorTheme("#FFE51255", "#B61548", "#FFE51255"))
            .withMerchantUrl("https://3805-114-10-151-98.ngrok-free.app/get-snap-token/")
            .enableLog(true)
            .build()
        startPayment()
    }

    private fun startPayment() {
        val eventId = intent.getStringExtra("event_id")
        val eventName = intent.getStringExtra("event_name")
        val eventPrice = intent.getIntExtra("event_price", 0)
        val userId = intent.getStringExtra("user_id")
        if (eventId != null && eventName != null && userId != null) {
            val snapToken = intent.getStringExtra("snap_token")

            if (snapToken.isNullOrEmpty()) {
                Log.e("PaymentMidtrans", "Snap Token tidak ditemukan!")
                Toast.makeText(this, "Snap Token tidak ditemukan!", Toast.LENGTH_SHORT).show()
                return
            }

            saveSnapTokenToFirebase(userId, eventId, snapToken, eventPrice, eventName)

            val transactionRequest = TransactionRequest(eventId, eventPrice.toDouble()).apply {
                itemDetails = arrayListOf(
                    ItemDetails(eventId, eventPrice.toDouble(), 1, eventName)
                )
            }

            MidtransSDK.getInstance().transactionRequest = transactionRequest

            // Mulai UI pembayaran menggunakan Snap Token
            UiKitApi.getDefaultInstance().startPaymentUiFlow(
                this,
                launcher,
                snapToken
            )
        } else {
            Toast.makeText(this, "Data event tidak lengkap", Toast.LENGTH_SHORT).show()
        }
    }

    fun saveSnapTokenToFirebase(userId: String, orderId: String, token: String, price: Int, eventName: String) {
        val database = FirebaseDatabase.getInstance()
        val ref = database.getReference("tokens").child(userId)

        val tokenData = mapOf(
            "orderId" to orderId,
            "snapToken" to token,
            "price" to price,
            "eventName" to eventName
        )

        ref.push().setValue(tokenData).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.d("PaymentMidtrans", "Token berhasil disimpan di Firebase")
            } else {
                Log.e("PaymentMidtrans", "Gagal menyimpan token di Firebase: ${task.exception?.message}")
            }
        }
    }
}