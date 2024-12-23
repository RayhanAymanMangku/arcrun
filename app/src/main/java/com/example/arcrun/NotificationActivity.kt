package com.example.arcrun

import GetUser
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.arcrun.adapter.NotificationAdapter
import com.example.arcrun.models.Transaction
import com.example.arcrun.network.ApiService
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class NotificationActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: NotificationAdapter
    private val notificationList = mutableListOf<Transaction>()
    private lateinit var database: DatabaseReference
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var userHandler: GetUser

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://possible-flying-mammal.ngrok-free.app")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val api = retrofit.create(ApiService::class.java)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notification)

        firebaseAuth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().getReference("Users")

        recyclerView = findViewById(R.id.displayAllNotification)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = NotificationAdapter()
        recyclerView.adapter = adapter

        userHandler = GetUser()
        val userNameTextView = findViewById<TextView>(R.id.textViewAyman)
        val userProfileImage = findViewById<ImageView>(R.id.profileButton)

        userHandler.getCurrentUser { user ->
            userNameTextView.text = user.name
            if (user.profileImageUrl != null) {
                Glide.with(this)
                    .load(user.profileImageUrl)
                    .apply(RequestOptions.circleCropTransform())
                    .into(userProfileImage)
            } else {
                userProfileImage.setImageResource(R.drawable.default_profile_image)
            }

            userProfileImage.setOnClickListener {
                val navigateToProfile = Intent(this, UserProfile::class.java)
                startActivity(navigateToProfile)
            }

            fetchUserOrders(user.id)
        }

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationContainer)
        val bottomNavigation = BottomNavigation(this)
        bottomNavigation.setupBottomNavigation(bottomNavigationView)

    }

    private fun fetchUserOrders(userId: String) {
        lifecycleScope.launch {
            try {
                Log.d("NotificationActivity", "Fetching orders for userId: $userId")

                // Debug request
                val response = api.getUserOrders(userId)
                Log.d("NotificationActivity", "Orders Response: $response")

                response.forEach { orderId ->
                    Log.d("NotificationActivity", "Order ID: $orderId")
                    getTransactionStatus(orderId)
                }
            } catch (e: HttpException) {
                Log.e("NotificationActivity", "HTTP Error: ${e.message()} ${e.response()?.errorBody()?.string()}")

                withContext(Dispatchers.Main) {
                    Toast.makeText(this@NotificationActivity, "Gagal mengambil order: ${e.message()}", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Log.e("NotificationActivity", "Error: ${e.message}")

                withContext(Dispatchers.Main) {
                    Toast.makeText(this@NotificationActivity, "Terjadi kesalahan: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun getTransactionStatus(orderId: String) {
        lifecycleScope.launch {
            try {
                Log.d("NotificationActivity", "Requesting status for orderId: $orderId")

                // Panggil API untuk mendapatkan status transaksi
                val response = api.getTransactionStatus(orderId)

                Log.d("NotificationActivity", "Transaction Status Response: $response")

                withContext(Dispatchers.Main) {
                    val transaction = Transaction(
                        transactionId = response.transaction_id ?: "Unknown",
                        orderId = response.order_id ?: "Unknown",
                        status = response.transaction_status ?: "Unknown",
                        amount = response.gross_amount?.toDoubleOrNull()?.toInt() ?: 0,
                        date = response.transaction_time ?: "N/A",
                        eventName = "Event Default" // Sesuaikan jika data ada di server
                    )

                    Log.d("NotificationActivity", "Transaction added: $transaction")

                    notificationList.add(transaction)
                    adapter.setData(notificationList) // Perbarui adapter dengan list terbaru
                }

            } catch (e: HttpException) {
                Log.e("NotificationActivity", "HTTP Error: ${e.message()}")
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@NotificationActivity, "Gagal mengambil status transaksi: ${e.message()}", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Log.e("NotificationActivity", "Error: ${e.message}")
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@NotificationActivity, "Terjadi kesalahan: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}