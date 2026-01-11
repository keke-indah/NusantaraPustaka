package com.example.nusantarapustaka.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.nusantarapustaka.R
import com.example.nusantarapustaka.data.historymodel
import com.example.nusantarapustaka.databinding.ActivityPaymentBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.text.NumberFormat
import java.util.*

class PaymentActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPaymentBinding
    private var metodeUtama = ""
    private var bankTerpilih = ""
    private val databaseUrl = "https://nusantarapustaka-f5363-default-rtdb.asia-southeast1.firebasedatabase.app/"
    private val userId = FirebaseAuth.getInstance().currentUser?.uid

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPaymentBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        val judul = intent.getStringExtra("JUDUL") ?: ""
        val penulis = intent.getStringExtra("PENULIS") ?: ""
        val hargaStr = intent.getStringExtra("HARGA") ?: "0"
        val gambarId = intent.getIntExtra("GAMBAR", 0)
        val imageName = intent.getStringExtra("IMAGE_NAME") ?: ""
        val fileFull = intent.getStringExtra("FILE_FULL") ?: ""
        val deskripsi = intent.getStringExtra("DESKRIPSI") ?: ""
        val bahasa = intent.getStringExtra("BAHASA") ?: "Indonesia"
        val penerbit = intent.getStringExtra("PENERBIT") ?: "-"
        val halaman = intent.getStringExtra("HALAMAN") ?: "-"
        val tglRilis = intent.getStringExtra("TANGGAL_RILIS") ?: "-"

        binding.apply {
            tvTitlePayment.text = judul
            tvAuthorPayment.text = penulis
            tvPriceItem.text = hargaStr
            if (gambarId != 0) imgBookPayment.setImageResource(gambarId)
        }

        setupPaymentMethods()
        hitungTotal(hargaStr)

        binding.btnBackPayment.setOnClickListener { finish() }

        binding.btnConfirmPayment.setOnClickListener {
            if (validateSelection()) {
                prosesPembayaran(
                    judul, penulis, imageName, fileFull,
                    deskripsi, bahasa, penerbit, halaman, tglRilis
                )
            }
        }
    }

    private fun prosesPembayaran(
        judul: String, penulis: String, img: String, file: String,
        desc: String, lang: String, pub: String, page: String, date: String
    ) {
        val uid = userId ?: return
        val hargaFinal = binding.tvTotalPrice.text.toString()

        binding.btnConfirmPayment.isEnabled = false
        binding.btnConfirmPayment.text = "Memproses..."

        val database = FirebaseDatabase.getInstance(databaseUrl)

        val refKoleksi = database.getReference("users").child(uid).child("koleksi")
        val dataBuku = mapOf(
            "title" to judul,
            "author" to penulis,
            "price" to hargaFinal,
            "imageResName" to img,
            "description" to desc,
            "language" to lang,
            "publisher" to pub,
            "pages" to page,
            "releaseDate" to date,
            "fileFull" to file
        )

        refKoleksi.child(judul).setValue(dataBuku).addOnSuccessListener {
            val refHistory = database.getReference("users").child(uid).child("history")
            val historyId = refHistory.push().key ?: ""

            val dataHistory = historymodel(
                title = judul,
                author = penulis,
                price = hargaFinal,
                imageResName = img,
                historyId = historyId,
                date = System.currentTimeMillis(),
                status = "Selesai"
            )

            refHistory.child(historyId).setValue(dataHistory).addOnSuccessListener {

                // PANGGIL FUNGSI NOTIFIKASI
                kirimNotifikasiPembelian(judul)

                Toast.makeText(this, "Pembayaran Berhasil!", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, KoleksiActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }

        }.addOnFailureListener {
            binding.btnConfirmPayment.isEnabled = true
            binding.btnConfirmPayment.text = "Konfirmasi Pembayaran"
            Toast.makeText(this, "Gagal memproses pembayaran", Toast.LENGTH_SHORT).show()
        }
    }

    // --- FUNGSI NOTIFIKASI YANG SUDAH DISINKRONKAN KE FOLDER USERS ---
    private fun kirimNotifikasiPembelian(judul: String) {
        if (userId != null) {
            val dbRef = FirebaseDatabase.getInstance(databaseUrl)
                .getReference("users")
                .child(userId)
                .child("notifikasi")

            val notifId = dbRef.push().key ?: return
            val dataNotif = mapOf(
                "id" to notifId,
                "judul" to "Pembelian Berhasil",
                "pesan" to "Selamat! Buku '$judul' sekarang sudah bisa kamu baca di Koleksi.",
                "waktu" to System.currentTimeMillis(),
                "type" to "BELI"
            )
            dbRef.child(notifId).setValue(dataNotif)
        }
    }

    private fun setupPaymentMethods() {
        binding.btnMethodGopay.setOnClickListener { selectMainMethod("Gopay"); hideBankList() }
        binding.btnMethodDana.setOnClickListener { selectMainMethod("DANA"); hideBankList() }
        binding.btnMethodBank.setOnClickListener { selectMainMethod("Transfer Bank"); toggleBankList() }

        binding.btnBankBRI.setOnClickListener { selectBank("Bank BRI") }
        binding.btnBankBNI.setOnClickListener { selectBank("Bank BNI") }
        binding.btnBankMandiri.setOnClickListener { selectBank("Bank Mandiri") }
    }

    private fun selectMainMethod(method: String) {
        metodeUtama = method
        bankTerpilih = ""
        binding.btnMethodGopay.setBackgroundResource(R.drawable.payment_method_normal)
        binding.btnMethodDana.setBackgroundResource(R.drawable.payment_method_normal)
        binding.btnMethodBank.setBackgroundResource(R.drawable.payment_method_normal)
        binding.rbGopay.isChecked = (method == "Gopay")
        binding.rbDana.isChecked = (method == "DANA")
        when (method) {
            "Gopay" -> binding.btnMethodGopay.setBackgroundResource(R.drawable.payment_method_selected)
            "DANA" -> binding.btnMethodDana.setBackgroundResource(R.drawable.payment_method_selected)
            "Transfer Bank" -> binding.btnMethodBank.setBackgroundResource(R.drawable.payment_method_selected)
        }
    }

    private fun selectBank(bank: String) {
        bankTerpilih = bank
        binding.rbBRI.isChecked = (bank == "Bank BRI")
        binding.rbBNI.isChecked = (bank == "Bank BNI")
        binding.rbMandiri.isChecked = (bank == "Bank Mandiri")
    }

    private fun toggleBankList() {
        if (binding.layoutBankList.visibility == View.GONE) {
            binding.layoutBankList.visibility = View.VISIBLE
            binding.ivChevronBank.setImageResource(android.R.drawable.arrow_up_float)
        } else { hideBankList() }
    }

    private fun hideBankList() {
        binding.layoutBankList.visibility = View.GONE
        binding.ivChevronBank.setImageResource(android.R.drawable.arrow_down_float)
    }

    private fun validateSelection(): Boolean {
        if (metodeUtama.isEmpty()) {
            Toast.makeText(this, "Pilih metode pembayaran!", Toast.LENGTH_SHORT).show()
            return false
        }
        if (metodeUtama == "Transfer Bank" && bankTerpilih.isEmpty()) {
            Toast.makeText(this, "Pilih bank tujuan!", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    private fun hitungTotal(hargaStr: String) {
        try {
            val cleanPrice = hargaStr.replace(Regex("[^0-9]"), "").toLongOrNull() ?: 0L
            val total = cleanPrice + 2000L
            val formatRupiah = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
            binding.tvTotalPrice.text = formatRupiah.format(total).replace("Rp", "Rp ")
        } catch (e: Exception) { binding.tvTotalPrice.text = hargaStr }
    }
}