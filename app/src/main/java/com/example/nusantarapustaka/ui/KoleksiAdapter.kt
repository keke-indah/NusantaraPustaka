package com.example.nusantarapustaka.ui

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.nusantarapustaka.data.book
import com.example.nusantarapustaka.databinding.ItemKoleksiBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class KoleksiAdapter(private var listBuku: MutableList<book>) :
    RecyclerView.Adapter<KoleksiAdapter.KoleksiViewHolder>() {

    fun updateData(newList: List<book>) {
        listBuku.clear()
        listBuku.addAll(newList)
        notifyDataSetChanged()
    }

    inner class KoleksiViewHolder(private val binding: ItemKoleksiBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(buku: book) {
            val context = itemView.context

            binding.tvTitle.text = if (buku.title.isNotEmpty()) buku.title else "Tanpa Judul"
            binding.tvAuthor.text = if (buku.author.isNotEmpty()) buku.author else "Tanpa Penulis"

            // LOGIKA GAMBAR
            val imageName = buku.imageResName.lowercase().replace(" ", "_").trim()
            val imageId = context.resources.getIdentifier(imageName, "drawable", context.packageName)

            if (imageId != 0) {
                binding.imgBook.setImageResource(imageId)
            } else {
                binding.imgBook.setImageResource(android.R.drawable.ic_menu_gallery)
            }

            // --- LOGIKA HAPUS ---
            binding.btnDelete.setOnClickListener {
                val currentPos = adapterPosition
                if (currentPos != RecyclerView.NO_POSITION) {
                    val bookTitle = buku.title

                    androidx.appcompat.app.AlertDialog.Builder(context)
                        .setTitle("Hapus Koleksi")
                        .setMessage("Apakah Anda yakin ingin menghapus '${bookTitle}'?")
                        .setPositiveButton("Hapus") { _, _ ->
                            val uid = FirebaseAuth.getInstance().currentUser?.uid
                            if (uid != null) {
                                val dbRef = FirebaseDatabase.getInstance("https://nusantarapustaka-f5363-default-rtdb.asia-southeast1.firebasedatabase.app/")
                                    .getReference("users")
                                    .child(uid)
                                    .child("koleksi")
                                    .child(bookTitle)

                                dbRef.removeValue().addOnSuccessListener {
                                    removeItem(currentPos)
                                    Toast.makeText(context, "Buku telah dihapus", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                        .setNegativeButton("Batal", null)
                        .show()
                }
            }

            // --- LOGIKA BACA (SUDAH DIAKTIFKAN) ---
            binding.btnRead.setOnClickListener {
                if (buku.fileFull.isNotEmpty()) {
                    // Beri tahu user proses sedang berjalan
                    Toast.makeText(context, "Membuka: ${buku.title}", Toast.LENGTH_SHORT).show()

                    // Pindah ke FullReaderActivity
                    val intent = Intent(context, FullReaderActivity::class.java).apply {
                        putExtra("PDF_NAME", buku.fileFull)
                        putExtra("JUDUL", buku.title)
                        // Karena dibuka dari koleksi, kita kirim FALSE (bukan preview)
                        putExtra("IS_PREVIEW", false)
                    }
                    context.startActivity(intent)
                } else {
                    Toast.makeText(context, "File PDF tidak ditemukan", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): KoleksiViewHolder {
        val binding = ItemKoleksiBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return KoleksiViewHolder(binding)
    }

    override fun onBindViewHolder(holder: KoleksiViewHolder, position: Int) {
        holder.bind(listBuku[position])
    }

    override fun getItemCount(): Int = listBuku.size

    fun removeItem(position: Int) {
        if (position >= 0 && position < listBuku.size) {
            listBuku.removeAt(position)
            notifyItemRemoved(position)
            notifyItemRangeChanged(position, listBuku.size)
        }
    }
}