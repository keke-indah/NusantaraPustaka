package com.example.nusantarapustaka.ui

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.view.MotionEvent
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.nusantarapustaka.R
import com.example.nusantarapustaka.databinding.ActivitySecurityBinding
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class SecurityActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySecurityBinding
    private val auth = FirebaseAuth.getInstance()
    private val databaseUrl = "https://nusantarapustaka-f5363-default-rtdb.asia-southeast1.firebasedatabase.app/"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySecurityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        // --- 1. INISIALISASI FITUR MATA PASSWORD ---
        setupPasswordVisibility(binding.etOldPassword)
        setupPasswordVisibility(binding.etNewPassword)
        setupPasswordVisibility(binding.etConfirmPassword)

        binding.btnBack.setOnClickListener { finish() }

        binding.btnUpdatePassword.setOnClickListener {
            val oldPass = binding.etOldPassword.text.toString()
            val newPass = binding.etNewPassword.text.toString()
            val confirmPass = binding.etConfirmPassword.text.toString()

            if (oldPass.isEmpty() || newPass.isEmpty() || confirmPass.isEmpty()) {
                Toast.makeText(this, "Semua kolom harus diisi", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (newPass.length < 6) {
                Toast.makeText(this, "Password minimal 6 karakter", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (newPass != confirmPass) {
                Toast.makeText(this, "Konfirmasi password tidak cocok", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            prosesGantiPassword(oldPass, newPass)
        }
    }

    // --- 2. LOGIKA MATA BUKA TUTUP (PENTING) ---
    @SuppressLint("ClickableViewAccessibility")
    private fun setupPasswordVisibility(editText: EditText) {
        editText.setOnTouchListener { v, event ->
            val DRAWABLE_RIGHT = 2 // Index icon sebelah kanan

            if (event.action == MotionEvent.ACTION_UP) {
                // Cek apakah ketukan user mengenai icon di pojok kanan EditText
                if (event.rawX >= (editText.right - editText.compoundDrawables[DRAWABLE_RIGHT].bounds.width())) {

                    if (editText.transformationMethod is PasswordTransformationMethod) {
                        // BUKA MATA (Teks Terlihat)
                        editText.transformationMethod = HideReturnsTransformationMethod.getInstance()
                        editText.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_visibility_on, 0)
                    } else {
                        // TUTUP MATA (Teks Jadi Titik-titik)
                        editText.transformationMethod = PasswordTransformationMethod.getInstance()
                        editText.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_visibility_off, 0)
                    }

                    // Jaga kursor agar tetap di paling akhir kata
                    editText.setSelection(editText.text.length)
                    return@setOnTouchListener true
                }
            }
            false
        }
    }

    private fun prosesGantiPassword(oldPass: String, newPass: String) {
        val user = auth.currentUser
        val email = user?.email

        if (user != null && email != null) {
            binding.btnUpdatePassword.isEnabled = false
            binding.btnUpdatePassword.text = "Memverifikasi..."

            val credential = EmailAuthProvider.getCredential(email, oldPass)

            user.reauthenticate(credential).addOnCompleteListener { reAuthTask ->
                if (reAuthTask.isSuccessful) {
                    user.updatePassword(newPass).addOnCompleteListener { updateTask ->
                        if (updateTask.isSuccessful) {
                            kirimNotifKeamanan("Keamanan Akun", "Kata sandi Anda baru saja diperbarui.")
                            Toast.makeText(this, "Kata sandi berhasil diubah", Toast.LENGTH_SHORT).show()
                            finish()
                        } else {
                            binding.btnUpdatePassword.isEnabled = true
                            binding.btnUpdatePassword.text = "Simpan Perubahan Kata Sandi"
                            Toast.makeText(this, "Gagal update: ${updateTask.exception?.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    binding.btnUpdatePassword.isEnabled = true
                    binding.btnUpdatePassword.text = "Simpan Perubahan Kata Sandi"
                    Toast.makeText(this, "Kata sandi lama salah!", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun kirimNotifKeamanan(judul: String, pesan: String) {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            val dbRef = FirebaseDatabase.getInstance(databaseUrl)
                .getReference("users")
                .child(userId)
                .child("notifikasi")

            val notifId = dbRef.push().key ?: return
            val dataNotif = mapOf(
                "id" to notifId,
                "judul" to judul,
                "pesan" to pesan,
                "waktu" to System.currentTimeMillis(),
                "type" to "KEAMANAN"
            )
            dbRef.child(notifId).setValue(dataNotif)
        }
    }
}