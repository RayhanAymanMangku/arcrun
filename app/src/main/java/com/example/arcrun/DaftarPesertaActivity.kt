package com.example.arcrun

import GetUser
import Participant
import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.arcrun.databinding.ActivityDaftarPesertaBinding
import com.example.arcrun.payments.PaymentMidtrans
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import org.json.JSONObject
import java.util.*

class DaftarPesertaActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDaftarPesertaBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var requestQueue: RequestQueue

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDaftarPesertaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        requestQueue = Volley.newRequestQueue(this)

        setupDropdowns()
        setupUI()
        setupUserProfile()
    }

    private fun setupDropdowns() {
        val kategoriList = listOf("Lari 5K", "Lari 10K", "Maraton")
        val genderList = listOf("Laki-Laki", "Perempuan")
        val sizeJerseyList = listOf("S", "M", "L", "XL")

        binding.kategoriInput.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, kategoriList)
        binding.jenisKelaminInput.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, genderList)
        binding.sizeJerseyInput.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, sizeJerseyList)
    }

    private fun setupUI() {
        binding.tanggalLahirInput.setOnClickListener { showDatePicker() }
        binding.submitBtn.setOnClickListener { submitForm() }
    }

    private fun setupUserProfile() {
        val userNameTextView = findViewById<TextView>(R.id.textViewAyman)
        val userProfileImage = findViewById<ImageView>(R.id.profileButton)

        GetUser().getCurrentUser { user ->
            userNameTextView.text = user.name
            Glide.with(this)
                .load(user.profileImageUrl ?: R.drawable.default_profile_image)
                .apply(RequestOptions.circleCropTransform())
                .into(userProfileImage)

            userProfileImage.setOnClickListener {
                startActivity(Intent(this, UserProfile::class.java))
            }
        }
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        DatePickerDialog(this, { _, year, month, day ->
            binding.tanggalLahirInput.setText("$day/${month + 1}/$year")
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
    }

    private fun submitForm() {
        val name = binding.namaPesertaInput.text.toString()
        val gender = binding.jenisKelaminInput.selectedItem.toString()
        val ttl = binding.tanggalLahirInput.text.toString()
        val email = binding.emailInput.text.toString()
        val phone = binding.teleponInput.text.toString().toDoubleOrNull()
        val emergencyContact = binding.kontakDaruratInput.text.toString().toDoubleOrNull()
        val category = binding.kategoriInput.selectedItem.toString()
        val jerseySize = binding.sizeJerseyInput.selectedItem.toString()
        val namaBib = binding.bibInput.text.toString()
        val riwayatPenyakit = binding.riwayatPenyakitInput.text.toString()
        val eventId = intent.getStringExtra("event_id")
        val eventName = intent.getStringExtra("event_name")
        val eventPrice = intent.getIntExtra("event_price", 0)

        if (name.isEmpty() || ttl.isEmpty() || email.isEmpty() || phone == null || emergencyContact == null || namaBib.isEmpty() || riwayatPenyakit.isEmpty()) {
            Toast.makeText(this, "Semua kolom harus diisi!", Toast.LENGTH_SHORT).show()
            return
        }

        val userId = firebaseAuth.currentUser?.uid
        val orderId = "ORDER-${System.currentTimeMillis()}"

        if (userId != null) {
            // Simpan data peserta ke database Firebase
            val participantData = Participant(
                name, gender, ttl, email, phone, emergencyContact, category, jerseySize, namaBib, orderId, eventId ?: "", userId, riwayatPenyakit
            )

            val participantsRef = database.getReference("Peserta").push()
            participantsRef.setValue(participantData).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Pendaftaran berhasil!", Toast.LENGTH_SHORT).show()
                    // Lanjutkan ke pembayaran setelah pendaftaran berhasil
                    startPayment(userId, orderId, eventPrice, eventName ?: "Event")
                } else {
                    Toast.makeText(this, "Terjadi kesalahan, coba lagi!", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            Toast.makeText(this, "Pengguna tidak terautentikasi!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun startPayment(userId: String, orderId: String, price: Int, eventName: String) {
        val url = "https://8d90-114-10-150-102.ngrok-free.app/create-snap-token"

        val jsonObject = JSONObject().apply {
            put("userId", userId)
            put("orderId", orderId)
            put("price", price)
            put("eventName", eventName)
        }

        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.POST, url, jsonObject,
            Response.Listener { response ->
                val snapToken = response.optString("snapToken")
                if (!snapToken.isNullOrEmpty()) {
                    Log.d("PaymentMidtrans", "Snap Token received: $snapToken")
                    // Proceed to Midtrans Payment with snapToken
                    proceedWithPayment(snapToken, orderId, price, eventName)
                } else {
                    Toast.makeText(this, "Snap Token tidak valid!", Toast.LENGTH_SHORT).show()
                }
            },
            Response.ErrorListener { error ->
                Log.e("PaymentMidtrans", "Error fetching Snap Token: ${error.message}")
                Toast.makeText(this, "Gagal mengambil Snap Token!", Toast.LENGTH_SHORT).show()
            }
        )

        requestQueue.add(jsonObjectRequest)
    }

    private fun proceedWithPayment(snapToken: String, orderId: String, price: Int, eventName: String) {
        val intent = Intent(this, PaymentMidtrans::class.java).apply {
            putExtra("snap_token", snapToken)
            putExtra("order_id", orderId)
            putExtra("event_price", price)
            putExtra("event_name", eventName)
        }
        startActivity(intent)
    }
}
