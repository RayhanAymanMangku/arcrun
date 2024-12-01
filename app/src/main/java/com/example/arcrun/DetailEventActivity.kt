package com.example.arcrun

import GetUser
import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import org.w3c.dom.Text

class DetailEventActivity : AppCompatActivity() {
    private lateinit var userHandler: GetUser

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail_event)
        enableEdgeToEdge()

        userHandler = GetUser ()

        val userNameTextView = findViewById<TextView>(R.id.textViewAyman)
        val userProfileImage = findViewById<ImageView>(R.id.profileButton)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.detailEvent)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

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

        val btnBuy = findViewById<AppCompatButton>(R.id.detailBuyButton)
        btnBuy.setOnClickListener {
            val navToDaftarPeserta = Intent(this, DaftarPesertaActivity::class.java)
            startActivity(navToDaftarPeserta)
        }

        val eventName = findViewById<TextView>(R.id.eventNameDetail)
        val eventDesc= findViewById<TextView>(R.id.deskripsiTxt)
        val eventImage = findViewById<ImageView>(R.id.imgEvent)
        val eventHarga = findViewById<TextView>(R.id.hargaEventDetail)

        val name = intent.getStringExtra("event_name")
        val desc = intent.getStringExtra("event_desc")
        val image = intent.getStringExtra("event_image")
        val harga = intent.getIntExtra("event_price", 0)


        eventDesc.text = desc
        eventName.text = name
        eventHarga.text = "Rp. ${harga}"

        if (eventImage != null) {
            Glide.with(this)
                .load(image)
                .apply(RequestOptions().error(R.drawable.imagenotfound))
                .into(eventImage)
        }  }
}