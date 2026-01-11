package com.example.nusantarapustaka.ui

import android.content.Intent
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.nusantarapustaka.R
import com.example.nusantarapustaka.databinding.ActivityRegisterBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var auth: FirebaseAuth
    private var isPassVisible = false
    private var isConfirmVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.hide()
        auth = FirebaseAuth.getInstance()

        binding.btnBack.setOnClickListener { finish() }
        binding.tvToLogin.setOnClickListener { finish() }

        binding.ivShowPasswordRegister.setOnClickListener {
            isPassVisible = !isPassVisible
            togglePassword(isPassVisible)
        }

        binding.ivShowConfirmPassword.setOnClickListener {
            isConfirmVisible = !isConfirmVisible
            toggleConfirmPassword(isConfirmVisible)
        }

        binding.btnRegister.setOnClickListener {
            val name = binding.etNameRegister.text.toString().trim()
            val email = binding.etEmailRegister.text.toString().trim()
            val password = binding.etPasswordRegister.text.toString().trim()
            val confirmPassword = binding.etConfirmPassword.text.toString().trim()

            if (name.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "Harap isi semua kolom!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        val userId = auth.currentUser?.uid
                        val database = FirebaseDatabase.getInstance("https://nusantarapustaka-f5363-default-rtdb.asia-southeast1.firebasedatabase.app/")
                            .getReference("users")

                        val userMap = mapOf(
                            "name" to name,
                            "email" to email
                        )

                        if (userId != null) {
                            database.child(userId).setValue(userMap)
                                .addOnSuccessListener {
                                    Toast.makeText(this, "Registrasi Berhasil!", Toast.LENGTH_SHORT).show()
                                    // Langsung pindah ke Main agar data terpanggil
                                    val intent = Intent(this, MainActivity::class.java)
                                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                    startActivity(intent)
                                }
                        }
                    } else {
                        Toast.makeText(this, "Gagal: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }

    private fun togglePassword(show: Boolean) {
        if (show) {
            binding.etPasswordRegister.transformationMethod = HideReturnsTransformationMethod.getInstance()
            binding.ivShowPasswordRegister.setImageResource(R.drawable.ic_visibility_on)
        } else {
            binding.etPasswordRegister.transformationMethod = PasswordTransformationMethod.getInstance()
            binding.ivShowPasswordRegister.setImageResource(R.drawable.ic_visibility_off)
        }
    }

    private fun toggleConfirmPassword(show: Boolean) {
        if (show) {
            binding.etConfirmPassword.transformationMethod = HideReturnsTransformationMethod.getInstance()
            binding.ivShowConfirmPassword.setImageResource(R.drawable.ic_visibility_on)
        } else {
            binding.etConfirmPassword.transformationMethod = PasswordTransformationMethod.getInstance()
            binding.ivShowConfirmPassword.setImageResource(R.drawable.ic_visibility_off)
        }
    }
}