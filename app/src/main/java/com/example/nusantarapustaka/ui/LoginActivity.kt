package com.example.nusantarapustaka.ui

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.view.ViewGroup
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.nusantarapustaka.R
import com.example.nusantarapustaka.databinding.ActivityLoginBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    private var isPasswordVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.hide()
        auth = FirebaseAuth.getInstance()

        // --- KONFIGURASI GOOGLE SIGN IN ---
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("589785762880-pb8t7g8v7q5oqmdvv8r6nv27ovucfkiv.apps.googleusercontent.com")
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        // --- LOGIKA TOMBOL ---

        // 1. Fitur Lupa Password (Dialog)
        binding.tvForgotPassword.setOnClickListener {
            showForgotPasswordDialog()
        }

        // Show/Hide Password
        binding.ivShowPassword.setOnClickListener {
            if (isPasswordVisible) {
                binding.etPassword.transformationMethod = PasswordTransformationMethod.getInstance()
                binding.ivShowPassword.setImageResource(R.drawable.ic_visibility_off)
            } else {
                binding.etPassword.transformationMethod = HideReturnsTransformationMethod.getInstance()
                binding.ivShowPassword.setImageResource(R.drawable.ic_visibility_on)
            }
            isPasswordVisible = !isPasswordVisible
            binding.etPassword.setSelection(binding.etPassword.text.length)
        }

        // Login Manual
        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Email dan Password tidak boleh kosong", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, "Login Berhasil!", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this, MainActivity::class.java))
                        finish()
                    } else {
                        Toast.makeText(this, "Gagal: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                    }
                }
        }

        // Login Google
        binding.btnLoginGoogle.setOnClickListener {
            val signInIntent = googleSignInClient.signInIntent
            startActivityForResult(signInIntent, 100)
        }

        // Pindah ke Register
        binding.tvToRegister.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
    }

    // --- FUNGSI RESET PASSWORD (DIALOG) ---
    private fun showForgotPasswordDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Reset Password")
        builder.setMessage("Masukkan email akun Nusantara Pustaka kamu")

        val input = EditText(this)
        input.hint = "email@example.com"
        input.inputType = InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS

        val container = FrameLayout(this)
        val params = FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        params.setMargins(48, 20, 48, 0)
        input.layoutParams = params
        container.addView(input)
        builder.setView(container)

        builder.setPositiveButton("Kirim") { _, _ ->
            val email = input.text.toString().trim()
            if (email.isNotEmpty()) {
                auth.sendPasswordResetEmail(email)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(this, "Link reset sudah dikirim ke email kamu", Toast.LENGTH_LONG).show()
                        } else {
                            Toast.makeText(this, "Gagal: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
            } else {
                Toast.makeText(this, "Email tidak boleh kosong", Toast.LENGTH_SHORT).show()
            }
        }
        builder.setNegativeButton("Batal") { dialog, _ -> dialog.dismiss() }
        builder.show()
    }

    // --- FUNGSI GOOGLE LOGIN ---
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 100) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)!!
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                Toast.makeText(this, "Google sign in failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Selamat Datang!", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                } else {
                    Toast.makeText(this, "Autentikasi Gagal", Toast.LENGTH_SHORT).show()
                }
            }
    }
}