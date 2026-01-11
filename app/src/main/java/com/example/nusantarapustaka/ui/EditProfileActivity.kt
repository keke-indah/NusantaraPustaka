package com.example.nusantarapustaka.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.nusantarapustaka.databinding.ActivityEditProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class EditProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditProfileBinding
    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private val databaseUrl = "https://nusantarapustaka-f5363-default-rtdb.asia-southeast1.firebasedatabase.app/"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityEditProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        val userId = auth.currentUser?.uid

        if (userId != null) {
            database = FirebaseDatabase.getInstance(databaseUrl)
                .getReference("users").child(userId)

            loadUserData()
        }

        binding.btnBack.setOnClickListener { finish() }

        binding.btnSaveProfile.setOnClickListener {
            saveProfileChanges()
        }

        binding.btnDeleteAccount.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Hapus Akun")
            builder.setMessage("Apakah Anda yakin ingin menghapus akun ini secara permanen? Tindakan ini tidak dapat dibatalkan.")

            builder.setPositiveButton("Ya, Hapus") { _, _ ->
                eksekusiHapusAkun()
            }

            builder.setNegativeButton("Batal") { dialog, _ ->
                dialog.dismiss()
            }

            val dialog = builder.create()
            dialog.show()
        }
    }

    private fun loadUserData() {
        database.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val name = snapshot.child("name").value.toString()
                    val email = auth.currentUser?.email

                    binding.etEditName.setText(name)
                    binding.etEditEmail.setText(email)
                }
            }
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@EditProfileActivity, "Gagal mengambil data", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun saveProfileChanges() {
        val newName = binding.etEditName.text.toString().trim()

        if (newName.isEmpty()) {
            binding.etEditName.error = "Nama tidak boleh kosong"
            return
        }

        database.child("name").setValue(newName)
            .addOnSuccessListener {
                // KIRIM NOTIFIKASI DENGAN TIPE PROFIL
                kirimNotifProfil("Update Profil", "Nama kamu berhasil diubah menjadi $newName")

                Toast.makeText(this, "Profil berhasil diperbarui!", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Gagal memperbarui profil", Toast.LENGTH_SHORT).show()
            }
    }

    // --- FUNGSI NOTIFIKASI YANG SUDAH DISINKRONKAN ---
    private fun kirimNotifProfil(judul: String, pesan: String) {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            val dbRef = FirebaseDatabase.getInstance(databaseUrl)
                .getReference("users")      // 1. Masuk ke folder users
                .child(userId)             // 2. Masuk ke UID
                .child("notifikasi")       // 3. Masuk ke folder notifikasi di dalam user

            val notifId = dbRef.push().key ?: return
            val dataNotif = mapOf(
                "id" to notifId,
                "judul" to judul,
                "pesan" to pesan,
                "waktu" to System.currentTimeMillis(),
                "type" to "PROFIL"
            )
            dbRef.child(notifId).setValue(dataNotif)
        }
    }

    private fun eksekusiHapusAkun() {
        val user = auth.currentUser
        val userId = auth.currentUser?.uid

        if (userId != null) {
            // Cukup hapus folder users/[userId]
            // Karena notifikasi sudah di dalam folder ini, otomatis ikut terhapus semua
            FirebaseDatabase.getInstance(databaseUrl).getReference("users").child(userId)
                .removeValue().addOnSuccessListener {

                    user?.delete()?.addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(this, "Akun berhasil dihapus", Toast.LENGTH_LONG).show()
                            val intent = Intent(this, LoginActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(intent)
                            finish()
                        } else {
                            // Jika token expired, minta user re-authenticate/login ulang
                            Toast.makeText(this, "Silakan login ulang sebelum menghapus akun demi keamanan.", Toast.LENGTH_LONG).show()
                            auth.signOut()
                            finish()
                        }
                    }
                }
        }
    }
}