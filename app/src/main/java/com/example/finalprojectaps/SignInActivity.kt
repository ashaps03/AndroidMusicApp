package com.example.finalprojectaps

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import com.firebase.ui.auth.AuthUI

class CustomSignInActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)

        // Find views
        val signInButton = findViewById<Button>(R.id.signInButton)

        // Set click listener for sign-in button
        signInButton.setOnClickListener {
            // Start sign-in flow
            startSignInFlow()
        }
    }

    private fun startSignInFlow() {
        // Example: Redirect to FirebaseUI sign-in activity
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
            .build()

        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    // Handle sign-in result if needed
    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            // Handle sign-in result here if needed
        }
    }

    companion object {
        private const val RC_SIGN_IN = 123
    }
}
