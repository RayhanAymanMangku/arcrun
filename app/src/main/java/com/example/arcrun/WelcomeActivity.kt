package com.example.arcrun

import GetUser
import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.card.MaterialCardView

class WelcomeActivity : AppCompatActivity() {

    private lateinit var userHandler: GetUser

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_welcome)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.welcomeActivity)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationContainer)
        val bottomNavigation = BottomNavigation(this)
        bottomNavigation.setupBottomNavigation(bottomNavigationView)


        val ticketPage = findViewById<LinearLayout>(R.id.nav2)
        ticketPage.setOnClickListener {
            val intent = Intent(this, DisplayProgram::class.java)
            startActivity(intent)
        }

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


    }
}