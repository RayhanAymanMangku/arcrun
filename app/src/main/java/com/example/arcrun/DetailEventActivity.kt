package com.example.arcrun

import GetUser
import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions

class DetailEventActivity : AppCompatActivity() {
    private lateinit var userHandler: GetUser

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail_event)

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

        val eventName = findViewById<TextView>(R.id.eventNameDetail)
        val eventDesc= findViewById<TextView>(R.id.deskripsiTxt)
        val eventImage = findViewById<ImageView>(R.id.imgEvent)

        val name = intent.getStringExtra("event_name")
        val desc = intent.getStringExtra("event_desc")
        val image = intent.getStringExtra("event_image")

        eventDesc.text = desc
        eventName.text = name

        if (eventImage != null) {
            Glide.with(this)
                .load(image)
                .apply(RequestOptions().error(R.drawable.imagenotfound))
                .into(eventImage)
        }  }
}