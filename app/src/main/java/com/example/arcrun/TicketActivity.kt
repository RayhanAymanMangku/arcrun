package com.example.arcrun

import GetUser
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.arcrun.adapter.TicketAdapter
import com.example.arcrun.databinding.ActivityTicketBinding
import com.example.arcrun.models.EventTicketModels
import com.example.arcrun.models.Participant
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TicketActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTicketBinding
    private val adapter = TicketAdapter()
    private lateinit var userHandler: GetUser
    private lateinit var database: DatabaseReference
    private lateinit var ticketEventDatabase: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTicketBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Inisialisasi
        userHandler = GetUser()
        database = FirebaseDatabase.getInstance().getReference("Peserta")
        ticketEventDatabase = FirebaseDatabase.getInstance().getReference("TiketEvents")

        // Set up RecyclerView
        binding.viewTiket.adapter = adapter
        binding.viewTiket.layoutManager = LinearLayoutManager(this)

        // Ambil data tiket berdasarkan user login
        fetchTicketsForCurrentUser()

        userHandler.getCurrentUser { user -> binding.textViewAyman.text = user.name
            if (user.profileImageUrl != null) {
                Glide.with(this)
                    .load(user.profileImageUrl)
                    .apply(RequestOptions.circleCropTransform())
                    .into(binding.profileButton)
            } else {
                binding.profileButton.setImageResource(R.drawable.default_profile_image)
            }

            binding.profileButton.setOnClickListener {
                val navigateToProfile = Intent(this, UserProfile::class.java)
                startActivity(navigateToProfile)
            }
        }
    }

    private fun fetchTicketsForCurrentUser() {
        val currentDate = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(Date())
        Log.d("TicketActivity", "Current Date: $currentDate") // Debug: Tanggal saat ini

        userHandler.getCurrentUser { currentUser ->
            Log.d("TicketActivity", "Current User ID: ${currentUser.id}")
            database.orderByChild("userId").equalTo(currentUser.id)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            Log.d("TicketActivity", "Data peserta ditemukan: ${snapshot.childrenCount} item")
                            val participants = mutableListOf<Participant>()
                            snapshot.children.forEach { data ->
                                val participant = data.getValue(Participant::class.java)
                                participant?.let {
                                    ticketEventDatabase.child(it.eventId).get()
                                        .addOnSuccessListener { eventSnapshot ->
                                            val eventTicket =
                                                eventSnapshot.getValue(EventTicketModels::class.java)
                                            if (eventTicket != null) {
                                                Log.d("TicketActivity", "Event ID: ${it.eventId}, Event Name: ${eventTicket.nama_event}, Waktu Mulai: ${eventTicket.waktu_mulai}") // Debug: Data event

                                                // Filter berdasarkan waktu_mulai
                                                if (eventTicket.waktu_mulai != null && eventTicket.waktu_mulai!! > currentDate) {
                                                    Log.d("TicketActivity", "Event memenuhi syarat: ${eventTicket.nama_event}")
                                                    it.eventName = eventTicket.nama_event
                                                    it.waktu_mulai = eventTicket.waktu_mulai
                                                    it.locationName = eventTicket.lokasi?.namaLokasi
                                                    participants.add(it)
                                                } else {
                                                    Log.d("TicketActivity", "Event tidak memenuhi syarat (waktu_mulai <= currentDate): ${eventTicket.nama_event}")
                                                }
                                                adapter.submitList(participants.toList())
                                            }
                                        }.addOnFailureListener { e ->
                                            Log.e("TicketActivity", "Gagal memuat data event: ${e.message}")
                                        }
                                }
                            }
                        } else {
                            Log.d("TicketActivity", "Tidak ada data peserta untuk user ini.")
                            Toast.makeText(this@TicketActivity, "Tidak ada tiket yang ditemukan.", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.e("TicketActivity", "Error: ${error.message}")
                        Toast.makeText(this@TicketActivity, "Gagal memuat data.", Toast.LENGTH_SHORT).show()
                    }
                })
        }
    }
}
