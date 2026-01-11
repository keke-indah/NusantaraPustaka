package com.example.nusantarapustaka.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.example.nusantarapustaka.R
import com.example.nusantarapustaka.data.book
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

// Ganti val menjadi var agar list bisa diperbarui lewat updateList
class WishlistAdapter(private var listBuku: MutableList<book>) :
    RecyclerView.Adapter<WishlistAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTitle: TextView = view.findViewById(R.id.tvTitleWishlist)
        val tvAuthor: TextView = view.findViewById(R.id.tvAuthorWishlist)
        val imgBook: ImageView = view.findViewById(R.id.ivBookCover)
        val btnRemove: Button = view.findViewById(R.id.btnRemoveWishlist)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_wishlist, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val buku = listBuku[position]
        val context = holder.itemView.context

        holder.tvTitle.text = buku.title
        holder.tvAuthor.text = "by ${buku.author}"

        // --- Logika Gambar ---
        if (!buku.imageResName.isNullOrEmpty()) {
            val imageId = context.resources.getIdentifier(
                buku.imageResName,
                "drawable",
                context.packageName
            )
            if (imageId != 0) {
                holder.imgBook.setImageResource(imageId)
            } else {
                // Gambar default jika tidak ditemukan
                holder.imgBook.setImageResource(R.drawable.icon)
            }
        }

        // --- Logika Tombol Hapus ---
        holder.btnRemove.setOnClickListener {
            AlertDialog.Builder(context).apply {
                setTitle("Hapus Wishlist")
                setMessage("Apakah Anda yakin ingin menghapus '${buku.title}'?")
                setPositiveButton("Ya") { _, _ ->
                    val userId = FirebaseAuth.getInstance().currentUser?.uid
                    if (userId != null) {
                        val db = FirebaseDatabase.getInstance("https://nusantarapustaka-f5363-default-rtdb.asia-southeast1.firebasedatabase.app/")
                            .getReference("users").child(userId).child("wishlist")

                        // Hapus dari Firebase berdasarkan judul
                        db.child(buku.title).removeValue().addOnSuccessListener {
                            Toast.makeText(context, "Berhasil dihapus", Toast.LENGTH_SHORT).show()
                            // Data akan terupdate otomatis via addValueEventListener di Activity
                        }
                    }
                }
                setNegativeButton("Batal") { dialog, _ -> dialog.dismiss() }
                create().show()
            }
        }
    }

    override fun getItemCount(): Int = listBuku.size

    // --- FUNGSI PENTING: Untuk update data dari WishlistActivity ---
    fun updateList(newList: List<book>) {
        this.listBuku = newList.toMutableList()
        notifyDataSetChanged()
    }
}