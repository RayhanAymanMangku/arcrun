package com.example.arcrun

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
import com.example.arcrun.adapter.AdapterDetailProgram
import com.example.arcrun.databinding.ActivityDetailProgramBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.example.arcrun.models.DetailProgramModel
import com.example.arcrun.models.ProgramModels
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import GetUser


class DetailProgram : AppCompatActivity() {
    private lateinit var database: FirebaseDatabase
    private lateinit var auth: FirebaseAuth
    private lateinit var userHandler: GetUser
    private lateinit var adapter: AdapterDetailProgram
    private lateinit var binding: ActivityDetailProgramBinding
    private val items = ArrayList<DetailProgramModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailProgramBinding.inflate(layoutInflater)
        setContentView(binding.root)

        database = FirebaseDatabase.getInstance()
        auth = FirebaseAuth.getInstance()

        adapter = AdapterDetailProgram(listProgram = items , context = this , trackProgresRef = database.getReference("Program"))
        binding.recyclerViewDetailProgram.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewDetailProgram.adapter = adapter

        userHandler = GetUser()

        val userNameTextView = findViewById<TextView>(R.id.textView3)
        val userProfileImage = findViewById<ImageView>(R.id.profile)

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

        //Mendapatkan programId yang dikirimkan melalui Intent
        val programId = intent.getStringExtra("programId") ?: return // Pastikan programId tersedia
        Log.d("programId Diterima", "Program ID yang diterima: $programId")


        loadProgram(programId) // Menggunakan programId yang diterima
    }



    @SuppressLint("NotifyDataSetChanged")
    private fun loadProgram(programID : String) {
        val currentUserUid = auth.currentUser?.uid ?: return
        val myRef = database.getReference("Program").child(currentUserUid).child(programID) // Mengakses program berdasarkan programId yang dipilih


        myRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    // Ambil data program yang sesuai dengan programId
                    val program = snapshot.getValue(ProgramModels::class.java)
                    if (program != null) {
                        Log.d("DetailProgram", "Program: $program")
                        val startDate = program.start_date
                        val endDate = program.batas_akhir
                        val deskripsiTextView = findViewById<TextView>(R.id.textView4)
                        deskripsiTextView.text = program.deskripsi_program

                        if (startDate != null && endDate != null) {
                            // Proses data program yang dipilih
                            processProgramData(startDate, endDate)

                        }
                    }
                } else {
                    Toast.makeText(this@DetailProgram, "No data found for the selected Program ID.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@DetailProgram, "Failed to load data: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun processProgramData(startDate: String, endDate: String) {
        val currentUserId = auth.currentUser?.uid ?: return
        val programId = intent.getStringExtra("programId") ?: return
        val trackProgresRef = database.getReference("Program")
            .child(currentUserId)
            .child(programId)
            .child("track_progres")

        items.clear() // Bersihkan data sebelum memuat ulang RecyclerView

        // Inisialisasi adapter di sini agar trackProgresRef bisa dikirim
        adapter = AdapterDetailProgram(
            listProgram = items,
            context = this,
            trackProgresRef = trackProgresRef
        )
        binding.recyclerViewDetailProgram.adapter = adapter
        adapter.notifyDataSetChanged()

        trackProgresRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    Log.d("DetailProgram", "Data ditemukan, memuat dari Firebase...")
                    items.clear()
                    for (childSnapshot in snapshot.children) {
                        val detail = childSnapshot.getValue(DetailProgramModel::class.java)
                        if (detail != null) {
                            items.add(detail)
                        }
                    }
                    adapter.notifyDataSetChanged()
                } else {
                    Log.d("DetailProgram", "Data tidak ditemukan, menambahkan data baru...")
                    val dateRange = calculateDateRange(startDate, endDate)
                    val newData = mutableListOf<DetailProgramModel>()

                    dateRange.forEachIndexed { index, date ->
                        val detail = DetailProgramModel(
                            hari = "Hari ke-${index + 1}",
                            jumlah_hari = index + 1,
                            tanggal_waktu = date,
                            gambar_checklist = "unchecked"
                        )
                        newData.add(detail)
                    }

                    items.addAll(newData)
                    adapter.notifyDataSetChanged()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("DetailProgram", "Error saat memuat data: ${error.message}")
                Toast.makeText(this@DetailProgram, "Gagal memuat data: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }



    /**
     * Fungsi untuk menghitung rentang tanggal antara dua tanggal.
     */
    private fun calculateDateRange(startDate: String, endDate: String): List<String> {
        val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        val start = dateFormat.parse(startDate)
        val end = dateFormat.parse(endDate)
        val dateList = mutableListOf<String>()

        if (start != null && end != null && !start.after(end)) {
            val calendar = Calendar.getInstance()
            calendar.time = start

            while (!calendar.time.after(end)) {
                dateList.add(dateFormat.format(calendar.time))
                calendar.add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        return dateList
    }
}