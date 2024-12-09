package com.example.arcrun

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.LinearLayout
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class UserStartActivity : AppCompatActivity() {
    private lateinit var firebaseAuth: FirebaseAuth

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_user_start)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.userStartActivity)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        firebaseAuth = FirebaseAuth.getInstance()

        val lanjutBtn = findViewById<LinearLayout>(R.id.lanjutBtn)
        val userNameInput = findViewById<EditText>(R.id.namaUserInput)

        lanjutBtn.setOnClickListener {
            val userName = userNameInput.text.toString().trim()
            if (userName.isNotEmpty()) {
                val user = firebaseAuth.currentUser
                user?.let {
                    val userId = it.uid
                    val database = FirebaseDatabase.getInstance()
                    val usersRef = database.getReference("users")

                    // update name in users collection
                    usersRef.child(userId).child("name").setValue(userName)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                Log.d("UserStartActivity", "User name updated in database")
                                val intent = Intent(this, WelcomeActivity::class.java)
                                startActivity(intent)
                                finish()
                            } else {
                                Log.w("UserStartActivity", "Failed to update user name in database", task.exception)
                            }
                        }
                }
            } else {
                userNameInput.error = "Nama pengguna tidak boleh kosong"
            }
        }
    }
}