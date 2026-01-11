package com.example.nusantarapustaka.ui

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.nusantarapustaka.R
import com.example.nusantarapustaka.data.book
import com.google.firebase.database.*

class RekomendasiActivity : AppCompatActivity() {

    private lateinit var rvRekomendasi: RecyclerView
    private lateinit var btnBack: ImageView
    private lateinit var etSearch: EditText

    private var listBukuAsli = mutableListOf<book>()
    // Menggunakan RekomendasiAdapter agar sesuai dengan item_book_grid
    private var adapter: RekomendasiAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rekomendasi)
        supportActionBar?.hide()

        rvRekomendasi = findViewById(R.id.rvRekomendasi)
        btnBack = findViewById(R.id.btnBack)
        etSearch = findViewById(R.id.etSearch)

        // Set Grid 2 Kolom
        rvRekomendasi.layoutManager = GridLayoutManager(this, 2)

        btnBack.setOnClickListener {
            finish()
        }

        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterData(s.toString().lowercase().trim())
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        ambilDataBuku()
    }

    private fun ambilDataBuku() {
        val database = FirebaseDatabase.getInstance("https://nusantarapustaka-f5363-default-rtdb.asia-southeast1.firebasedatabase.app/")
            .getReference("books")

        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                listBukuAsli.clear()

                if (snapshot.exists()) {
                    for (data in snapshot.children) {
                        try {
                            // TAMBAHKAN filePreview dan fileFull agar bisa baca buku!
                            val buku = book(
                                title = data.child("title").value.toString(),
                                author = data.child("author").value.toString(),
                                price = data.child("price").value.toString(),
                                description = data.child("description").value.toString(),
                                imageResName = data.child("imageResName").value.toString(),
                                language = data.child("language").value.toString(),
                                publisher = data.child("publisher").value.toString(),
                                pages = data.child("pages").value.toString(),
                                releaseDate = data.child("releaseDate").value.toString(),
                                // Ambil data PDF dari Firebase
                                filePreview = data.child("filePreview").value.toString(),
                                fileFull = data.child("fileFull").value.toString()
                            )
                            listBukuAsli.add(buku)
                        } catch (e: Exception) {
                            Log.e("RekomendasiError", "Gagal baca data: ${e.message}")
                        }
                    }
                    // Setup Adapter
                    adapter = RekomendasiAdapter(listBukuAsli)
                    rvRekomendasi.adapter = adapter
                } else {
                    Toast.makeText(this@RekomendasiActivity, "Data buku tidak ditemukan", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FirebaseError", "Gagal mengambil data: ${error.message}")
            }
        })
    }

    private fun filterData(query: String) {
        val listFiltered = listBukuAsli.filter {
            it.title.lowercase().contains(query) || it.author.lowercase().contains(query)
        }
        // Update list tanpa membuat adapter baru terus-menerus
        adapter?.updateList(listFiltered)
    }
}