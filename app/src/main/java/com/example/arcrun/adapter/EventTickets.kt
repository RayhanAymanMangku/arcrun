package com.example.arcrun.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.arcrun.DetailEventActivity
import com.example.arcrun.R
import com.example.arcrun.models.EventTicketModels
import com.example.arcrun.databinding.CardEventItemsBinding

class EventTickets(
    private val items: ArrayList<EventTicketModels>,
    private val context: Context,
    private val eventIds: List<String>,
    private val listener: OnEventClickListener // Add listener parameter
) : RecyclerView.Adapter<EventTickets.Viewholder>() {

    interface OnEventClickListener {
        fun onEventClick(eventId: String, eventName: String, eventDesc: String, eventImage: String?, eventPrice: Int)
    }

    inner class Viewholder(private val binding: CardEventItemsBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: EventTicketModels, eventId: String) {
            // Set event data on the card
            binding.eventName.text = item.nama_event
            binding.statusTxt.text = item.status_event
            binding.eventDate.text = item.waktu_mulai
            binding.priceEvent.text = "Rp. ${item.harga}"

            when (item.status_event) {
                "Active" -> {
                    binding.statusTxt.setTextColor(ContextCompat.getColor(context, R.color.greenTxt)) // Hijau
                }
                "Finish" -> {
                    binding.statusTxt.setTextColor(ContextCompat.getColor(context, R.color.redTxt)) // Merah
                }
                else -> {
                    binding.statusTxt.setTextColor(ContextCompat.getColor(context, R.color.greenTxt)) // Warna default
                }
            }

            binding.root.setOnClickListener {
                // Call listener when event is clicked
                listener.onEventClick(item.nama_event ?: "", item.nama_event ?: "", item.deskripsi ?: "", item.gambar, item.harga)
            }

            // Load gambar menggunakan Glide
            val requestOptions = RequestOptions().error(R.drawable.imagenotfound)

            Glide.with(context)
                .load(item.gambar)
                .apply(requestOptions)
                .into(binding.eventCardImg)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Viewholder {
        val binding = CardEventItemsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return Viewholder(binding)
    }

    override fun onBindViewHolder(holder: Viewholder, position: Int) {
        holder.bind(items[position], eventIds[position])
    }

    override fun getItemCount(): Int = items.size
}
