package com.example.arcrun.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import com.midtrans.sdk.uikit.api.model.CustomColorTheme
import com.midtrans.sdk.uikit.external.UiKitApi

class MidtransHelper(private val context: Context) {
    // Inisialisasi SDK Midtrans
    fun initialize(clientKey: String, baseUrl: String) {
        UiKitApi.Builder()
            .withContext(context)
            .withMerchantClientKey(clientKey)
            .withColorTheme(CustomColorTheme("#FFE51255", "#B61548", "#FFE51255"))
            .withMerchantUrl(baseUrl)
            .enableLog(true)
            .build()
    }

    fun startPaymentWithSnapToken(activity: Activity, snapToken: String, launcher: ActivityResultLauncher<Intent>) {
        UiKitApi.getDefaultInstance().startPaymentUiFlow(activity, launcher, snapToken)
    }
}
