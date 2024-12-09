package com.example.arcrun

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

import androidx.recyclerview.widget.LinearLayoutManager
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


class DetailProgram : AppCompatActivity() {
    private lateinit var database: FirebaseDatabase
    private lateinit var auth: FirebaseAuth
    private lateinit var adapter: AdapterDetailProgram
    private lateinit var binding: ActivityDetailProgramBinding
    private val items = ArrayList<DetailProgramModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailProgramBinding.inflate(layoutInflater)
        setContentView(binding.root)

        database = FirebaseDatabase.getInstance()
        auth = FirebaseAuth.getInstance()

        // Mendapatkan programId yang dikirimkan melalui Intent
        val programId = intent.getStringExtra("programId") ?: return // Pastikan programId tersedia


        loadProgram(programId) // Menggunakan programId yang diterima
    }



    @SuppressLint("NotifyDataSetChanged")
    private fun loadProgram(programId: String) {
        val currentUserUid = auth.currentUser?.uid ?: return
        val myRef = database.getReference("Program").child(currentUserUid).child(programId) // Mengakses program berdasarkan programId yang dipilih

        adapter = AdapterDetailProgram(listProgram = items, context = this)
        binding.recyclerViewDetailProgram.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewDetailProgram.adapter = adapter

        myRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    // Ambil data program yang sesuai dengan programId
                    val program = snapshot.getValue(ProgramModels::class.java)
                    if (program != null) {
                        Log.d("DetailProgram", "Program: $program")
                        val startDate = program.waktu_mulai
                        val endDate = program.batas_akhir

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
        val dateRange = calculateDateRange(startDate, endDate)
        items.clear() // Bersihkan list sebelum menambahkan data baru

        dateRange.forEachIndexed { index, date ->
            items.add(
                DetailProgramModel(
                    hari = "Hari ke-${index + 1}",
                    jumlah_hari = index + 1,
                    tanggal_waktu = date,
                    button_checklist = "Unchecked"
                )
            )
        }

        adapter.notifyDataSetChanged()
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