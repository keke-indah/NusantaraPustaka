package com.example.nusantarapustaka.ui

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.nusantarapustaka.data.book
import com.example.nusantarapustaka.databinding.ItemBookHorizontalBinding

class BookSearchAdapter(private var listBook: List<book>) : RecyclerView.Adapter<BookSearchAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemBookHorizontalBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemBookHorizontalBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentBook = listBook[position]
        val context = holder.itemView.context

        // 1. Ambil ID Gambar
        val imageId = context.resources.getIdentifier(currentBook.imageResName, "drawable", context.packageName)

        // 2. Set Data ke UI
        holder.binding.tvTitle.text = currentBook.title
        holder.binding.tvAuthor.text = currentBook.author
        holder.binding.tvPrice.text = currentBook.price

        if (imageId != 0) {
            holder.binding.imgBook.setImageResource(imageId)
        } else {
            holder.binding.imgBook.setImageResource(context.resources.getIdentifier("ikon", "drawable", context.packageName))
        }

        // 3. Logika Tombol Hapus (Opsional, pastikan ini sesuai keinginanmu)
        holder.binding.btnDeleteBook.setOnClickListener {
            val mutableList = listBook.toMutableList()
            if (position < mutableList.size) {
                mutableList.removeAt(position)
                updateList(mutableList)
            }
        }

        // 4. Klik ke Detail (Lengkap untuk Preview dan Full PDF)
        holder.itemView.setOnClickListener {
            val intent = Intent(context, DetailBookActivity::class.java).apply {
                putExtra("JUDUL", currentBook.title)
                putExtra("PENULIS", currentBook.author)
                putExtra("HARGA", currentBook.price)
                putExtra("DESKRIPSI", currentBook.description)
                putExtra("IMAGE_NAME", currentBook.imageResName)
                putExtra("BAHASA", currentBook.language)
                putExtra("PENERBIT", currentBook.publisher)
                putExtra("HALAMAN", currentBook.pages)
                putExtra("TANGGAL_RILIS", currentBook.releaseDate)
                putExtra("GAMBAR", imageId)

                // KIRIM KEDUANYA DI SINI
                putExtra("FILE_PREVIEW", currentBook.filePreview) // Tambahkan ini
                putExtra("FILE_FULL", currentBook.fileFull)       // Ini sudah ada
            }
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = listBook.size

    fun updateList(newList: List<book>) {
        this.listBook = newList
        notifyDataSetChanged()
    }
}