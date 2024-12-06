package com.example.arcrun

import GetUser
import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.card.MaterialCardView

class EduInformation : AppCompatActivity() {

    private lateinit var userHandler: GetUser


    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_edu_information)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.eduInformationActivity)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationContainer)
        val bottomNavigation = BottomNavigation(this)
        bottomNavigation.setupBottomNavigation(bottomNavigationView)


        val educationCard = findViewById<MaterialCardView>(R.id.educationCard)
        val educationCard2 = findViewById<MaterialCardView>(R.id.educationCard2)
        val educationCard3 = findViewById<MaterialCardView>(R.id.educationCard3)

        educationCard.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://medium.com/@shamimakhtar12220/morning-running-health-821d480c8e99"))
            startActivity(intent)
        }

        educationCard2.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://medium.com/@MindMyFitness/running-health-experiments-111512a4dc60"))
            startActivity(intent)
        }

        educationCard3.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://medium.com/illumination/running-is-destroying-your-heart-if-you-do-this-9445eaee568d"))
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

    }
}