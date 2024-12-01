package com.example.arcrun.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.arcrun.R
import com.example.arcrun.models.EventTicketModels
import com.example.arcrun.databinding.CardEventItemsBinding


class EventTickets (
    private val items: ArrayList<EventTicketModels>,
    private val context: Context
) : RecyclerView.Adapter<EventTickets.Viewholder>() {

    inner class Viewholder(private val binding: CardEventItemsBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: EventTicketModels) {
            binding.eventName.text = item.nama_event
            binding.statusTxt.text = item.status_event
            binding.eventDate.text = item.waktu_mulai

            val requestOptions = RequestOptions()
//                .error(R.drawable.imagenotfound)

            Glide.with(context)
                .load(item.gambar)
                .apply (requestOptions)
                .into(binding.eventCardImg)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Viewholder {
        val binding = CardEventItemsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return Viewholder(binding)
    }

    override fun onBindViewHolder(holder: Viewholder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size


}