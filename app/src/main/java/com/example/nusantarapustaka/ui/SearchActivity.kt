package com.example.nusantarapustaka.ui

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.nusantarapustaka.R
import com.example.nusantarapustaka.data.book
import com.example.nusantarapustaka.databinding.ActivitySearchBinding
import com.google.firebase.database.*
// Pastikan Activity Koleksi diimport jika berada di package yang berbeda (biasanya otomatis)
import com.example.nusantarapustaka.ui.KoleksiActivity

class SearchActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySearchBinding
    private lateinit var database: DatabaseReference
    private lateinit var searchAdapter: BookSearchAdapter
    private var allBooksList = mutableListOf<book>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.hide()

        // 1. Setup RecyclerView
        searchAdapter = BookSearchAdapter(mutableListOf())
        binding.rvBookList.layoutManager = LinearLayoutManager(this)
        binding.rvBookList.adapter = searchAdapter


        //2. Tombol Back sekarang mengarah ke Home agar alur navigasi jelas
        binding.btnBack.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        // 3. Hubungkan ke Database
        database = FirebaseDatabase.getInstance("https://nusantarapustaka-f5363-default-rtdb.asia-southeast1.firebasedatabase.app/")
            .getReference("books")

        ambilDataFirebase()

        // 4. Logika Search
        binding.etSearchPage.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s.toString().lowercase().trim()
                updateSearchUI(query)
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        binding.btnClearSearch.setOnClickListener {
            binding.etSearchPage.text.clear()
        }

        setupBottomNavigation()
    }

    private fun updateSearchUI(query: String) {
        if (query.isNotEmpty()) {
            binding.btnClearSearch.visibility = View.VISIBLE
            binding.rvBookList.visibility = View.VISIBLE
            binding.layoutEmpty.visibility = View.GONE
            filterList(query)
        } else {
            binding.btnClearSearch.visibility = View.GONE
            binding.rvBookList.visibility = View.GONE
            binding.layoutEmpty.visibility = View.VISIBLE
        }
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigation.selectedItemId = R.id.nav_search
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, MainActivity::class.java))
                    overridePendingTransition(0, 0)
                    finish()
                    true
                }
                R.id.nav_search -> true // Tetap di sini

                // --- TAMBAHAN NAVIGASI KOLEKSI ---
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

    private fun ambilDataFirebase() {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                allBooksList.clear()
                if (snapshot.exists()) {
                    for (data in snapshot.children) {
                        try {
                            val item = data.getValue(book::class.java)
                            if (item != null) {
                                allBooksList.add(item)
                            }
                        } catch (e: Exception) {
                            android.util.Log.e("SearchError", "Gagal parsing: ${e.message}")
                        }
                    }

                    val currentQuery = binding.etSearchPage.text.toString().lowercase().trim()
                    if (currentQuery.isNotEmpty()) {
                        filterList(currentQuery)
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {
                android.util.Log.e("FirebaseError", error.message)
            }
        })
    }

    private fun filterList(query: String) {
        val filtered = allBooksList.filter {
            it.title.lowercase().contains(query) || it.author.lowercase().contains(query)
        }

        searchAdapter.updateList(filtered)

        if (filtered.isEmpty() && query.isNotEmpty()) {
            binding.layoutEmpty.visibility = View.VISIBLE
            binding.rvBookList.visibility = View.GONE
        }
    }
}