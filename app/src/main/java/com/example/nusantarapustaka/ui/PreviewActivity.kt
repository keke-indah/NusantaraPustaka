package com.example.nusantarapustaka.ui

import android.graphics.pdf.PdfRenderer
import android.os.Bundle
import android.os.ParcelFileDescriptor
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.example.nusantarapustaka.databinding.ActivityPreviewBinding
import java.io.File
import java.io.FileOutputStream

class PreviewActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPreviewBinding
    private var pdfRenderer: PdfRenderer? = null
    // Ubah jadi 'var' agar bisa menyesuaikan jika buku aslinya sangat pendek
    private var maxPages = 5

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPreviewBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        val fileName = intent.getStringExtra("PDF_NAME") ?: ""

        try {
            val file = File(cacheDir, fileName)
            assets.open(fileName).use { input ->
                FileOutputStream(file).use { output -> input.copyTo(output) }
            }

            val parcelFileDescriptor = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
            pdfRenderer = PdfRenderer(parcelFileDescriptor)

            // --- PERBAIKAN 1: Validasi jumlah halaman asli ---
            val actualPageCount = pdfRenderer?.pageCount ?: 0
            if (actualPageCount < maxPages) {
                maxPages = actualPageCount
            }

            binding.viewPagerPdf.adapter = PdfAdapter(pdfRenderer!!, maxPages)

            // --- PERBAIKAN 2: Paksa cek halaman pertama saat dibuka ---
            updateWarningUI(0)

            binding.viewPagerPdf.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    updateWarningUI(position)
                }
            })

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Gagal memuat buku", Toast.LENGTH_SHORT).show()
        }

        binding.btnBackPreview.setOnClickListener { finish() }

        binding.btnBuyInPreview.setOnClickListener {
            finish()
            Toast.makeText(this, "Silakan klik tombol Beli di halaman Detail", Toast.LENGTH_SHORT).show()
        }
    }

    // --- PERBAIKAN 3: Fungsi terpusat untuk kontrol Peringatan ---
    private fun updateWarningUI(position: Int) {
        val currentPage = position + 1
        binding.tvPageIndicator.text = "$currentPage / $maxPages"

        // Menggunakan >= lebih aman untuk mendeteksi halaman terakhir
        if (currentPage >= maxPages) {
            binding.layoutBuyWarning.visibility = View.VISIBLE
            binding.tvPageIndicator.visibility = View.GONE
        } else {
            binding.layoutBuyWarning.visibility = View.GONE
            binding.tvPageIndicator.visibility = View.VISIBLE
        }
    }

    override fun onDestroy() {
        pdfRenderer?.close()
        super.onDestroy()
    }
}