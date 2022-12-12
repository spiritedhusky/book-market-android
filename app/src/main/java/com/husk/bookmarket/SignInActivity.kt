

package com.husk.bookmarket

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract
import com.firebase.ui.auth.KickoffActivity.createIntent
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult
import com.firebase.ui.auth.ui.email.EmailActivity.createIntent
import com.firebase.ui.auth.ui.email.WelcomeBackEmailLinkPrompt.createIntent
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase

class SignInActivity : AppCompatActivity() {
    private var usersRef = FirebaseFirestore.getInstance().collection("users")

    private val signInLauncher = registerForActivityResult(
        FirebaseAuthUIActivityResultContract()
    ) { res ->
        this.onSignInResult(res)
    }

    private fun startSignIn() {
        // Choose authentication providers
        val providers = arrayListOf(
            AuthUI.IdpConfig.EmailBuilder().build(),
            AuthUI.IdpConfig.GoogleBuilder().build()
        )

        // Create and launch sign-in intent
        val signInIntent = AuthUI.getInstance()
            .createSignInIntentBuilder()
            .setAvailableProviders(providers)
            .setIsSmartLockEnabled(false)
            .build()
        signInLauncher.launch(signInIntent)
    }

    private fun registerUser(user: FirebaseUser) {
        usersRef.whereEqualTo("uid", user.uid).get().addOnCompleteListener {
            if (it.isSuccessful) {
                if (it.result.size() == 0) { // user doesn't exist yet
                    var newUser = mapOf(
                        "uid" to user.uid,
                        "name" to user.displayName,
                        "photo" to user.photoUrl,
                        "email" to user.email
                    )
                    usersRef.add(newUser).addOnCompleteListener { task ->
                        if (!task.isSuccessful) {
                            Toast.makeText(this, "Failed to register user: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                            registerUser(user) // retry
                        }
                    }
                }
            } else {
                Toast.makeText(this, "Failed to retrieve users: ${it.exception?.message}", Toast.LENGTH_LONG).show()
                registerUser(user) // retry
            }
        }
    }

    private fun onSignInResult(result: FirebaseAuthUIAuthenticationResult) {
        val response = result.idpResponse
        Log.e("Sign in", "Sign in complete")
        if (result.resultCode == AppCompatActivity.RESULT_OK) {
            // Successfully signed in
            val user = FirebaseAuth.getInstance().currentUser!!
            Log.d("Sign in", "Successfully logged in user ${user.displayName}")
            registerUser(user)
            startActivity(Intent(this, MainActivity::class.java));
            finish();
        } else {
            Snackbar.make(
                findViewById(android.R.id.content),
                "Sign In Failed",
                Snackbar.LENGTH_SHORT
            ).show()
            // just try to sign in again, for now
            startSignIn()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        if (Firebase.auth.currentUser != null){
            Log.e("Sign In", "current user: ${Firebase.auth.currentUser?.displayName}")
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }

        setContentView(R.layout.activity_sign_in)
        startSignIn()
    }
}