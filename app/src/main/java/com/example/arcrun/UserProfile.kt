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
import com.google.firebase.auth.FirebaseAuth

class UserProfile : AppCompatActivity() {

    private lateinit var userHandler : GetUser

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_user_profile)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.userProfileActivity)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        userHandler = GetUser()

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationContainer)
        val bottomNavigation = BottomNavigation(this)
        bottomNavigation.setupBottomNavigation(bottomNavigationView)


        val userNameTextView = findViewById<TextView>(R.id.textViewUserProfile)
        val userProfileImage = findViewById<ImageView>(R.id.userProfileButton)
        val signOutButton = findViewById<LinearLayout>(R.id.signOutButton)
        val userEmail = findViewById<TextView>(R.id.emailUserText)

        userHandler.getCurrentUser  { user ->
            userNameTextView.text = user.name
            userEmail.text = user.email
            if (user.profileImageUrl != null) {
                Glide.with(this)
                    .load(user.profileImageUrl)
                    .apply(RequestOptions.circleCropTransform())
                    .into(userProfileImage)
            } else {
                userProfileImage.setImageResource(R.drawable.default_profile_image)
            }

        }

        userProfileImage.setOnClickListener {
            val navigateToProfile = Intent(this, UserProfile::class.java)
            startActivity(navigateToProfile)
        }

        val userNotification = findViewById<ImageView>(R.id.notificationBtn)

        userNotification.setOnClickListener {
            val navigateToNotif = Intent(this, NotificationActivity::class.java)
            startActivity(navigateToNotif)
        }

        signOutButton.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK // Clear the activity stack
            startActivity(intent)
            finish()
        }
    }
}