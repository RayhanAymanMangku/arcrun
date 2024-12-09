package com.example.arcrun

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.arcrun.databinding.ActivityInputProgramBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class InputProgramActivity : AppCompatActivity() {
    private lateinit var binding: ActivityInputProgramBinding
    private lateinit var database: FirebaseDatabase
    private lateinit var auth: FirebaseAuth
    private val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()) // Format tanggal

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInputProgramBinding.inflate(layoutInflater)
        setContentView(binding.root)

        database = FirebaseDatabase.getInstance()
        auth = FirebaseAuth.getInstance()

        binding.startDateInput.setOnClickListener { showDatePicker { date -> binding.startDateInput.setText(date) } }
        binding.endDateInput.setOnClickListener { showDatePicker { date -> binding.endDateInput.setText(date) } }

        binding.submitAddProgramBtn.setOnClickListener { saveProgramToFirebase() }
    }

    private fun showDatePicker(onDateSelected: (String) -> Unit) {
        val calendar = Calendar.getInstance()
        DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                calendar.set(year, month, dayOfMonth)
                onDateSelected(dateFormat.format(calendar.time))
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun saveProgramToFirebase() {
        val namaProgram = binding.namaProgramInput.text.toString()
        val deskripsi = binding.deskripsiInput.text.toString()
        val startDate = binding.startDateInput.text.toString()
        val endDate = binding.endDateInput.text.toString()

        // Validasi input
        if (namaProgram.isEmpty() || deskripsi.isEmpty() || startDate.isEmpty() || endDate.isEmpty()) {
            Toast.makeText(this, "Mohon lengkapi semua data!", Toast.LENGTH_SHORT).show()
            return
        }

        val currentUserUid = auth.currentUser?.uid ?: return // Mendapatkan UID pengguna yang sedang login

        // Simpan data program ke Firebase dengan UID pengguna sebagai kunci utama
        val programId = database.getReference("Program").child(currentUserUid).push().key ?: return
        val program = mapOf(
            "nama_program" to namaProgram,
            "deskripsi_program" to deskripsi,
            "start_date" to startDate,
            "batas_akhir" to endDate,
            "status_program" to "active"
        )

        database.getReference("Program")
            .child(currentUserUid) // Menyimpan data program di bawah UID pengguna
            .child(programId)
            .setValue(program)
            .addOnSuccessListener {
                Toast.makeText(this, "Program berhasil disimpan!", Toast.LENGTH_SHORT).show()
                finish() // Kembali ke Activity sebelumnya
            }
            .addOnFailureListener {
                Toast.makeText(this, "Gagal menyimpan program", Toast.LENGTH_SHORT).show()
            }
    }
}