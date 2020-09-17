package com.fahru.hrdreminder.handler

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import com.fahru.hrdreminder.Login
import com.fahru.hrdreminder.MainActivity
import com.fahru.hrdreminder.R
import com.fahru.hrdreminder.model.BaseModel
import com.google.firebase.auth.FirebaseAuth.AuthStateListener
import kotlinx.android.synthetic.main.auth_verification.*

class AuthVerification(): BaseModel(){
    private val auth = AuthHandler()
    private var doubleBackPressed = false

    override fun onStart() {
        super.onStart()
        authStateListener.onAuthStateChanged(auth.getInstance())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.auth_verification)
    }

    private val authStateListener = AuthStateListener { firebaseAuth ->
        firebaseAuth.currentUser?.reload()
        if (firebaseAuth.currentUser != null){
            when(auth.isEmailVerified()){
                true -> startActivity(Intent(this, MainActivity::class.java))
                false -> {
//                    auth.signOut().also {
//                        val intent = Intent(this, Login::class.java)
//                        intent.putExtra("emailVerification", false)
//                        startActivity(intent)
//                    }
                    auth_verification_anim.speed = 1.2f
                    auth_verification_wait.text = getString(R.string.email_not_verified_yet)
                }
            }
        }else{
            startActivity(Intent(this, Login::class.java))
        }
    }

    override fun onBackPressed() {
        if (doubleBackPressed){
            super.onBackPressed()
            moveTaskToBack(true)
            return
        }

        auth.currentUser()?.reload()
        this.doubleBackPressed = true
        toastNormal(this, getString(R.string.double_back_pressed_warning))
        Handler().postDelayed(Runnable { doubleBackPressed = false }, 2000)
    }

    override fun onRestart() {
        super.onRestart()
        auth.currentUser()?.reload()
    }
}