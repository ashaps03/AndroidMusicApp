package com.example.finalprojectaps


import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.firebase.ui.auth.AuthMethodPickerLayout
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.auth.FirebaseAuth
import com.koushikdutta.ion.builder.Builders.Any.M


class SignUpActivity2 : AppCompatActivity() {

    private val TAG = "RegisterActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        val playButton = findViewById<Button>(R.id.play_button)

        playButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        val currentUser = FirebaseAuth.getInstance().currentUser

        if (currentUser != null) {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        } else {
            val signActivityLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {

                    val user = FirebaseAuth.getInstance().currentUser
                    Log.d(TAG, "onActivityResult: $user")

                    if (user?.metadata?.creationTimestamp == user?.metadata?.lastSignInTimestamp) {
                        Toast.makeText(this, "Welcome New User!", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "Welcome Back!", Toast.LENGTH_SHORT).show()
                    }

                    startActivity(Intent(this, menu::class.java))
                    finish()

                } else {
                    val response = IdpResponse.fromResultIntent(result.data)
                    if (response == null) {
                        Log.d(TAG, "onActivityResult: the user has cancelled the sign in request")
                    } else {
                        Log.e(TAG, "onActivityResult: ${response.error?.errorCode}")
                    }
                }
            }

            findViewById<Button>(R.id.login_button).setOnClickListener {
                // Choose authentication providers -- make sure enable them on your firebase account first
                val providers = arrayListOf(
                    AuthUI.IdpConfig.EmailBuilder().build(),
                    AuthUI.IdpConfig.GoogleBuilder().build()
                )

                val signInIntent = AuthUI.getInstance()
                    .createSignInIntentBuilder()
                    .setAvailableProviders(providers)
                    .setTosAndPrivacyPolicyUrls("https://example.com", "https://example.com")
                    .setAlwaysShowSignInMethodScreen(true)
                    .setIsSmartLockEnabled(false)
                    .setTheme(R.style.blueColor)
                    .setLogo(R.drawable.guessthatsong)
                    .build()
                signActivityLauncher.launch(signInIntent)
            }
        }
    }


}