package com.example.nusantarapustaka.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.nusantarapustaka.R
import com.example.nusantarapustaka.databinding.ActivityProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.hide()
        auth = FirebaseAuth.getInstance()

        // 1. Inisialisasi Fitur
        setupBottomNavigation()
        loadUserData()

        // 2. Tombol Back
        binding.btnBack.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        // 3. Tombol Edit Profil
        binding.btnEditProfile.setOnClickListener {
            try {
                startActivity(Intent(this, EditProfileActivity::class.java))
            } catch (e: Exception) {
                Toast.makeText(this, "Halaman Edit Profil belum tersedia", Toast.LENGTH_SHORT).show()
            }
        }

        // 4. Menu List (Wishlist, Riwayat, Keamanan, & KOLEKSI)

        // --- MENU KOLEKSI (Menggunakan ID menuNotification di XML) ---
        binding.menuNotification.setOnClickListener {
            try {
                val intent = Intent(this, NotificationActivity::class.java)
                startActivity(intent)
            } catch (e: Exception) {
                Toast.makeText(this, "Halaman Notifikasi", Toast.LENGTH_SHORT).show()
            }
        }

        binding.menuWishlist.setOnClickListener {
            try {
                startActivity(Intent(this, WishlistActivity::class.java))
            } catch (e: Exception) {
                Toast.makeText(this, "Halaman Wishlist belum tersedia", Toast.LENGTH_SHORT).show()
            }
        }

        binding.menuHistory.setOnClickListener {
            try {
                val intent = Intent(this, HistoryActivity::class.java)
                startActivity(intent)
            } catch (e: Exception) {
                Toast.makeText(this, "Gagal membuka Riwayat Pembelian", Toast.LENGTH_SHORT).show()
            }
        }

        binding.menuSecurity.setOnClickListener {
            try {
                startActivity(Intent(this, SecurityActivity::class.java))
            } catch (e: Exception) {
                Toast.makeText(this, "Gagal membuka Keamanan & Sandi", Toast.LENGTH_SHORT).show()
            }
        }

        // 5. Tombol Logout (Dengan Konfirmasi)
        binding.btnLogout.setOnClickListener {
            val builder = androidx.appcompat.app.AlertDialog.Builder(this)
            builder.setTitle("Keluar Akun")
            builder.setMessage("Apakah Anda yakin ingin keluar dari akun ini?")

            // Tombol YA
            builder.setPositiveButton("Ya, Keluar") { _, _ ->
                auth.signOut()
                val intent = Intent(this, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }

            // Tombol TIDAK (Batal)
            builder.setNegativeButton("Batal") { dialog, _ ->
                dialog.dismiss() // Menutup kotak dialog
            }

            // Menampilkan Dialog
            val alertDialog = builder.create()
            alertDialog.show()
        }
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigation.selectedItemId = R.id.nav_profile

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
                R.id.nav_koleksi -> {
                    startActivity(Intent(this, KoleksiActivity::class.java))
                    overridePendingTransition(0, 0)
                    finish()
                    true
                }
                R.id.nav_profile -> true
                else -> false
            }
        }
    }

    private fun loadUserData() {
        val user = auth.currentUser
        if (user != null) {
            binding.tvProfileEmail.text = user.email
            val uid = user.uid
            database = FirebaseDatabase.getInstance("https://nusantarapustaka-f5363-default-rtdb.asia-southeast1.firebasedatabase.app/")
                .getReference("users").child(uid)

            database.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val nama = snapshot.child("name").value.toString()
                        binding.tvProfileName.text = if (nama != "null" && nama.isNotEmpty()) nama else "User Nusantara"
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@ProfileActivity, "Koneksi bermasalah", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }
}