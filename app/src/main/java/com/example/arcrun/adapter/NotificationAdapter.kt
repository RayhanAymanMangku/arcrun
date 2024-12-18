package com.example.arcrun.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.arcrun.R
import com.example.arcrun.models.Transaction

class NotificationAdapter : RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder>() {

    private val transactionList = mutableListOf<Transaction>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.view_notification, parent, false)
        return NotificationViewHolder(view)
    }

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        val transaction = transactionList[position]
        holder.bind(transaction)
    }

    override fun getItemCount(): Int {
        return transactionList.size
    }

    fun setData(newList: List<Transaction>) {
        transactionList.clear()
        transactionList.addAll(newList)
        notifyDataSetChanged() // Perbarui RecyclerView
    }

    inner class NotificationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val notificationHeader: TextView = itemView.findViewById(R.id.notification_header)
        private val notificationBody: TextView = itemView.findViewById(R.id.notification_body)

        fun bind(transaction: Transaction) {
            notificationHeader.text = "Transaksi ID: ${transaction.transactionId}"
            notificationBody.text = "Status: ${transaction.status}\nEvent: ${transaction.eventName}\nAmount: Rp${transaction.amount}"
        }
    }
}
