package com.example.arcrun.adapter

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.arcrun.R
import com.example.arcrun.databinding.ViewholderProgramBinding
import com.example.arcrun.models.ProgramModels
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class BuatProgram(

    private val items: ArrayList<ProgramModels>, // List program
    private val context: Context
) : RecyclerView.Adapter<BuatProgram.Viewholder>() {

    inner class Viewholder(private val binding: ViewholderProgramBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: ProgramModels) {

            val currentDate = Calendar.getInstance().time
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

            // Parse batas_akhir menjadi Date
            val endDate = item.batas_akhir?.let {
                sdf.parse(it)
            }

            // Tentukan status program berdasarkan tanggal
            item.status_program = if (endDate != null && currentDate.after(endDate)) {
                "Inactive"
            } else {
                "Active"
            }
            binding.namaProgramTxt.text = item.nama_program
            binding.statusProgramTxt.text = item.status_program
            binding.batasAkhirTxt.text = item.batas_akhir

            // Mengubah warna berdasarkan status
            if (item.status_program == "Active") {
                binding.statusProgramTxt.setTextColor(Color.parseColor("#4CAF50")) // Hijau untuk Active
            } else {
                binding.statusProgramTxt.setTextColor(Color.RED) // Merah untuk Inactive
            }

            // Configurasi gambar dengan Glide (Jika ada)
            val requestOptions = RequestOptions().error(R.drawable.imagenotfound)

            Glide.with(context)
                .load(item.gambar)
                .apply(requestOptions)
                .into(binding.gambarBg)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Viewholder {
        val binding = ViewholderProgramBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return Viewholder(binding)
    }

    override fun onBindViewHolder(holder: Viewholder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size
}