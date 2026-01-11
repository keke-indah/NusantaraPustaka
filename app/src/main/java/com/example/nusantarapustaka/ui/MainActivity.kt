package com.example.nusantarapustaka.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.example.nusantarapustaka.R
import com.example.nusantarapustaka.data.book
import com.example.nusantarapustaka.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private var listBukuAsli = mutableListOf<book>()
    private lateinit var bookAdapter: BookAdapter

    private val userId = FirebaseAuth.getInstance().currentUser?.uid
    private val databaseUrl = "https://nusantarapustaka-f5363-default-rtdb.asia-southeast1.firebasedatabase.app/"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        // 1. Setup RecyclerView
        bookAdapter = BookAdapter(mutableListOf())
        binding.rvBooks.layoutManager = GridLayoutManager(this, 2)
        binding.rvBooks.adapter = bookAdapter
        binding.rvBooks.isNestedScrollingEnabled = false

        // 2. Tombol Notifikasi
        binding.btnNotification.setOnClickListener {
            startActivity(Intent(this, NotificationActivity::class.java))
        }

        // 3. Tombol Lihat Semua
        binding.tvSeeAll.setOnClickListener {
            startActivity(Intent(this, RekomendasiActivity::class.java))
        }

        // 4. LOGIKA BARU: SEARCH DUMMY (Navigasi ke SearchActivity)
        // Kita buat area container dan edittext-nya memicu perpindahan halaman
        val intentKeSearch = Intent(this, SearchActivity::class.java)

        binding.etSearchHome.setOnClickListener {
            startActivity(intentKeSearch)
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }

        binding.searchContainer.setOnClickListener {
            binding.etSearchHome.performClick()
        }

        // 5. Bottom Nav
        setupBottomNavigation()

        // Ambil Data
        ambilDataDariFirebase()
        updateBadgeNotifikasi()
    }

    // --- FUNGSI BADGE (SUDAH SINKRON DENGAN USER ID) ---
    private fun updateBadgeNotifikasi() {
        if (userId != null) {
            val dbRef = FirebaseDatabase.getInstance(databaseUrl)
                .getReference("users")
                .child(userId)
                .child("notifikasi")

            dbRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val count = snapshot.childrenCount
                    if (count > 0) {
                        binding.tvBadgeCount.visibility = View.VISIBLE
                        binding.tvBadgeCount.text = if (count > 9) "9+" else count.toString()
                    } else {
                        binding.tvBadgeCount.visibility = View.GONE
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                    Log.e("FirebaseNotif", error.message)
                }
            })
        }
    }

    private fun ambilDataDariFirebase() {
        val database = FirebaseDatabase.getInstance(databaseUrl).getReference("books")
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                listBukuAsli.clear()
                if (snapshot.exists()) {
                    for (data in snapshot.children) {
                        val itemBuku = data.getValue(book::class.java)
                        if (itemBuku != null) listBukuAsli.add(itemBuku)
                    }
                    bookAdapter.updateList(listBukuAsli)
                }
            }
            override fun onCancelled(error: DatabaseError) {
                Log.e("FirebaseError", error.message)
            }
        })
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigation.selectedItemId = R.id.nav_home
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> true
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
}