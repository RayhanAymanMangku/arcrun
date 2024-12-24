package com.example.arcrun

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.LinearLayout
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.FirebaseApp
import com.google.firebase.database.FirebaseDatabase

class MainActivity : AppCompatActivity() {

    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize Firebase
        FirebaseApp.initializeApp(this)
        firebaseAuth = FirebaseAuth.getInstance()

        val currentUser = firebaseAuth.currentUser
        if (currentUser != null) {
            checkUserName(currentUser.uid)
            return
        }
        setContentView(R.layout.activity_main)

        // Configure Google Sign-In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        val googleSignInLayout: LinearLayout = findViewById(R.id.googleSignInLayout)
        googleSignInLayout.setOnClickListener {
            signInWithGoogle()
        }


    }

    private fun signInWithGoogle() {
        val signInIntent = googleSignInClient.signInIntent
        googleSignInLauncher.launch(signInIntent)
    }

    private val googleSignInLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                firebaseAuthWithGoogle(account)
            } catch (e: ApiException) {
                Log.d("MainActivity", "Google sign-in failed: ${e.message}")
            }
        }

    private fun firebaseAuthWithGoogle(account: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        firebaseAuth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.d("MainActivity", "signInWithCredential:success")
                    val user = firebaseAuth.currentUser
                    user?.let {
                        val userId = it.uid
                        val email = it.email
                        val name = "User"

                        val userMap = mapOf(
                            "email" to email,
                            "name" to name
                        )

                        val database = FirebaseDatabase.getInstance()
                        val usersRef = database.getReference("users")

                        usersRef.child(userId).setValue(userMap)
                            .addOnCompleteListener { dbTask ->
                                if (dbTask.isSuccessful) {
                                    Log.d("MainActivity", "User data added to database")
                                    checkUserName(userId)
                                } else {
                                    Log.w("MainActivity", "Failed to add user data to database", dbTask.exception)
                                }
                            }
                    }
                } else {
                    Log.w("MainActivity", "signInWithCredential:failure", task.exception)
                }
            }
    }

    private fun checkUserName(userId: String) {
        val database = FirebaseDatabase.getInstance()
        val usersRef = database.getReference("users").child(userId)

        usersRef.child("name").get().addOnSuccessListener { dataSnapshot ->
            val name = dataSnapshot.getValue(String::class.java)
            if (name != null && name != "User") {
                navigateToWelcomeActivity()
            } else {
                navigateToUserStartActivity()
            }
        }.addOnFailureListener {
            navigateToUserStartActivity()
        }
    }

    private fun navigateToUserStartActivity() {
        val intent = Intent(this, UserStartActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun navigateToWelcomeActivity() {
        val intent = Intent(this, WelcomeActivity::class.java)
        startActivity(intent)
        finish()
    }
}