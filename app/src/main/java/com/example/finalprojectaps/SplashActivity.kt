package com.example.finalprojectaps

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import com.example.finalprojectaps.databinding.ActivitySplashBinding
import com.google.firebase.auth.FirebaseAuth

class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        val animation = AnimationUtils.loadAnimation(this, R.anim.logo_animation)
        binding.logo.startAnimation(animation)

        Handler().postDelayed({

            val currentUser = auth.currentUser
            if (currentUser != null) {
                startActivity(Intent(this, MainActivity::class.java))
            } else {
                startActivity(Intent(this, SignUpActivity::class.java))
            }
            finish()
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out)

        }, 3000)
    }
}