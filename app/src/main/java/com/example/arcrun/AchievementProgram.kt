package com.example.arcrun

import GetUser
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.card.MaterialCardView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class AchievementProgram : AppCompatActivity() {

    private lateinit var userHandler: GetUser
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_achievement_program)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Inisialisasi Firebase
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

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

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationContainer)
        val bottomNavigation = BottomNavigation(this)
        bottomNavigation.setupBottomNavigation(bottomNavigationView)


        // Update achievement saat layar dibuka
        updateAchievementAndProgressBar()
    }

    /**
     * Menghitung total checklist "checked" dan memperbarui ke Firebase
     */
    private fun updateAchievementAndProgressBar() {
        val currentUserId = auth.currentUser?.uid ?: return
        val programRef = database.getReference("Program").child(currentUserId)
        val achievementRef = database.getReference("Achievement").child(currentUserId)

        programRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var totalMedali = 0

                // Hitung jumlah total medali
                for (programSnapshot in snapshot.children) {
                    val trackProgresSnapshot = programSnapshot.child("track_progres")
                    for (child in trackProgresSnapshot.children) {
                        val gambarChecklist = child.child("gambar_checklist").value.toString()
                        if (gambarChecklist == "checked") {
                            totalMedali++
                        }
                    }
                }

                // Tentukan rank berdasarkan totalMedali
                val rank = when {
                    totalMedali >= 60 -> "Gold"
                    totalMedali >= 30 -> "Silver"
                    else -> "Bronze"
                }



                // Hitung batas progres rank berikutnya
                val maxProgress = when (rank) {
                    "Bronze" -> 30
                    "Silver" -> 60
                    else -> 60 // Untuk Gold, progres tidak melebihi
                }

                val currentProgress = when (rank) {
                    "Bronze" -> totalMedali
                    "Silver" -> totalMedali - 30
                    else -> 30 // Gold mencapai maksimal
                }

                // Simpan ke Firebase
                achievementRef.child("total_medali").setValue(totalMedali)
                achievementRef.child("rank").setValue(rank)
                    .addOnSuccessListener {
                        Log.d("AchievementUpdate", "Achievement diperbarui: $totalMedali, Rank: $rank")
                        Toast.makeText(this@AchievementProgram, "Rank Anda: $rank", Toast.LENGTH_SHORT).show()

                        // Update rank dan progress di UI
                        val rankTextView = findViewById<TextView>(R.id.namaAchievement)
                        val progressBar = findViewById<ProgressBar>(R.id.progressBar)
                        val progressTextView = findViewById<TextView>(R.id.descAchievement)
                        val totalhari = findViewById<TextView>(R.id.totalHari)
                        val medalImageView = findViewById<ImageView>(R.id.imageAchievement)

                        rankTextView.text = "$rank"
                        progressBar.max = maxProgress
                        progressBar.progress = currentProgress
                        progressTextView.text = "Dapatkan medali hingga $maxProgress untuk meraih rank selanjutnya"
                        totalhari.text = totalMedali.toString()

                        when (rank) {
                            "Bronze" -> medalImageView.setImageResource(R.drawable.bronze)
                            "Silver" -> medalImageView.setImageResource(R.drawable.silver)
                            "Gold" -> medalImageView.setImageResource(R.drawable.gold)
                        }
                    }
                    .addOnFailureListener { e ->
                        Log.e("AchievementUpdate", "Gagal memperbarui achievement: ${e.message}")
                    }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("AchievementUpdate", "Gagal membaca data: ${error.message}")
            }
        })
    }

}