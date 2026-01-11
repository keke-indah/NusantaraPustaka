package com.example.nusantarapustaka.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.nusantarapustaka.R
import com.example.nusantarapustaka.data.book
import com.example.nusantarapustaka.databinding.ActivityKoleksiBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class KoleksiActivity : AppCompatActivity() {

    private lateinit var binding: ActivityKoleksiBinding
    private lateinit var adapter: KoleksiAdapter
    private val listBuku = mutableListOf<book>()

    private val auth = FirebaseAuth.getInstance()
    // URL Database kamu sudah benar
    private val databaseUrl = "https://nusantarapustaka-f5363-default-rtdb.asia-southeast1.firebasedatabase.app/"
    private val database = FirebaseDatabase.getInstance(databaseUrl).reference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityKoleksiBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        setupRecyclerView()
        setupBottomNavigation()
        ambilDataKoleksi()

        // Tombol Back sekarang mengarah ke Home agar alur navigasi jelas
        binding.btnBack.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }

    private fun setupRecyclerView() {
        // Inisialisasi adapter dengan list kosong
        adapter = KoleksiAdapter(listBuku)
        binding.rvKoleksi.layoutManager = LinearLayoutManager(this)
        binding.rvKoleksi.adapter = adapter
    }

    private fun ambilDataKoleksi() {
        val uid = auth.currentUser?.uid ?: return

        // Path: users -> {uid} -> koleksi
        val userKoleksiRef = database.child("users").child(uid).child("koleksi")

        userKoleksiRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                listBuku.clear()
                if (snapshot.exists()) {
                    for (data in snapshot.children) {
                        try {
                            // Firebase otomatis memetakan JSON ke data class 'book'
                            val buku = data.getValue(book::class.java)
                            if (buku != null) {
                                listBuku.add(buku)
                            }
                        } catch (e: Exception) {
                            // Menghindari crash jika ada data yang formatnya tidak sesuai
                        }
                    }
                }

                // Beritahu adapter untuk memperbarui tampilan
                adapter.notifyDataSetChanged()

                // Jika kosong setelah looping, tampilkan toast
                if (listBuku.isEmpty()) {
                    Toast.makeText(this@KoleksiActivity, "Koleksi masih kosong", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@KoleksiActivity, "Gagal memuat data: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun setupBottomNavigation() {
        // Menandai menu Koleksi sedang aktif
        binding.bottomNavigation.selectedItemId = R.id.nav_koleksi

        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, MainActivity::class.java))
                    overridePendingTransition(0, 0)
                    finish()
                    true
                }
                R.id.nav_search -> {
                    startActivity(Intent(this, SearchActivity::class.java))
                    overridePendingTransition(0, 0)
                    finish()
                    true
                }
                R.id.nav_koleksi -> true // Tetap di halaman ini
                R.id.nav_profile -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
                    overridePendingTransition(0, 0)
                    finish()
                    true
                }
                else -> false
            }
        }
    }

    // Tambahkan ini agar saat menekan tombol back di HP, tidak keluar aplikasi tapi ke Home
    override fun onBackPressed() {
        super.onBackPressed()
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}