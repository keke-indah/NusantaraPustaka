package com.example.nusantarapustaka.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.nusantarapustaka.R
import com.example.nusantarapustaka.data.historymodel
import java.text.SimpleDateFormat
import java.util.*

// 1. Tambahkan parameter onDeleteClick agar Activity bisa menerima data yang akan dihapus
class HistoryAdapter(
    private val list: List<historymodel>,
    private val onDeleteClick: (historymodel) -> Unit
) : RecyclerView.Adapter<HistoryAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.tvHistoryTitle)
        val price: TextView = view.findViewById(R.id.tvHistoryPrice)
        val date: TextView = view.findViewById(R.id.tvHistoryDate)
        val image: ImageView = view.findViewById(R.id.ivHistoryBook)
        // 2. Deklarasikan tombol hapus dari XML baru tadi
        val btnDelete: ImageButton = view.findViewById(R.id.btnDeleteHistory)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_history, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]

        holder.title.text = item.title
        holder.price.text = item.price

        // Konversi Tanggal
        if (item.date != 0L) {
            val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
            val dateFormatted = sdf.format(Date(item.date))
            holder.date.text = dateFormatted
        } else {
            holder.date.text = "-"
        }

        // Tampilkan Gambar
        val context = holder.itemView.context
        val imageName = item.imageResName

        if (!imageName.isNullOrEmpty()) {
            val cleanImageName = imageName.split(".")[0]
            val resId = context.resources.getIdentifier(cleanImageName, "drawable", context.packageName)
            if (resId != 0) {
                holder.image.setImageResource(resId)
            } else {
                holder.image.setImageResource(R.drawable.ic_launcher_background)
            }
        } else {
            holder.image.setImageResource(R.drawable.ic_launcher_background)
        }

        // 3. Set Klik Listener pada tombol hapus
        holder.btnDelete.setOnClickListener {
            onDeleteClick(item)
        }
    }

    override fun getItemCount(): Int = list.size
}