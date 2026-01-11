package com.example.nusantarapustaka.data

data class historymodel(
// Ambil dari Book.kt
val title: String = "",
val author: String = "",
val price: String = "",
val imageResName: String = "", // URL gambar buku

// Tambahan Wajib untuk Riwayat
val historyId: String = "",    // ID Transaksi unik
val date: Long = 0,            // Waktu pembelian (Timestamp)
val status: String = "Selesai" // Status: Selesai/Gagal/Pending
)
