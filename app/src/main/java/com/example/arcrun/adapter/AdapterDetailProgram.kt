package com.example.arcrun.adapter

import com.example.arcrun.models.DetailProgramModel
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.arcrun.DetailProgram
import com.example.arcrun.R


class AdapterDetailProgram(
    private val context: Context,
    private val listProgram: List<DetailProgramModel>
): RecyclerView.Adapter<AdapterDetailProgram.DetailViewHolder>() {

    inner class DetailViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val hariTextView: TextView = itemView.findViewById(R.id.hariTxt)
        private val jumlahHariTextView: TextView = itemView.findViewById(R.id.numberDayTxt)
        private val tanggalTextView: TextView = itemView.findViewById(R.id.tanggalWaktuTxt)
        private val buttonChecklist: Button = itemView.findViewById(R.id.buttonChecklist)

        fun bind(program: DetailProgramModel) {
            hariTextView.text = program.hari
            jumlahHariTextView.text = "${program.jumlah_hari}"
            tanggalTextView.text = program.tanggal_waktu
            buttonChecklist.text = program.button_checklist

            // Set listener untuk button checklist jika ada
            // seharusnya berisi poin atau adanya penambahan poin ketika diklik tetapi blom ada
            buttonChecklist.setOnClickListener {
                // Contoh jika ingin melakukan aksi saat button diklik
                Toast.makeText(itemView.context, "Button di ${program.hari} diklik", Toast.LENGTH_SHORT).show()
            }

            // Set listener untuk item RecyclerView
            itemView.setOnClickListener {
                // Membuat Intent untuk berpindah ke DetailActivity
                val intent = Intent(itemView.context, DetailProgram::class.java)

                // Menyertakan data dengan Intent (menggunakan Parcelable)
                intent.putExtra("detail_program", program)

                // Mulai Activity tujuan
                itemView.context.startActivity(intent)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DetailViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.view_detailprogram, parent, false)
        return DetailViewHolder(view)
    }

    override fun onBindViewHolder(holder: DetailViewHolder, position: Int) {
        val program = listProgram[position]

        // Mengatur data ke dalam item view
        holder.bind(program)
    }

    override fun getItemCount(): Int {
        return listProgram.size
    }
}