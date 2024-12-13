package com.example.arcrun.payments

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.arcrun.utils.MidtransHelper
import com.midtrans.sdk.uikit.api.model.TransactionResult

class PaymentMidtrans : AppCompatActivity() {
    private lateinit var launcher: ActivityResultLauncher<Intent>
    private lateinit var midtransHelper: MidtransHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Setup result launcher for payment result
        launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val transactionResult = result.data?.getParcelableExtra<TransactionResult>(
                    com.midtrans.sdk.uikit.internal.util.UiKitConstants.KEY_TRANSACTION_RESULT
                )
                Toast.makeText(this, "Transaction ID: ${transactionResult?.transactionId}", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(this, "Transaction was canceled or failed", Toast.LENGTH_LONG).show()
            }
            finish()
        }

        val snapToken = intent.getStringExtra("snap_token") ?: return

        midtransHelper = MidtransHelper(this)
        midtransHelper.initialize("SB-Mid-client-O1xxtK0Y2EGeDKIh", "https://possible-flying-mammal.ngrok-free.app")

        midtransHelper.startPaymentWithSnapToken(this, snapToken, launcher)
    }
}
