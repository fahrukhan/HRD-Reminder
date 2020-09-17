package com.fahru.hrdreminder

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.view.ContextThemeWrapper
import androidx.core.content.ContextCompat
import com.fahru.hrdreminder.handler.ActionCallback
import com.fahru.hrdreminder.handler.AuthHandler
import com.fahru.hrdreminder.handler.AuthVerification
import com.fahru.hrdreminder.model.BaseModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.android.synthetic.main.activity_login.*

class Login : BaseModel() {
    private val auth = AuthHandler()
    private val rcSignIn: Int = 1
    private lateinit var googleSignInClient: GoogleSignInClient
    private val authListener = FirebaseAuth.AuthStateListener { authListener ->
        val user = authListener.currentUser
        if (user != null) {
            startActivity(Intent(this, AuthVerification::class.java))
        }
    }

    override fun onStart() {
        super.onStart()
        authListener.onAuthStateChanged(auth.getInstance())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        checkIntentExtra()
        setButton()

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)
    }

    private fun checkIntentExtra() {
        if (!intent.getBooleanExtra("emailVerification", true)) {
            toastNormal(this, getString(R.string.email_verification_false))
        }
    }

    private fun setButton() {
        login_title.typeface = faceHymen(this)
        login_register_text.setOnClickListener {
            startActivity(Intent(this, Register::class.java))
        }
        login_google_button.setOnClickListener {
            signIn()
        }
        login_button.setOnClickListener {
            hideSoftKeyboard(this)
            beforeLogin()
        }
        login_forgot_pass.setOnClickListener {
            showForgotPassword()
        }
    }

    private fun showForgotPassword() {
        val alertDialog = AlertDialog.Builder(this)
        alertDialog.setTitle("Forgot Password")
        alertDialog.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_logo_app))

        val textInputLayout = TextInputLayout(ContextThemeWrapper(this, R.style.myInputLayout))
        val input = TextInputEditText(this)

        input.inputType = InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
        input.isSingleLine = true
        input.hint = "email"
        val height = 24
        val container = FrameLayout(this)
        val params = FrameLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
            height)
        params.setMargins(50, 50, 50, 50)
        textInputLayout.layoutParams = params
        textInputLayout.addView(input)
        container.addView(textInputLayout)

        alertDialog.setView(container)
        alertDialog.setPositiveButton("KIRIM") { _, _ ->
            val email = input.text.toString()
            if (email.isNotEmpty()) {
                auth.forgotPassword(email, object : ActionCallback {
                    override fun onActionFinish(code: Int, msg: String) {
                        when (code) {
                            1 -> toastSuccess(this@Login, msg)
                            0 -> toastError(this@Login, msg)
                        }
                    }
                })
            }
        }

        alertDialog.setNegativeButton("BATAL") { dialog, which ->
            dialog.cancel()
        }
        alertDialog.show()
    }

    //login with email and password
    private fun beforeLogin() {
        if (thisEmail().isEmpty()) {
            emptyField(login_email, 0)
        } else if (thisPass().isEmpty()) {
            emptyField(login_pass, 0)
        } else if (!Patterns.EMAIL_ADDRESS.matcher(thisEmail()).matches()) {
            emptyField(login_email, 1)
        } else {
            login()
        }
    }

    private fun emptyField(field: TextInputEditText, code: Int) {
        when (code) {
            0 -> field.error = getString(R.string.empty_field_error)
            1 -> field.error = getString(R.string.email_not_valid)
        }
        field.isTextInputLayoutFocusedRectEnabled = true
        field.isFocusable = true
    }

    private fun login() {
        loadAnim(true)
        auth.loginWithEmailPassword(thisEmail(), thisPass(), object : ActionCallback {
            override fun onActionFinish(code: Int, msg: String) {
                when (code) {
                    0 -> {
                        loadAnim(false)
                        toastError(this@Login, msg)
                    }
                    1 -> {
                        toastSuccess(this@Login, msg)
                        loadAnim(false)
                        startActivity(Intent(this@Login, AuthVerification::class.java))
                    }
                }
            }

        })
    }

    //Login with google button
    private fun signIn() {
        val signIntent = googleSignInClient.signInIntent
        startActivityForResult(signIntent, rcSignIn)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == rcSignIn) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                if (account != null) {

                    authWithGoogle(account)
                }
            } catch (e: ApiException) {
                Log.d("google login", "CANCEL")
            }
        }
    }

    private fun authWithGoogle(account: GoogleSignInAccount) {
        loadAnim(true)
        val credential: AuthCredential = GoogleAuthProvider.getCredential(account.idToken, null)
        auth.signInWithCredential(credential, object : ActionCallback {
            override fun onActionFinish(code: Int, msg: String) {
                when (code) {
                    0 -> {
                        loadAnim(false)
                        toastError(this@Login, msg)
                    }

                    1 -> {
                        loadAnim(false)
                        toastSuccess(this@Login, msg)
                        startActivity(Intent(this@Login, MainActivity::class.java))
                    }
                }
            }
        })

    }

    // Getter input text
    private fun thisEmail(): String {
        return login_email.text.toString()
    }

    private fun thisPass(): String {
        return login_pass.text.toString()
    }

    private fun loadAnim(show: Boolean) {
        if (show) login_anim.visibility = View.VISIBLE
        else login_anim.visibility = View.GONE
    }

    override fun onBackPressed() {
        super.onBackPressed()
        moveTaskToBack(true)
    }
}