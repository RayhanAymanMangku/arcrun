package com.example.arcrun

import GetUser
import Participant
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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.midtrans.sdk.uikit.api.model.CustomColorTheme
import com.midtrans.sdk.uikit.external.UiKitApi
import com.example.arcrun.network.NetworkUtils  // Import NetworkUtils
import java.util.*

class DaftarPesertaActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDaftarPesertaBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var clientKey: String
    private lateinit var baseUrl: String
    private var eventPrice: Int = 0  // Event price will be retrieved from the Intent

    // Declare paymentLauncher at the class level
    private lateinit var paymentLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDaftarPesertaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        // Retrieve the event details passed from the previous screen via Intent
        val eventId = intent.getStringExtra("event_id")
        val eventName = intent.getStringExtra("event_name")
        eventPrice = intent.getIntExtra("event_price", 0)

        // Log the event details to verify it's being passed correctly
        Log.d("DaftarPesertaActivity", "Event ID: $eventId")
        Log.d("DaftarPesertaActivity", "Event Name: $eventName")
        Log.d("DaftarPesertaActivity", "Event Price: $eventPrice")

        clientKey = "Mid-client-6RENkc4aeBYKJfVf"  // Replace with your actual client key
        baseUrl = "https://possible-flying-mammal.ngrok-free.app"  // Replace with your server base URL

        setupDropdowns()
        setupUI()
        setupUserProfile()
        showDatePicker()

        setupSubmitButton(eventId, eventName, eventPrice)

        // Initialize the ActivityResultLauncher for payment
        paymentLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                // Handle the result of the payment here
                Toast.makeText(this, "Payment successful", Toast.LENGTH_SHORT).show()
            } else {
                // Handle payment failure or cancellation
                Toast.makeText(this, "Payment failed or canceled", Toast.LENGTH_SHORT).show()
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

    // Function to set up the DatePicker for selecting a date
    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        DatePickerDialog(this, { _, year, month, day ->
            binding.tanggalLahirInput.setText("$day/${month + 1}/$year")
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
    }

    // Function to set up the Submit Button
    private fun setupSubmitButton(eventId: String?, eventName: String?, eventPrice: Int) {
        binding.submitBtn.setOnClickListener {
            // Validate and collect form data
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

            // Validate form inputs
            if (name.isEmpty() || ttl.isEmpty() || email.isEmpty() || phone.isEmpty() || emergencyContact.isEmpty() || namaBib.isEmpty() || riwayatPenyakit.isEmpty()) {
                Toast.makeText(this, "Semua kolom harus diisi!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Validate event data
            if (eventId == null || eventName == null || eventPrice <= 0) {
                Toast.makeText(this, "Data event tidak valid!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Check if the user is authenticated
            val userId = firebaseAuth.currentUser?.uid
            if (userId == null) {
                Toast.makeText(this, "Pengguna tidak terautentikasi!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Generate order ID
            val orderId = "ORDER-${System.currentTimeMillis()}"

            // Create Participant object
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
                eventId = eventId,
                userId = userId,
                eventPrice = eventPrice
            )

            // Save participant data to Firebase
            val participantsRef = database.getReference("Peserta").push()
            participantsRef.setValue(participant).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Pendaftaran berhasil!", Toast.LENGTH_SHORT).show()
                    // Proceed with the payment process
                    fetchSnapToken(orderId, eventPrice)
                } else {
                    Toast.makeText(this, "Terjadi kesalahan, coba lagi!", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun fetchSnapToken(orderId: String, grossAmount: Int) {
        NetworkUtils.generateSnapToken(orderId, grossAmount) { snapToken ->
            runOnUiThread {
                if (snapToken != null) {
                    Log.d("Midtrans", "Snap Token received: $snapToken")
                    initiatePayment(snapToken)
                    startPaymentWithSnapToken(this, snapToken, paymentLauncher)
                } else {
                    Toast.makeText(this, "Failed to retrieve Snap Token", Toast.LENGTH_LONG).show()
                }
            }
        }
    }



    // Function to initiate the Midtrans payment using the Snap Token
    private fun initiatePayment(snapToken: String) {
        // Build the Midtrans SDK with necessary configurations
        UiKitApi.Builder()
            .withContext(this)
            .withMerchantClientKey(clientKey)
            .withColorTheme(CustomColorTheme("#FFE51255", "#B61548", "#FFE51255"))
            .withMerchantUrl(baseUrl)  // URL of your merchant (your server)
            .enableLog(true)
            .build()
    }

    // Function to start payment with the Snap Token
    private fun startPaymentWithSnapToken(activity: Activity, snapToken: String, launcher: ActivityResultLauncher<Intent>) {
        UiKitApi.getDefaultInstance().startPaymentUiFlow(activity, launcher, snapToken)
        finish()

    }
}
