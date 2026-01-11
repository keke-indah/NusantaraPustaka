package com.example.nusantarapustaka.ui

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen // Pastikan import ini ada
import com.example.nusantarapustaka.databinding.ActivitySplashBinding

class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        // 1. PINDAHKAN KE SINI (Paling atas sebelum super.onCreate)
        // Ini kunci agar splash bawaan Android tidak error atau tabrakan
        val splashScreen = installSplashScreen()

        super.onCreate(savedInstanceState)

        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Sembunyikan ActionBar agar full screen
        supportActionBar?.hide()

        // --- LOGIKA ANIMASI LOGO ---
        binding.logoIcon.alpha = 0f
        binding.logoIcon.translationY = 50f

        binding.logoIcon.animate()
            .alpha(1f)
            .translationY(0f)
            .setDuration(1500)
            .start()

        // --- PINDAH KE LOGIN ACTIVITY ---
        Handler(Looper.getMainLooper()).postDelayed({
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)

            // Transisi halus
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)

            finish()
        }, 3000)
    }
}