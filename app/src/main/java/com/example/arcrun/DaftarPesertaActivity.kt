package com.example.arcrun

import GetUser
import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.arcrun.databinding.ActivityDaftarPesertaBinding
import com.example.arcrun.models.Participant
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.midtrans.sdk.uikit.api.model.CustomColorTheme
import com.midtrans.sdk.uikit.external.UiKitApi
import com.example.arcrun.network.NetworkUtils
import com.google.firebase.database.GenericTypeIndicator
import java.util.*

class DaftarPesertaActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDaftarPesertaBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var clientKey: String
    private lateinit var baseUrl: String
    private var eventPrice: Int = 0
    private var participant: Participant? = null

    private lateinit var paymentLauncher: ActivityResultLauncher<Intent>
    private lateinit var userHandler: GetUser

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDaftarPesertaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        val eventId = intent.getStringExtra("event_id")
        val eventName = intent.getStringExtra("event_name")
        eventPrice = intent.getIntExtra("event_price", 0)

        Log.d("DaftarPesertaActivity", "Event ID: $eventId")
        Log.d("DaftarPesertaActivity", "Event Name: $eventName")
        Log.d("DaftarPesertaActivity", "Event Price: $eventPrice")

        clientKey = "Mid-client-6RENkc4aeBYKJfVf"
        baseUrl = "https://possible-flying-mammal.ngrok-free.app"

        setupDropdowns()
        setupUI()
        setupSubmitButton(eventId, eventName, eventPrice)

        paymentLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                Toast.makeText(this, "Pembayaran berhasil!", Toast.LENGTH_SHORT).show()
                // Get transaction data from the result
                val transactionStatus = result.data?.getStringExtra("transaction_status") ?: "Unknown"
                val paymentMethod = result.data?.getStringExtra("payment_method") ?: "Unknown"
                val transactionId = result.data?.getStringExtra("transaction_id") ?: "Unknown"

                // Save the transaction data to Firebase
                saveDataToFirebase(transactionStatus, paymentMethod, transactionId)
            } else {
                Toast.makeText(this, "Pembayaran dibatalkan atau gagal", Toast.LENGTH_SHORT).show()
            }
        }

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
        }
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
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        DatePickerDialog(this, { _, year, month, day ->
            binding.tanggalLahirInput.setText("$day/${month + 1}/$year")
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
    }

    private fun setupSubmitButton(eventId: String?, eventName: String?, eventPrice: Int) {
        binding.submitBtn.setOnClickListener {
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

            if (name.isEmpty() || ttl.isEmpty() || email.isEmpty() || phone.isEmpty() || emergencyContact.isEmpty() || namaBib.isEmpty()) {
                Toast.makeText(this, "Semua kolom harus diisi!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val userId = firebaseAuth.currentUser?.uid
            if (userId == null) {
                Toast.makeText(this, "Pengguna tidak terautentikasi!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val orderId = "ORDER-${System.currentTimeMillis()}"

            participant = Participant(
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

            fetchSnapToken(orderId, eventPrice, participant!!)
        }
    }

    private fun fetchSnapToken(orderId: String, grossAmount: Int, participant: Participant) {
        NetworkUtils.generateSnapToken(orderId, grossAmount) { snapToken ->
            runOnUiThread {
                if (snapToken != null) {
                    initiatePayment(snapToken, participant)
                } else {
                    Toast.makeText(this, "Gagal mendapatkan Snap Token", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun initiatePayment(snapToken: String, participant: Participant) {
        UiKitApi.Builder()
            .withContext(this)
            .withMerchantClientKey(clientKey)
            .withColorTheme(CustomColorTheme("#002855", "#383942", "#FFE51255"))
            .withMerchantUrl(baseUrl)
            .enableLog(true)
            .build()

        UiKitApi.getDefaultInstance().startPaymentUiFlow(this, paymentLauncher, snapToken)
    }

    private fun saveDataToFirebase(transactionStatus: String, paymentMethod: String, transactionId: String) {
        val userId = firebaseAuth.currentUser?.uid
        if (userId == null) {
            Toast.makeText(this, "Pengguna tidak terautentikasi", Toast.LENGTH_SHORT).show()
            return
        }

        participant?.let {
            val updatedParticipant = it.copy(
                // Tidak ada data transaksi yang dimasukkan
            )

            val participantsRef = database.getReference("Peserta").push()
            Log.d("DaftarPesertaActivity", "Saving data to Firebase at ${participantsRef.key}")

            participantsRef.setValue(updatedParticipant)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        // Update user's orders array
                        val userRef = database.getReference("users").child(userId)
                        userRef.child("orders").get().addOnSuccessListener { dataSnapshot ->
                            val orders = dataSnapshot.getValue(object : GenericTypeIndicator<List<String>>() {})?.toMutableList() ?: mutableListOf()
                            orders.add(it.orderId)

                            userRef.child("orders").setValue(orders)
                                .addOnCompleteListener { updateTask ->
                                    if (updateTask.isSuccessful) {
                                        Toast.makeText(this, "Data berhasil disimpan dan order diperbarui!", Toast.LENGTH_SHORT).show()
                                        finish()
                                    } else {
                                        Toast.makeText(this, "Gagal memperbarui order", Toast.LENGTH_SHORT).show()
                                        updateTask.exception?.let { e ->
                                            Log.e("DaftarPesertaActivity", "Error updating orders: ${e.message}")
                                        }
                                    }
                                }
                        }.addOnFailureListener { e ->
                            Toast.makeText(this, "Gagal mengambil data order", Toast.LENGTH_SHORT).show()
                            Log.e("DaftarPesertaActivity", "Error fetching orders: ${e.message}")
                        }
                    } else {
                        Toast.makeText(this, "Gagal menyimpan data", Toast.LENGTH_SHORT).show()
                        task.exception?.let { e ->
                            Log.e("DaftarPesertaActivity", "Error saving data: ${e.message}")
                        }
                    }
                }
        } ?: run {
            Toast.makeText(this, "Data peserta tidak valid", Toast.LENGTH_SHORT).show()
        }
    }
}