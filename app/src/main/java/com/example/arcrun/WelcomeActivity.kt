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
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class WelcomeActivity : AppCompatActivity() {

    private lateinit var userHandler: GetUser
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_welcome)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.welcomeActivity)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()


        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationContainer)
        val bottomNavigation = BottomNavigation(this)
        bottomNavigation.setupBottomNavigation(bottomNavigationView)

        userHandler = GetUser ()

        val userNameTextView = findViewById<TextView>(R.id.textViewAyman)
        val userProfileImage = findViewById<ImageView>(R.id.profileButton)

        userHandler.getCurrentUser  { user ->
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

        val eventPage = findViewById<MaterialCardView>(R.id.eventOnGoingCard)

        eventPage.setOnClickListener {
            val navToEventPage = Intent(this, BuyTicketActivity::class.java)
            startActivity(navToEventPage)
        }

        val achievement = findViewById<MaterialCardView>(R.id.achievementCard)
        achievement.setOnClickListener {
            val toAchievement = Intent(this, AchievementProgram::class.java)
            startActivity(toAchievement)
        }

        val programCard = findViewById<MaterialCardView>(R.id.programCard)

        programCard.setOnClickListener {
            val navToDisplayProgram = Intent(this, DisplayProgram::class.java)
            startActivity(navToDisplayProgram)
        }

        val eduInfoCard = findViewById<MaterialCardView>(R.id.educationCard)
        eduInfoCard.setOnClickListener {
            val navToEduInfo = Intent(this, EduInformation::class.java)
            startActivity(navToEduInfo)
        }

        loadAchievementData()
    }


    private fun loadAchievementData() {
        val currentUserId = auth.currentUser?.uid ?: return
        val achievementRef = database.getReference("Achievement").child(currentUserId)

        achievementRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val rank = snapshot.child("rank").value?.toString() ?: "Bronze"
                val totalMedali = snapshot.child("total_medali").value?.toString()?.toInt() ?: 0

                // Hitung batas progres rank berikutnya
                val maxProgress = when (rank) {
                    "Bronze" -> 30
                    "Silver" -> 60
                    else -> 60 // Gold
                }

                val currentProgress = when (rank) {
                    "Bronze" -> totalMedali
                    "Silver" -> totalMedali - 30
                    else -> 30 // Gold
                }

                // Update UI
                val rankTextView = findViewById<TextView>(R.id.namaAchievement)
                val progressBar = findViewById<ProgressBar>(R.id.progressBar)
                val progressTextView = findViewById<TextView>(R.id.descAchievement)
                val medalImageView = findViewById<ImageView>(R.id.imageAchievement)

                rankTextView.text = "$rank"
                progressBar.max = maxProgress
                progressBar.progress = currentProgress
                progressTextView.text = "Dapatkan medali hingga $maxProgress untuk meraih rank selanjutnya"

                when (rank) {
                    "Bronze" -> medalImageView.setImageResource(R.drawable.bronze)
                    "Silver" -> medalImageView.setImageResource(R.drawable.silver)
                    "Gold" -> medalImageView.setImageResource(R.drawable.gold)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("WelcomeActivity", "Gagal membaca data: ${error.message}")
                Toast.makeText(this@WelcomeActivity, "Gagal memuat data", Toast.LENGTH_SHORT).show()
            }
        })
    }
}