package com.example.finalprojectaps

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton

class menu : AppCompatActivity() {

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setContentView(R.layout.activity_menu)

            val buttonToPlay = findViewById<Button>(R.id.playButton)

            buttonToPlay.setOnClickListener {
                val intent = Intent(this@menu, MainActivity::class.java)
                startActivity(intent)
            }
        }
    }