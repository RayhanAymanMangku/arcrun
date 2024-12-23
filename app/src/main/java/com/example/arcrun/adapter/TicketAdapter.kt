package com.example.arcrun.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.arcrun.databinding.ViewholderTicketBinding
import com.example.arcrun.models.Participant

class TicketAdapter : ListAdapter<Participant, TicketAdapter.TicketViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TicketViewHolder {
        val binding = ViewholderTicketBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TicketViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TicketViewHolder, position: Int) {
        holder.bind(getItem(position))

    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Participant>() {
            override fun areItemsTheSame(oldItem: Participant, newItem: Participant): Boolean {
                return oldItem.orderId == newItem.orderId
            }

            override fun areContentsTheSame(oldItem: Participant, newItem: Participant): Boolean {
                return oldItem == newItem
            }
        }
    }

    inner class TicketViewHolder(private val binding: ViewholderTicketBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(participant: Participant) {
            binding.namaEventTxt.text = participant.eventName
            binding.idPeserta.text = participant.orderId
            binding.lokasiTxt.text = participant.locationName
            binding.waktuMulaiTxt.text = participant.waktu_mulai

            Log.d("TicketAdapter", "Binding data: ${participant.eventName}, ${participant.orderId}")
        }
    }
}

