package com.example.finalprojectaps

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth

class SignInActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var loginEmail: EditText
    private lateinit var loginPass: EditText
    private lateinit var signUpRedirect: TextView
    private lateinit var loginButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)

        auth = FirebaseAuth.getInstance()
        loginEmail = findViewById(R.id.signin_email)
        loginPass = findViewById(R.id.signin_password)
        signUpRedirect = findViewById(R.id.text_loginn)
        loginButton = findViewById(R.id.signin_button)

        loginButton.setOnClickListener {
            val email = loginEmail.text.toString().trim()
            val password = loginPass.text.toString().trim()

            if (email.isEmpty()) {
                loginEmail.error = "Email cannot be empty"
                return@setOnClickListener
            }
            if (password.isEmpty()) {
                loginPass.error = "Password cannot be empty"
                return@setOnClickListener
            }

            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(TAG, "signInWithEmail:success")
                        val user = auth.currentUser
                        updateUI(user)
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w(TAG, "signInWithEmail:failure", task.exception)
                        Toast.makeText(baseContext, "Authentication failed.",
                            Toast.LENGTH_SHORT).show()
                        updateUI(null)
                    }
                }
        }

        signUpRedirect.setOnClickListener {
            startActivity(Intent(this@SignInActivity, SignUpActivity2::class.java))
        }
    }

    private fun updateUI(user: com.google.firebase.auth.FirebaseUser?) {
        if (user != null) {
            startActivity(Intent(this@SignInActivity, MainActivity::class.java))
            finish()
        }
    }

    companion object {
        private const val TAG = "SignInActivity"
    }
}
