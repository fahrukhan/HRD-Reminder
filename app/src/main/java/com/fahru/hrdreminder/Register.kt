package com.fahru.hrdreminder

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.View
import com.fahru.hrdreminder.handler.ActionCallback
import com.fahru.hrdreminder.handler.AuthHandler
import com.fahru.hrdreminder.handler.AuthVerification
import com.fahru.hrdreminder.model.BaseModel
import com.google.android.material.textfield.TextInputEditText
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.activity_register.*

class Register : BaseModel() {
    private val auth = AuthHandler()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        setButton()
    }

    private fun setButton(){
        register_title.typeface = faceHymen(this)
        register_button.setOnClickListener {
            hideSoftKeyboard(this)
            beforeRegister()
        }
    }

    //login with email and password
    private fun beforeRegister(){
        if (thisName().isEmpty()) {
            emptyField(register_name, 0)
        }else if (thisEmail().isEmpty()){
                emptyField(register_email, 0)
        }else if (thisPass().isEmpty()){
            emptyField(register_pass, 0)
        }else if (thisPass().length < 6){
            toastWarning(this, "Password minimum 6 character")
        }else if (!Patterns.EMAIL_ADDRESS.matcher(thisEmail()).matches()){
            emptyField(login_email, 1)
        }else{
            register()
        }
    }

    private fun register() {
        loadAnim(true)
        auth.registerWithEmailPassword(thisEmail(), thisPass(), thisName(), object : ActionCallback{
            override fun onActionFinish(code: Int, msg: String) {
                when(code){
                    0 -> {
                        loadAnim(false)
                        toastError(this@Register, msg)
                    }
                    1 -> {
                        loadAnim(false)
                        toastSuccess(this@Register, msg)
                        startActivity(Intent(this@Register, AuthVerification::class.java))
                    }
                }
            }
        })
    }

    private fun emptyField(field: TextInputEditText, code: Int){
        when(code){
            0 -> field.error = getString(R.string.empty_field_error)
            1 -> field.error = getString(R.string.email_not_valid)
        }
        field.isTextInputLayoutFocusedRectEnabled = true
        field.isFocusable = true
    }

    // Getter input text
    private fun thisEmail(): String{ return register_email.text.toString() }
    private fun thisPass(): String{ return register_pass.text.toString() }
    private fun thisName(): String{ return register_name.text.toString() }

    private fun loadAnim(show: Boolean) {
        if (show) reg_anim.visibility = View.VISIBLE
        else reg_anim.visibility = View.GONE
    }
}