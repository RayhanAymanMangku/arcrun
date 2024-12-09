package com.example.arcrun

import GetUser
import Participant
import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.arcrun.databinding.ActivityDaftarPesertaBinding
import com.example.arcrun.payments.PaymentMidtrans
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.util.*

class DaftarPesertaActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDaftarPesertaBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var userHandler: GetUser

    // Tambahkan konstanta REQUEST_PAYMENT
    private val REQUEST_PAYMENT = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDaftarPesertaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        userHandler = GetUser()

        val kategoriList = listOf("Lari 5K", "Lari 10K", "Maraton")
        val adapterKategori = ArrayAdapter(this, android.R.layout.simple_spinner_item, kategoriList)
        adapterKategori.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.kategoriInput.adapter = adapterKategori

        val genderList = listOf("Laki-Laki", "Perempuan")
        val adapterGender = ArrayAdapter(this, android.R.layout.simple_spinner_item, genderList)
        adapterGender.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.jenisKelaminInput.adapter = adapterGender

        val sizeJerseyList = listOf("S", "M", "L", "XL")
        val adapterSizeJersey = ArrayAdapter(this, android.R.layout.simple_spinner_item, sizeJerseyList)
        adapterSizeJersey.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.sizeJerseyInput.adapter = adapterSizeJersey

        setupUI()
        setupUserProfile()
    }

    private fun setupUI() {
        binding.tanggalLahirInput.setOnClickListener {
            showDatePicker()
        }

        binding.submitBtn.setOnClickListener {
            submitForm()
        }
    }

    private fun setupUserProfile() {
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

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            this,
            { _, selectedYear, selectedMonth, selectedDay ->
                val date = "${selectedDay}/${selectedMonth + 1}/${selectedYear}"
                binding.tanggalLahirInput.setText(date)
            },
            year,
            month,
            day
        )
        datePickerDialog.show()
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

        if (name.isEmpty() || ttl.isEmpty() || email.isEmpty() || phone == null || emergencyContact == null || namaBib.isEmpty()) {
            Toast.makeText(this, "Semua kolom harus diisi!", Toast.LENGTH_SHORT).show()
            return
        }

        val orderId = "ORDER-${System.currentTimeMillis()}"
        val userId = firebaseAuth.currentUser?.uid

        if (userId != null) {
            val participantData = Participant(
                name = name,
                gender = gender,
                ttl = ttl,
                email = email,
                phone = phone,
                emergencyContact = emergencyContact,
                category = category,
                jerseySize = jerseySize,
                namaBib = namaBib,
                orderId = orderId
            )

            // Simpan peserta ke Firebase Database
            database.reference.child("Peserta").child(userId).push().setValue(participantData)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, "Pendaftaran berhasil!", Toast.LENGTH_SHORT).show()

                        // Generate Snap Token
                        val snapToken = UUID.randomUUID().toString()

                        // Simpan Snap Token ke Firebase
                        PaymentMidtrans().saveSnapTokenToFirebase(orderId, snapToken, intent.getIntExtra("event_price", 0), intent.getStringExtra("event_name") ?: "Event")

                        // Pass data dan Snap Token ke PaymentMidtrans
                        val pembayaranIntent = Intent(this, PaymentMidtrans::class.java)
                        pembayaranIntent.putExtra("event_id", intent.getStringExtra("event_id"))
                        pembayaranIntent.putExtra("event_name", intent.getStringExtra("event_name"))
                        pembayaranIntent.putExtra("event_price", intent.getIntExtra("event_price", 0))
                        pembayaranIntent.putExtra("snap_token", snapToken)

                        // Mulai pembayaran Midtrans dan menunggu hasilnya
                        startActivity(pembayaranIntent)
                    } else {
                        Toast.makeText(this, "Terjadi kesalahan, coba lagi!", Toast.LENGTH_SHORT).show()
                    }
                }
        } else {
            Toast.makeText(this, "Pengguna tidak terautentikasi!", Toast.LENGTH_SHORT).show()
        }
    }

    // Mengganti onActivityResult dengan mengatur hasil pembayaran
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_PAYMENT) {
            // Handle payment result (success or failure)
            if (resultCode == RESULT_OK) {
                // Pembayaran sukses, lanjutkan ke halaman berikutnya atau tampilkan konfirmasi
                Toast.makeText(this, "Pembayaran sukses!", Toast.LENGTH_SHORT).show()
                finish() // Tutup aktivitas hanya setelah pembayaran selesai
            } else {
                // Pembayaran gagal, tampilkan pesan error
                Toast.makeText(this, "Pembayaran gagal!", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
