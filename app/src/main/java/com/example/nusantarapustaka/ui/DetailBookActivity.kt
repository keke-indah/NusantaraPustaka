package com.example.nusantarapustaka.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.nusantarapustaka.R
import com.example.nusantarapustaka.databinding.ActivityDetailBookBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class DetailBookActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDetailBookBinding
    private val userId = FirebaseAuth.getInstance().currentUser?.uid
    // Simpan URL database dalam variabel agar kodingan lebih bersih
    private val databaseUrl = "https://nusantarapustaka-f5363-default-rtdb.asia-southeast1.firebasedatabase.app/"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailBookBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        // 1. Tangkap data dari intent
        val judul = intent.getStringExtra("JUDUL") ?: ""
        val penulis = intent.getStringExtra("PENULIS") ?: ""
        val harga = intent.getStringExtra("HARGA") ?: ""
        val deskripsi = intent.getStringExtra("DESKRIPSI") ?: ""
        val gambarResId = intent.getIntExtra("GAMBAR", 0)
        val imageName = intent.getStringExtra("IMAGE_NAME") ?: ""
        val filePreview = intent.getStringExtra("FILE_PREVIEW") ?: ""
        val fileFull = intent.getStringExtra("FILE_FULL") ?: ""
        val bahasa = intent.getStringExtra("BAHASA") ?: "Indonesia"
        val penerbit = intent.getStringExtra("PENERBIT") ?: "-"
        val halaman = intent.getStringExtra("HALAMAN") ?: "-"
        val tglRilis = intent.getStringExtra("TANGGAL_RILIS") ?: "-"

        // 2. Pasang data ke tampilan UI
        binding.apply {
            tvDetailTitle.text = judul
            tvDetailPrice.text = harga
            tvDetailDescription.text = deskripsi
            imgDetailBook.setImageResource(gambarResId)
            tvInfoLanguage.text = bahasa
            tvInfoPublisher.text = penerbit
            tvInfoPages.text = halaman
            tvInfoRelease.text = tglRilis
        }

        binding.btnBack.setOnClickListener { finish() }

        // --- 3. TOMBOL SHARE ---
        binding.btnShare.setOnClickListener {
            val linkAplikasi = "https://play.google.com/store/apps/details?id=com.example.nusantarapustaka"
            val pesanShare = "Halo! Cek buku keren ini di Nusantara Pustaka: \n\n" +
                    "Judul: $judul\n" +
                    "Penulis: $penulis\n\n" +
                    "Baca di sini: $linkAplikasi"

            val intentShare = Intent(Intent.ACTION_SEND)
            intentShare.type = "text/plain"
            intentShare.putExtra(Intent.EXTRA_TEXT, pesanShare)
            startActivity(Intent.createChooser(intentShare, "Bagikan melalui:"))

            // Panggil fungsi notifikasi dengan tipe "SHARE"
            tambahNotifikasi("Berbagi Buku", "Kamu membagikan buku '$judul'", "SHARE")
        }

        // --- 4. TOMBOL PREVIEW ---
        binding.btnPreview.setOnClickListener {
            if (filePreview.isNotEmpty()) {
                val intentPreview = Intent(this, PreviewActivity::class.java).apply {
                    putExtra("PDF_NAME", filePreview)
                    putExtra("IS_PREVIEW", true)
                }
                startActivity(intentPreview)
            } else {
                Toast.makeText(this, "Preview belum tersedia", Toast.LENGTH_SHORT).show()
            }
        }

        // --- 5. TOMBOL BELI ---
        binding.btnBuy.setOnClickListener {
            val intentPayment = Intent(this, PaymentActivity::class.java).apply {
                putExtra("JUDUL", judul)
                putExtra("PENULIS", penulis)
                putExtra("HARGA", harga)
                putExtra("GAMBAR", gambarResId)
                putExtra("IMAGE_NAME", imageName)
                putExtra("FILE_FULL", fileFull)
                putExtra("DESKRIPSI", deskripsi)
                putExtra("BAHASA", bahasa)
                putExtra("PENERBIT", penerbit)
                putExtra("HALAMAN", halaman)
                putExtra("TANGGAL_RILIS", tglRilis)
            }
            startActivity(intentPayment)
        }

        // --- 6. TOMBOL FAVORIT ---
        binding.btnFavorite.setOnClickListener {
            if (userId != null) {
                val database = FirebaseDatabase.getInstance(databaseUrl)
                val ref = database.getReference("users").child(userId).child("wishlist")

                val dataWishlist = mapOf(
                    "title" to judul,
                    "author" to penulis,
                    "price" to harga,
                    "imageResName" to imageName,
                    "fileFull" to fileFull,
                    "description" to deskripsi,
                    "publisher" to penerbit,
                    "pages" to halaman,
                    "releaseDate" to tglRilis
                )

                ref.child(judul).setValue(dataWishlist).addOnSuccessListener {
                    binding.btnFavorite.setImageResource(R.drawable.ic_heart_filled)
                    Toast.makeText(this, "Tersimpan di Wishlist", Toast.LENGTH_SHORT).show()

                    // Panggil fungsi notifikasi dengan tipe "WISHLIST"
                    tambahNotifikasi("Wishlist", "Berhasil menambah '$judul' ke favorit.", "WISHLIST")
                }
            }
        }
    }

    // --- FUNGSI NOTIFIKASI (SUDAH DISINKRONKAN KE FOLDER USERS) ---
    private fun tambahNotifikasi(judulNotif: String, pesanNotif: String, tipe: String) {
        if (userId != null) {
            // ALAMAT BARU: users -> UID -> notifikasi
            val dbRef = FirebaseDatabase.getInstance(databaseUrl)
                .getReference("users")
                .child(userId)
                .child("notifikasi")

            val notifId = dbRef.push().key ?: return
            val dataNotif = mapOf(
                "id" to notifId,
                "judul" to judulNotif,
                "pesan" to pesanNotif,
                "waktu" to System.currentTimeMillis(),
                "type" to tipe // Penting untuk icon di adapter
            )
            dbRef.child(notifId).setValue(dataNotif)
        }
    }
}