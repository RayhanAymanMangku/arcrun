package com.example.arcrun

import GetUser
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.arcrun.adapter.BuatProgram
import com.example.arcrun.databinding.ActivityProgramBinding
import com.example.arcrun.models.ProgramModels
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class DisplayProgram : AppCompatActivity() {
    private lateinit var binding: ActivityProgramBinding
    private lateinit var database: FirebaseDatabase
    private lateinit var auth: FirebaseAuth
    private lateinit var userHandler: GetUser
    private lateinit var adapter: BuatProgram
    val items = ArrayList<ProgramModels>() // Deklarasi items di luar metode
    val programid = ArrayList<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProgramBinding.inflate(layoutInflater)
        setContentView(binding.root)

        database = FirebaseDatabase.getInstance()
        auth = FirebaseAuth.getInstance()

        // Menambahkan event listener untuk tombol add
        binding.addBtn.setOnClickListener {
            val intent = Intent(this@DisplayProgram, InputProgramActivity::class.java)
            startActivity(intent)
        }

        // Memuat program berdasarkan UID pengguna yang sedang login
        initViewProgram()

        userHandler = GetUser()

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationContainer)
        val bottomNavigation = BottomNavigation(this)
        bottomNavigation.setupBottomNavigation(bottomNavigationView)


        val userNameTextView = findViewById<TextView>(R.id.textView3)
        val userProfileImage = findViewById<ImageView>(R.id.profile)

        userHandler.getCurrentUser { user ->
            userNameTextView?.let {
                it.text = user.name
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
            } ?: Log.e("DisplayProgram", "TextView is null")
        }
    }

    private fun initViewProgram() {
        val currentUserUid = auth.currentUser?.uid ?: return // Mendapatkan UID pengguna yang sedang login
        val myRef = database.getReference("Program").child(currentUserUid) // Menyaring data berdasarkan UID pengguna

        // Inisialisasi adapter hanya sekali di sini
        adapter = BuatProgram(items, programid, this@DisplayProgram)
        binding.recyclerView.layoutManager = LinearLayoutManager(this@DisplayProgram)
        binding.recyclerView.adapter = adapter

        myRef.addListenerForSingleValueEvent(object : ValueEventListener {
            @SuppressLint("NotifyDataSetChanged")
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    for (issue in snapshot.children) {
                        val program = issue.getValue(ProgramModels::class.java)
                        if (program != null) {
                            items.add(program)
                            programid.add(issue.key.toString())
                            Log.d("Add berhasil", "Add program berhasil")
                        }
                    }
                    // Panggil notifyDataSetChanged setelah menambahkan data
                    adapter.notifyDataSetChanged()

                    // Menampilkan data dalam RecyclerView
                    if (items.isNotEmpty()) {
                        binding.recyclerView.layoutManager = LinearLayoutManager(this@DisplayProgram, LinearLayoutManager.VERTICAL, false)
                    } else {
                        Toast.makeText(this@DisplayProgram, "No programs found.", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this@DisplayProgram, "No data in Firebase.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@DisplayProgram, "Failed to load data: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}