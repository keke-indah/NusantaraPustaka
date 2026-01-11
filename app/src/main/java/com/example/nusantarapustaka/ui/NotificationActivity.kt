package com.example.nusantarapustaka.ui

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.nusantarapustaka.data.NotificationModel
import com.example.nusantarapustaka.databinding.ActivityNotificationBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class NotificationActivity : AppCompatActivity() {
    private lateinit var binding: ActivityNotificationBinding
    private lateinit var adapter: NotificationAdapter
    private val listNotif = mutableListOf<NotificationModel>()
    private val auth = FirebaseAuth.getInstance()
    private val databaseUrl = "https://nusantarapustaka-f5363-default-rtdb.asia-southeast1.firebasedatabase.app/"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNotificationBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        setupRecyclerView()

        val userId = auth.currentUser?.uid
        if (userId != null) {
            ambilDataNotifikasi(userId)
        }

        binding.btnBackNotif.setOnClickListener { finish() }

        binding.tvClearAll.setOnClickListener { hapusSemuaNotifikasi() }
    }

    private fun setupRecyclerView() {
        adapter = NotificationAdapter(listNotif)
        binding.rvNotifications.layoutManager = LinearLayoutManager(this)
        binding.rvNotifications.adapter = adapter
    }

    private fun ambilDataNotifikasi(uid: String) {
        // Alamat folder disesuaikan agar masuk ke dalam Users
        val dbRef = FirebaseDatabase.getInstance(databaseUrl)
            .getReference("users")
            .child(uid)
            .child("notifikasi")

        dbRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                listNotif.clear()
                for (data in snapshot.children) {
                    val item = data.getValue(NotificationModel::class.java)
                    if (item != null) listNotif.add(item)
                }

                // Urutkan: Notifikasi terbaru di paling atas
                listNotif.sortByDescending { it.waktu }
                adapter.notifyDataSetChanged()

                // Tampilan jika notifikasi kosong
                if (listNotif.isEmpty()) {
                    binding.tvEmptyState.visibility = View.VISIBLE
                    binding.rvNotifications.visibility = View.GONE
                } else {
                    binding.tvEmptyState.visibility = View.GONE
                    binding.rvNotifications.visibility = View.VISIBLE
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@NotificationActivity, "Gagal memuat data", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun hapusSemuaNotifikasi() {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            FirebaseDatabase.getInstance(databaseUrl)
                .getReference("users")
                .child(userId)
                .child("notifikasi")
                .removeValue()
                .addOnSuccessListener {
                    Toast.makeText(this, "Semua notifikasi dibersihkan", Toast.LENGTH_SHORT).show()
                }
        }
    }
}