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
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.database.*

class DetailEventActivity : AppCompatActivity() {
    private lateinit var userHandler: GetUser
    private lateinit var database: DatabaseReference // New

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail_event)
        enableEdgeToEdge()

        // New
        val mapFragment = SupportMapFragment.newInstance()
        supportFragmentManager.beginTransaction()
            .replace(R.id.mapFragmentContainer, mapFragment)
            .commit()

        userHandler = GetUser()

        setupUserProfile()

        val eventId = intent.getStringExtra("event_id")
        val eventName = intent.getStringExtra("event_name")
        val eventDesc = intent.getStringExtra("event_desc")
        val eventImage = intent.getStringExtra("event_image")
        val eventPrice = intent.getIntExtra("event_price", 0)

        // Update
        if (eventId != null) {
            fetchEventLocation(eventId, mapFragment)
        }

        // Update
        if (eventName != null && eventDesc != null) {
            displayEventDetails(eventName, eventDesc, eventImage, eventPrice)
        } else {
            Log.e("DetailEventActivity", "Event data not found!")
        }

        findViewById<AppCompatButton>(R.id.detailBuyButton).setOnClickListener {
            if (eventId != null && eventName != null) {
                navigateToDaftarPeserta(eventId, eventName, eventPrice)
            }
        }
    }

    private fun setupUserProfile() {
        val userNameTextView = findViewById<TextView>(R.id.textViewAyman)
        val userProfileImage = findViewById<ImageView>(R.id.profileButton)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.detailEvent)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

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

    // New
    private fun fetchEventLocation(eventId: String, mapFragment: SupportMapFragment) {
        database = FirebaseDatabase.getInstance().getReference("TiketEvents")

        // Ambil lokasi berdasarkan eventId
        database.child(eventId).child("lokasi").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val latitude = snapshot.child("latitude").getValue(Double::class.java) ?: 0.0
                val longitude = snapshot.child("longitude").getValue(Double::class.java) ?: 0.0
                val locationName = snapshot.child("namaLokasi").getValue(String::class.java) ?: "Unknown Location"

                Log.d("DetailEventActivity", "Latitude: $latitude, Longitude: $longitude, Location Name: $locationName")

                mapFragment.getMapAsync { googleMap ->
                    setupMap(googleMap, latitude, longitude, locationName)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("DetailEventActivity", "Failed to fetch location: ${error.message}")
            }
        })
    }

    // New
    private fun setupMap(googleMap: GoogleMap, latitude: Double, longitude: Double, locationName: String) {
        if (latitude != 0.0 && longitude != 0.0) {
            val eventLocation = LatLng(latitude, longitude)
            googleMap.addMarker(
                MarkerOptions()
                    .position(eventLocation)
                    .title(locationName)
            )
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(eventLocation, 15f))
        } else {
            Log.e("DetailEventActivity", "Invalid location data")
        }
    }

    private fun displayEventDetails(eventName: String, eventDesc: String, eventImage: String?, eventPrice: Int) {
        val eventNameText = findViewById<TextView>(R.id.eventNameDetail)
        val eventDescText = findViewById<TextView>(R.id.deskripsiTxt)
        val eventImageView = findViewById<ImageView>(R.id.imgEvent)
        val eventPriceText = findViewById<TextView>(R.id.hargaEventDetail)

        eventNameText.text = eventName
        eventDescText.text = eventDesc
        eventPriceText.text = "Rp. $eventPrice"

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