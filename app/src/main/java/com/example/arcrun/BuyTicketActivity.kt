package com.example.arcrun

import GetUser
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.arcrun.models.EventTicketModels
import com.example.arcrun.adapter.EventTickets
import com.example.arcrun.databinding.ActivityBuyTicketBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.database.*

class BuyTicketActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBuyTicketBinding
    private lateinit var database: FirebaseDatabase
    private lateinit var userHandler: GetUser

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityBuyTicketBinding.inflate(layoutInflater)
        setContentView(binding.root)

        database = FirebaseDatabase.getInstance()
        initViewEvent()

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

    private fun initViewEvent() {
        val myRef: DatabaseReference = database.getReference("TiketEvents")
        val items = ArrayList<EventTicketModels>()
        val eventIds = ArrayList<String>()

        myRef.addListenerForSingleValueEvent(object : ValueEventListener {
            @SuppressLint("NotifyDataSetChanged")
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    for (issue in snapshot.children) {
                        val event = issue.getValue(EventTicketModels::class.java)
                        val eventId = issue.key // Dapatkan key dari node untuk ID
                        if (event != null && eventId != null) {
                            items.add(event)
                            eventIds.add(eventId) // Simpan eventId
                            Log.d("FirebaseData", "Loaded event: ${event.nama_event}, ID: $eventId")
                        }
                    }

                    // Pastikan RecyclerView menampilkan data jika ada
                    if (items.isNotEmpty()) {
                        binding.displayAllEvents.layoutManager = LinearLayoutManager(
                            this@BuyTicketActivity, LinearLayoutManager.VERTICAL, false
                        )

                        // Set adapter without listener
                        binding.displayAllEvents.adapter = EventTickets(items, this@BuyTicketActivity, eventIds)
                        (binding.displayAllEvents.adapter as EventTickets).notifyDataSetChanged()
                    } else {
                        Toast.makeText(this@BuyTicketActivity, "No events found.", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this@BuyTicketActivity, "No data in Firebase.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@BuyTicketActivity, "Failed to load data: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun navigateToDetailEvent(eventIds: String, eventName: String, eventDesc: String, eventImage: String?, eventPrice: Int) {
        val intent = Intent(this, DetailEventActivity::class.java)
        intent.putExtra("event_id", eventIds)
        intent.putExtra("event_name", eventName)
        intent.putExtra("event_desc", eventDesc)
        intent.putExtra("event_image", eventImage)
        intent.putExtra("event_price", eventPrice)
        startActivity(intent)
    }
}