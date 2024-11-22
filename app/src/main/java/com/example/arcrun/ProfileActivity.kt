package com.example.arcrun

import GetUser
import android.annotation.SuppressLint
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
import com.google.firebase.auth.FirebaseAuth

class ProfileActivity : AppCompatActivity() {

    private lateinit var userHandler: GetUser

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_profile)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val userNameTextView = findViewById<TextView>(R.id.textViewUserProfile)
        val userProfileImage = findViewById<ImageView>(R.id.profileButton)

        userHandler = GetUser()

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
        }


        val signOutButton = findViewById<LinearLayout>(R.id.signOutButton)
        signOutButton.setOnClickListener {
            FirebaseAuth.getInstance().signOut() // Sign out from Firebase
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK // Clear the activity stack
            startActivity(intent)
            finish()
        }
    }
}