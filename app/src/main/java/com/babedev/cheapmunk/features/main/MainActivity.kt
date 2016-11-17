package com.babedev.cheapmunk.features.main

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import com.babedev.cheapmunk.R
import com.babedev.cheapmunk.app.MyApp
import com.babedev.cheapmunk.features.feed.FeedActivity
import com.facebook.AccessToken
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginResult
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    val mAuth: FirebaseAuth = FirebaseAuth.getInstance()

    lateinit var mAuthListener: FirebaseAuth.AuthStateListener
    lateinit var mCallbackManager: CallbackManager

    var listActivityCalled = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mAuthListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser

            if (user != null) {
                if (!listActivityCalled) {
                    listActivityCalled = true
                    MyApp.me.firebaseId = user.uid

                    startActivity(Intent(this, FeedActivity::class.java))
                    finish()
                }
            }
        }

        mCallbackManager = CallbackManager.Factory.create()

        btn_fb.setReadPermissions("email", "public_profile")
        btn_fb.registerCallback(mCallbackManager, object : FacebookCallback<LoginResult> {
            override fun onCancel() {
                // Do nothing
            }

            override fun onError(error: FacebookException?) {
                // Do nothing
            }

            override fun onSuccess(result: LoginResult) {
                handleFacebookAccessToken(result.accessToken)
            }
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        mCallbackManager.onActivityResult(requestCode, resultCode, data)
    }

    override fun onStart() {
        super.onStart()
        mAuth.addAuthStateListener(mAuthListener)
    }

    override fun onStop() {
        super.onStop()
        mAuth.removeAuthStateListener(mAuthListener)
    }

    private fun handleFacebookAccessToken(token: AccessToken) {
        val credential = FacebookAuthProvider.getCredential(token.token)

        mAuth.signInWithCredential(credential).addOnCompleteListener(this) { task ->
            if (!task.isSuccessful) {
                Toast.makeText(this@MainActivity, "Authentication failed. ${task.exception}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
