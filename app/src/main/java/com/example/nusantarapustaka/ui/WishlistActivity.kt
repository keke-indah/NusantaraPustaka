package com.example.nusantarapustaka.ui

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.nusantarapustaka.data.book
import com.example.nusantarapustaka.databinding.ActivityWishlistBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class WishlistActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWishlistBinding
    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private lateinit var wishlistAdapter: WishlistAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWishlistBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        // 1. Inisialisasi Firebase
        auth = FirebaseAuth.getInstance()
        val userId = auth.currentUser?.uid

        // 2. Setup RecyclerView & Adapter di awal
        wishlistAdapter = WishlistAdapter(mutableListOf())
        binding.rvWishlist.layoutManager = LinearLayoutManager(this)
        binding.rvWishlist.adapter = wishlistAdapter

        // 3. Tombol Kembali
        binding.btnBackWishlist.setOnClickListener {
            finish()
        }

        // 4. Ambil data jika User ID tersedia
        if (userId != null) {
            database = FirebaseDatabase.getInstance("https://nusantarapustaka-f5363-default-rtdb.asia-southeast1.firebasedatabase.app/")
                .getReference("users").child(userId).child("wishlist")

            ambilDataWishlist()
        } else {
            // Jika user belum login, langsung tampilkan empty state
            tampilkanEmptyState(true)
            Toast.makeText(this, "Silakan login terlebih dahulu", Toast.LENGTH_SHORT).show()
        }
    }

    private fun ambilDataWishlist() {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val listBuku = mutableListOf<book>()

                if (snapshot.exists()) {
                    for (data in snapshot.children) {
                        try {
                            val item = data.getValue(book::class.java)
                            if (item != null) {
                                listBuku.add(item)
                            }
                        } catch (e: Exception) {
                            Log.e("WishlistError", "Gagal parsing buku: ${e.message}")
                        }
                    }
                }

                // 5. Logika Tampilan: Jika list kosong, munculkan layoutEmptyWishlist
                if (listBuku.isEmpty()) {
                    tampilkanEmptyState(true)
                } else {
                    tampilkanEmptyState(false)
                    wishlistAdapter.updateList(listBuku)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("WishlistError", "Gagal ambil data: ${error.message}")
            }
        })
    }

    // Fungsi pembantu untuk mengatur visibility
    private fun tampilkanEmptyState(isEmpty: Boolean) {
        if (isEmpty) {
            binding.rvWishlist.visibility = View.GONE
            binding.layoutEmptyWishlist.visibility = View.VISIBLE
        } else {
            binding.rvWishlist.visibility = View.VISIBLE
            binding.layoutEmptyWishlist.visibility = View.GONE
        }
    }
}