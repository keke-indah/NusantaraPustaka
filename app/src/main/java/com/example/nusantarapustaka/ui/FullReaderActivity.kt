package com.example.nusantarapustaka.ui

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.pdf.PdfRenderer
import android.os.Bundle
import android.os.ParcelFileDescriptor
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.example.nusantarapustaka.R
import com.example.nusantarapustaka.databinding.ActivityFullReaderBinding
import java.io.File
import java.io.FileOutputStream

class FullReaderActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFullReaderBinding
    private var pdfRenderer: PdfRenderer? = null
    private var parcelFileDescriptor: ParcelFileDescriptor? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFullReaderBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        // Ambil data intent (Nama PDF dan status apakah ini cuma Preview)
        val pdfFileName = intent.getStringExtra("PDF_NAME") ?: ""
        val isPreview = intent.getBooleanExtra("IS_PREVIEW", false)

        if (pdfFileName.isNotEmpty()) {
            setupPdfReader(pdfFileName, isPreview)
        } else {
            Toast.makeText(this, "File tidak ditemukan", Toast.LENGTH_SHORT).show()
            finish()
        }

        binding.btnBackPreview.setOnClickListener { finish() }
    }

    private fun setupPdfReader(fileName: String, isPreview: Boolean) {
        try {
            val file = File(cacheDir, fileName)
            if (!file.exists()) {
                assets.open(fileName).use { input ->
                    FileOutputStream(file).use { output -> input.copyTo(output) }
                }
            }

            parcelFileDescriptor = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
            pdfRenderer = PdfRenderer(parcelFileDescriptor!!)

            // Batasi 5 halaman jika isPreview = true
            val maxPages = if (isPreview) 5 else pdfRenderer!!.pageCount

            val adapter = object : RecyclerView.Adapter<PdfViewHolder>() {
                override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PdfViewHolder {
                    val view = LayoutInflater.from(parent.context).inflate(R.layout.item_pdf_page, parent, false)
                    return PdfViewHolder(view)
                }

                override fun onBindViewHolder(holder: PdfViewHolder, position: Int) {
                    val page = pdfRenderer!!.openPage(position)
                    val bitmap = Bitmap.createBitmap(page.width, page.height, Bitmap.Config.ARGB_8888)
                    val canvas = Canvas(bitmap)
                    canvas.drawColor(Color.WHITE)
                    page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                    holder.img.setImageBitmap(bitmap)
                    page.close()
                }

                override fun getItemCount(): Int = maxPages
            }

            binding.viewPagerPdf.adapter = adapter

            // Callback untuk Page Indicator dan Warning
            binding.viewPagerPdf.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    val currentPage = position + 1
                    binding.tvPageIndicator.text = "$currentPage / $maxPages"

                    // Munculkan overlay beli jika sudah di halaman terakhir preview
                    if (isPreview && currentPage == maxPages) {
                        binding.layoutBuyWarning.visibility = View.VISIBLE
                    } else {
                        binding.layoutBuyWarning.visibility = View.GONE
                    }
                }
            })

        } catch (e: Exception) {
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    class PdfViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        val img: ImageView = v.findViewById(R.id.ivPage)
    }

    override fun onDestroy() {
        pdfRenderer?.close()
        parcelFileDescriptor?.close()
        super.onDestroy()
    }
}