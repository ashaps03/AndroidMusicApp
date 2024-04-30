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
    private lateinit var audioPlayer: AudioPlayer

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //sound initiation
        audioPlayer = AudioPlayer.getInstance(this)

        //firebase auth initiation
        auth = FirebaseAuth.getInstance()

        //animation will play then transtion into the SignUpActivity2
        val animation = AnimationUtils.loadAnimation(this, R.anim.logo_animation)
        binding.logo.startAnimation(animation)

        Handler().postDelayed({

            val currentUser = auth.currentUser
            if (currentUser != null) {
                startActivity(Intent(this, MainActivity::class.java))
            } else {
                startActivity(Intent(this, SignUpActivity2::class.java))
            }
            finish()
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out)

        }, 3000)
    }
}