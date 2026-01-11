package com.example.nusantarapustaka.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.nusantarapustaka.R
import com.example.nusantarapustaka.data.NotificationModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class NotificationAdapter(private val list: MutableList<NotificationModel>) :
    RecyclerView.Adapter<NotificationAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTitle: TextView = view.findViewById(R.id.tvNotifTitle)
        val tvMessage: TextView = view.findViewById(R.id.tvNotifMessage)
        val btnDelete: ImageView = view.findViewById(R.id.btnDeleteNotif)
        val ivIcon: ImageView = view.findViewById(R.id.ivNotifIcon)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_notification, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]
        holder.tvTitle.text = item.judul
        holder.tvMessage.text = item.pesan

        // SET ICON DINAMIS BERDASARKAN TYPE
        when (item.type) {
            "BELI" -> holder.ivIcon.setImageResource(R.drawable.ic_order)
            "PROFIL" -> holder.ivIcon.setImageResource(R.drawable.ic_user_placeholder)
            "KEAMANAN" -> holder.ivIcon.setImageResource(R.drawable.ic_security)
            else -> holder.ivIcon.setImageResource(R.drawable.ic_notification)
        }

        // HAPUS NOTIFIKASI SATUAN
        holder.btnDelete.setOnClickListener {
            val userId = FirebaseAuth.getInstance().currentUser?.uid
            if (userId != null) {
                FirebaseDatabase.getInstance("https://nusantarapustaka-f5363-default-rtdb.asia-southeast1.firebasedatabase.app/")
                    .getReference("users")
                    .child(userId)
                    .child("notifikasi")
                    .child(item.id)
                    .removeValue()
            }
        }
    }

    override fun getItemCount(): Int = list.size
}