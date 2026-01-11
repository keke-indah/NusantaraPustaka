package com.example.nusantarapustaka.ui

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.nusantarapustaka.R
import com.example.nusantarapustaka.data.book
import com.example.nusantarapustaka.databinding.ItemBookGridBinding

class RekomendasiAdapter(private var listBook: List<book>) : RecyclerView.Adapter<RekomendasiAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemBookGridBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemBookGridBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentBook = listBook[position]
        val context = holder.itemView.context

        // 1. Ambil ID Gambar dari Drawable (berdasarkan nama di Firebase)
        val imageId = context.resources.getIdentifier(currentBook.imageResName, "drawable", context.packageName)

        // 2. Set Data ke UI (Disesuaikan persis dengan ID di item_book_grid.xml kamu)
        holder.binding.tvBookTitle.text = currentBook.title
        holder.binding.tvBookAuthor.text = currentBook.author
        holder.binding.tvBookPrice.text = currentBook.price

        if (imageId != 0) {
            holder.binding.ivBookCover.setImageResource(imageId)
        } else {
            // Jika gambar tidak ditemukan, gunakan gambar default ic_book
            holder.binding.ivBookCover.setImageResource(R.drawable.ic_book)
        }

        // 3. Klik ke Detail (Membawa SEMUA data termasuk link FILE PDF)
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

                // DATA PENTING AGAR BISA BACA BUKU
                putExtra("FILE_PREVIEW", currentBook.filePreview)
                putExtra("FILE_FULL", currentBook.fileFull)
            }
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = listBook.size

    // Fungsi untuk memperbarui daftar saat pencarian (Search)
    fun updateList(newList: List<book>) {
        this.listBook = newList
        notifyDataSetChanged()
    }
}