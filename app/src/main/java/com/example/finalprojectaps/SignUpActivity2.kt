package com.example.finalprojectaps


import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.auth.FirebaseAuth


class SignUpActivity2 : AppCompatActivity() {

    private val TAG = "RegisterActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        // Get instance of the FirebaseAuth
        val currentUser = FirebaseAuth.getInstance().currentUser


        // If currentUser is not null, we have a user and go back to the MainActivity
        if (currentUser != null) {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            // Make sure to call finish(), otherwise the user would be able to go back to the RegisterActivity
            finish()
        } else {
            // create a new ActivityResultLauncher to launch the sign-in activity and handle the result
            // When the result is returned, the result parameter will contain the data and resultCode (e.g., OK, Cancelled etc.).
            val signActivityLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    // The user has successfully signed in or he/she is a new user

                    val user = FirebaseAuth.getInstance().currentUser
                    Log.d(TAG, "onActivityResult: $user")

                    //Checking for User (New/Old) (optional--you do not have to show these toast messages)
                    if (user?.metadata?.creationTimestamp == user?.metadata?.lastSignInTimestamp) {
                        //This is a New User
                        Toast.makeText(this, "Welcome New User!", Toast.LENGTH_SHORT).show()
                    } else {
                        //This is a returning user
                        Toast.makeText(this, "Welcome Back!", Toast.LENGTH_SHORT).show()
                    }

                    // Since the user signed in, the user can go back to main activity
                    startActivity(Intent(this, MainActivity::class.java))
                    // Make sure to call finish(), otherwise the user would be able to go back to the RegisterActivity
                    finish()

                } else {
                    // Sign in failed. If response is null the user canceled the
                    // sign-in flow using the back button. Otherwise check
                    // response.getError().getErrorCode() and handle the error.
                    val response = IdpResponse.fromResultIntent(result.data)
                    if (response == null) {
                        Log.d(TAG, "onActivityResult: the user has cancelled the sign in request")
                    } else {
                        Log.e(TAG, "onActivityResult: ${response.error?.errorCode}")
                    }
                }
            }

            // Login Button
            findViewById<Button>(R.id.login_button).setOnClickListener {
                // Choose authentication providers -- make sure enable them on your firebase account first
                val providers = arrayListOf(
                    AuthUI.IdpConfig.EmailBuilder().build(),
                    AuthUI.IdpConfig.GoogleBuilder().build()
                    //AuthUI.IdpConfig.PhoneBuilder().build(),
                    //AuthUI.IdpConfig.FacebookBuilder().build(),
                    //AuthUI.IdpConfig.TwitterBuilder().build()
                )

                // Create  sign-in intent
                val signInIntent = AuthUI.getInstance()
                    .createSignInIntentBuilder()
                    .setAvailableProviders(providers)
                    .setTosAndPrivacyPolicyUrls("https://example.com", "https://example.com")
                    .setLogo(R.drawable.logo2)
                    .setAlwaysShowSignInMethodScreen(true) // use this if you have only one provider and really want the see the signin page
                    .setIsSmartLockEnabled(false)
                    .build()

                // Launch sign-in Activity with the sign-in intent above
                signActivityLauncher.launch(signInIntent)
            }
        }
    }


}