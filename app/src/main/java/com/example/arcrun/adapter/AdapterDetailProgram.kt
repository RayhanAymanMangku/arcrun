package com.example.arcrun.adapter

import android.content.Context
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.arcrun.R
import com.example.arcrun.models.DetailProgramModel
import com.google.firebase.database.DatabaseReference

class AdapterDetailProgram(
    private val context: Context,
    private val listProgram: MutableList<DetailProgramModel>,
    private val trackProgresRef: DatabaseReference // Tambahkan referensi Firebase
): RecyclerView.Adapter<AdapterDetailProgram.DetailViewHolder>() {

    inner class DetailViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val hariTextView: TextView = itemView.findViewById(R.id.hariTxt)
        private val jumlahHariTextView: TextView = itemView.findViewById(R.id.numberDayTxt)
        private val tanggalTextView: TextView = itemView.findViewById(R.id.tanggalWaktuTxt)
        private val buttonChecklist: TextView = itemView.findViewById(R.id.gambar_checklist)

        fun bind(program: DetailProgramModel, position: Int) {
            hariTextView.text = program.hari
            jumlahHariTextView.text = "${program.jumlah_hari}"
            tanggalTextView.text = program.tanggal_waktu

            val drawable = buttonChecklist.background as? android.graphics.drawable.VectorDrawable
            // Atur warna checklist berdasarkan status
            if (program.gambar_checklist == "checked") {
                drawable?.setTint(Color.parseColor("#4CAF50")) // Ubah warna // Hijau
            } else {
                drawable?.setTint(Color.parseColor("#808080")) // Ubah warna // Hijau
            }

            buttonChecklist.setOnClickListener {
                // Toggle status checklist
                val newStatus = if (program.gambar_checklist == "unchecked") "checked" else "unchecked"
                program.gambar_checklist = newStatus

                // Update ke Firebase
                val key = trackProgresRef.child(position.toString())
                key.ref.setValue(program)
                    .addOnSuccessListener {
                        Log.d("AdapterDetailProgram", "Checklist diperbarui di Firebase.")
                        Toast.makeText(context, "Checklist diperbarui", Toast.LENGTH_SHORT).show()
                        notifyItemChanged(position)
                    }
                    .addOnFailureListener { e ->
                        Log.e("AdapterDetailProgram", "Gagal memperbarui checklist: ${e.message}")
                        Toast.makeText(context, "Gagal memperbarui checklist", Toast.LENGTH_SHORT).show()
                    }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DetailViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.view_detailprogram, parent, false)
        return DetailViewHolder(view)
    }

    override fun onBindViewHolder(holder: DetailViewHolder, position: Int) {
        val program = listProgram[position]
        holder.bind(program, position)
    }

    override fun getItemCount(): Int {
        return listProgram.size
    }
}