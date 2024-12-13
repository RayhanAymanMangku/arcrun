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
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.arcrun.databinding.ActivityDaftarPesertaBinding
import com.example.arcrun.models.OrderRequest
import com.example.arcrun.network.ApiClient
import com.example.arcrun.network.ApiService
import com.example.arcrun.payments.PaymentMidtrans
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

class DaftarPesertaActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDaftarPesertaBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var database: FirebaseDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDaftarPesertaBinding.inflate(layoutInflater)
        setContentView(binding.root)
        Log.d("DaftarPesertaActivity", "Intent extras: ${intent.extras}")

        firebaseAuth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

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
        // Mengambil data event dari intent pada saat form disubmit
        val eventId = intent.getStringExtra("event_id")
        val eventName = intent.getStringExtra("event_name")
        val eventPrice = intent.getIntExtra("event_price", 0)

        // Log untuk memeriksa data event yang diambil
        Log.d("DaftarPesertaActivity", "Event ID: $eventId")
        Log.d("DaftarPesertaActivity", "Event Name: $eventName")
        Log.d("DaftarPesertaActivity", "Event Price: $eventPrice")

        // Validasi data form
        val name = binding.namaPesertaInput.text.toString()
        val gender = binding.jenisKelaminInput.selectedItem.toString()
        val ttl = binding.tanggalLahirInput.text.toString()
        val email = binding.emailInput.text.toString()
        val phone = binding.teleponInput.text.toString()
        val emergencyContact = binding.kontakDaruratInput.text.toString()
        val category = binding.kategoriInput.selectedItem.toString()
        val jerseySize = binding.sizeJerseyInput.selectedItem.toString()
        val namaBib = binding.bibInput.text.toString()
        val riwayatPenyakit = binding.riwayatPenyakitInput.text.toString()

        // Validasi jika event data tidak ditemukan
        if (eventId == null || eventName == null || eventPrice == 0) {
            Log.e("DetailEventActivity", "Event data not found!")
            Toast.makeText(this, "Data event tidak valid!", Toast.LENGTH_SHORT).show()
            return
        }

        // Validasi form peserta
        if (name.isEmpty() || ttl.isEmpty() || email.isEmpty() || phone.isEmpty() || emergencyContact.isEmpty() || namaBib.isEmpty() || riwayatPenyakit.isEmpty()) {
            Toast.makeText(this, "Semua kolom harus diisi!", Toast.LENGTH_SHORT).show()
            return
        }

        // Validasi harga event
        if (eventPrice <= 0) {
            Toast.makeText(this, "Harga event tidak valid!", Toast.LENGTH_SHORT).show()
            return
        }

        val userId = firebaseAuth.currentUser?.uid
        val orderId = "ORDER-${System.currentTimeMillis()}"

        if (userId != null) {
            val participant = Participant(
                name = name,
                gender = gender,
                ttl = ttl,
                email = email,
                phone = phone,
                emergencyContact = emergencyContact,
                category = category,
                jerseySize = jerseySize,
                namaBib = namaBib,
                riwayatPenyakit = riwayatPenyakit,
                orderId = orderId,
                eventId = eventId ?: "",
                userId = userId,
                eventPrice = eventPrice
            )

            val participantsRef = database.getReference("Peserta").push()
            participantsRef.setValue(participant).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Pendaftaran berhasil!", Toast.LENGTH_SHORT).show()
                    createOrder(userId, participant, eventPrice, eventName ?: "Event")
                } else {
                    Toast.makeText(this, "Terjadi kesalahan, coba lagi!", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            Toast.makeText(this, "Pengguna tidak terautentikasi!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun createOrder(userId: String, participant: Participant, eventPrice: Int, eventName: String) {
        val orderRequest = OrderRequest(
            orderData = participant,
            eventPrice = eventPrice
        )
        val apiService = ApiClient.createService(ApiService::class.java)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = apiService.createOrder(userId, orderRequest)

                withContext(Dispatchers.Main) {
                    if (response.statusCode == 200) {
                        val snapToken = response.data.midtransToken.token
                        if (!snapToken.isNullOrEmpty()) {
                            startPayment(snapToken, participant.orderId, eventPrice, eventName)
                        } else {
                            Toast.makeText(this@DaftarPesertaActivity, "Snap Token tidak ditemukan!", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(this@DaftarPesertaActivity, "Gagal membuat pesanan!", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@DaftarPesertaActivity, "Terjadi kesalahan: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    private fun startPayment(snapToken: String, orderId: String, eventPrice: Int, eventName: String) {
        val intent = Intent(this, PaymentMidtrans::class.java).apply {
            putExtra("snap_token", snapToken)
            putExtra("order_id", orderId)
            putExtra("event_price", eventPrice)
            putExtra("event_name", eventName)
        }
        startActivity(intent)
    }

}
