package com.example.nusantarapustaka.ui

import android.os.Bundle
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.nusantarapustaka.R
import com.example.nusantarapustaka.data.historymodel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class HistoryActivity : AppCompatActivity() {

    private lateinit var rvHistory: RecyclerView
    private lateinit var historyAdapter: HistoryAdapter
    private var historyList = mutableListOf<historymodel>()

    private val databaseUrl = "https://nusantarapustaka-f5363-default-rtdb.asia-southeast1.firebasedatabase.app/"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        findViewById<ImageButton>(R.id.btnBack).setOnClickListener { finish() }

        rvHistory = findViewById(R.id.rvHistory)
        rvHistory.layoutManager = LinearLayoutManager(this)

        // Perbaikan: Tambahkan logic hapus di sini saat inisialisasi adapter
        historyAdapter = HistoryAdapter(historyList) { item ->
            showDeleteDialog(item)
        }
        rvHistory.adapter = historyAdapter

        loadHistoryData()
    }

    private fun loadHistoryData() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val ref = FirebaseDatabase.getInstance(databaseUrl)
            .getReference("users")
            .child(uid)
            .child("history")

        ref.orderByChild("date").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                historyList.clear()
                if (snapshot.exists()) {
                    for (ds in snapshot.children) {
                        val model = ds.getValue(historymodel::class.java)
                        if (model != null) {
                            historyList.add(model)
                        }
                    }
                    historyList.reverse()
                }
                historyAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@HistoryActivity, "Gagal memuat: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    // Fungsi untuk menampilkan konfirmasi hapus
    private fun showDeleteDialog(item: historymodel) {
        AlertDialog.Builder(this)
            .setTitle("Hapus Riwayat")
            .setMessage("Apakah Anda yakin ingin menghapus riwayat pembelian '${item.title}'?")
            .setPositiveButton("Hapus") { _, _ ->
                hapusHistoryDariFirebase(item)
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    // Fungsi eksekusi hapus ke Firebase
    private fun hapusHistoryDariFirebase(item: historymodel) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        // Alamat: users -> UID -> history -> ID_TRANSAKSI
        val ref = FirebaseDatabase.getInstance(databaseUrl)
            .getReference("users")
            .child(uid)
            .child("history")
            .child(item.historyId)

        ref.removeValue().addOnSuccessListener {
            Toast.makeText(this, "Riwayat berhasil dihapus", Toast.LENGTH_SHORT).show()
        }.addOnFailureListener { e ->
            Toast.makeText(this, "Gagal menghapus: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}