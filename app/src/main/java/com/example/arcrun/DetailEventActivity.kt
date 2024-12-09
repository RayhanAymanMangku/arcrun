package com.example.arcrun

import GetUser
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.arcrun.models.EventTicketModels

class DetailEventActivity : AppCompatActivity() {

    private lateinit var userHandler: GetUser

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail_event)
        enableEdgeToEdge()

        // Inisialisasi GetUser
        userHandler = GetUser()

        // Setup user profile
        setupUserProfile()

        // Mendapatkan data event dari Intent
        val eventId = intent.getStringExtra("event_id")
        val eventName = intent.getStringExtra("event_name")
        val eventDesc = intent.getStringExtra("event_desc")
        val eventImage = intent.getStringExtra("event_image")
        val eventPrice = intent.getIntExtra("event_price", 0)

        if (eventId != null && eventName != null && eventDesc != null) {
            Log.d("DetailEventActivity", "Received event: Name=$eventName, ID=$eventId")
            // Menampilkan data ke UI
            displayEventDetails(eventName, eventDesc, eventImage, eventPrice)
        } else {
            Log.e("DetailEventActivity", "Event data not found!")
            // You can show an error or default UI here
        }

        // Tombol untuk daftar peserta
        findViewById<AppCompatButton>(R.id.detailBuyButton).setOnClickListener {
            if (eventId != null && eventName != null) {
                navigateToDaftarPeserta(eventId, eventName, eventPrice)
            }
        }
    }

    private fun setupUserProfile() {
        val userNameTextView = findViewById<TextView>(R.id.textViewAyman)
        val userProfileImage = findViewById<ImageView>(R.id.profileButton)

        // Menyesuaikan padding untuk edge-to-edge
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.detailEvent)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Mendapatkan data pengguna
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

            // Klik pada profil
            userProfileImage.setOnClickListener {
                val navigateToProfile = Intent(this, UserProfile::class.java)
                startActivity(navigateToProfile)
            }
        }
    }

    private fun displayEventDetails(eventName: String, eventDesc: String, eventImage: String?, eventPrice: Int) {
        // Mendapatkan referensi ke View
        val eventNameText = findViewById<TextView>(R.id.eventNameDetail)
        val eventDescText = findViewById<TextView>(R.id.deskripsiTxt)
        val eventImageView = findViewById<ImageView>(R.id.imgEvent)
        val eventPriceText = findViewById<TextView>(R.id.hargaEventDetail)

        // Menampilkan data ke UI
        eventNameText.text = eventName
        eventDescText.text = eventDesc
        eventPriceText.text = "Rp. $eventPrice"

        // Memuat gambar menggunakan Glide
        if (eventImage != null) {
            Glide.with(this)
                .load(eventImage)
                .apply(RequestOptions().error(R.drawable.imagenotfound))
                .into(eventImageView)
        } else {
            eventImageView.setImageResource(R.drawable.imagenotfound)
        }
    }

    private fun navigateToDaftarPeserta(eventId: String, eventName: String, eventPrice: Int) {
        val intent = Intent(this, DaftarPesertaActivity::class.java)
        intent.putExtra("event_id", eventId)
        intent.putExtra("event_name", eventName)
        intent.putExtra("event_price", eventPrice)
        startActivity(intent)
    }
}