package com.example.nusantarapustaka.ui

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.pdf.PdfRenderer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.example.nusantarapustaka.R

class PdfAdapter(private val renderer: PdfRenderer, private val maxPages: Int) :
    RecyclerView.Adapter<PdfAdapter.PdfViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PdfViewHolder {
        // Gunakan layout match_parent agar satu halaman mengisi satu layar ViewPager2
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_pdf_page, parent, false)
        return PdfViewHolder(view)
    }

    override fun onBindViewHolder(holder: PdfViewHolder, position: Int) {
        if (position >= maxPages || position >= renderer.pageCount) return

        try {
            val page = renderer.openPage(position)

            // --- PERBAIKAN: UPSCALING AGAR JERNIH ---
            // Kita kalikan lebar & tinggi (misal x2) agar hasil render tidak pecah di layar HP tinggi
            val scale = 2
            val width = page.width * scale
            val height = page.height * scale

            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

            val canvas = Canvas(bitmap)
            canvas.drawColor(Color.WHITE) // Background putih wajib

            // Render halaman ke bitmap
            page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)

            holder.imageView.setImageBitmap(bitmap)
            page.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // --- TAMBAHAN: MEMBERSIHKAN MEMORY ---
    override fun onViewRecycled(holder: PdfViewHolder) {
        super.onViewRecycled(holder)
        // Menghapus referensi bitmap saat halaman di-swipe jauh agar RAM tidak penuh
        holder.imageView.setImageBitmap(null)
    }

    override fun getItemCount(): Int {
        val actualCount = renderer.pageCount
        return if (actualCount > maxPages) maxPages else actualCount
    }

    class PdfViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageView: ImageView = view.findViewById(R.id.ivPage)
    }
}