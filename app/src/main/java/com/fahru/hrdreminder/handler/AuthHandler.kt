package com.fahru.hrdreminder.handler

import android.util.Log
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest

class AuthHandler : BaseHandler() {
    private val user = LogHandler()

    fun registerWithEmailPassword(
        email: String,
        pass: String,
        name: String,
        actionCallback: ActionCallback,
    ) {
        auth.createUserWithEmailAndPassword(email, pass).addOnCompleteListener { task ->
            if (task.isSuccessful) {

                val profileUpdates = UserProfileChangeRequest.Builder()
                    .setDisplayName(name)
                    .build()
                auth.currentUser?.updateProfile(profileUpdates)
                    ?.addOnCompleteListener { task2 ->
                        if (task2.isSuccessful) {
                            user.addUser(0)
                            auth.currentUser?.sendEmailVerification()
                                ?.addOnCompleteListener { task3 ->
                                    if (task3.isSuccessful) {
                                        actionCallback.onActionFinish(1, "Email verifikasi telah dikirim, mohon cek kotak masuk email anda!")
                                    }
                                }?.addOnFailureListener { e ->
                                    actionCallback.onActionFinish(0, "Gagal mengirim email verifikasi\nError: ${e.message.toString()}")
                            }
                        }
                        Log.d("Update Profile", "Display Name Updated")
                    }
                    ?.addOnFailureListener {
                        Log.d("Update Profile", "Failed set Display Name")
                    }
            }
        }.addOnFailureListener { e ->
            actionCallback.onActionFinish(0, "Gagal melakukan registrasi\nError: ${e.message.toString()}")
        }
    }

    //Google button
    fun signInWithCredential(credential: AuthCredential, actionCallback: ActionCallback) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    user.addUser(1)
                    actionCallback.onActionFinish(success, "SignIn With Google Success!")
                }
            }
            .addOnFailureListener { e ->
                actionCallback.onActionFinish(failed, e.toString())
            }
    }

    fun loginWithEmailPassword(email: String, pass: String, actionCallback: ActionCallback) {
        auth.signInWithEmailAndPassword(email, pass)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    user.addLoginLog()
                    actionCallback.onActionFinish(success, "Login success!")
                }
            }.addOnFailureListener { e ->
                actionCallback.onActionFinish(failed, e.message.toString())
            }
    }



    //Authentication LOG
    fun loginToLog(){

    }

    fun getInstance(): FirebaseAuth {
        return auth
    }

    fun currentUser(): FirebaseUser? {
        return auth.currentUser
    }

    fun userId(): String {
        return auth.currentUser?.uid.toString()
    }

    fun isEmailVerified(): Boolean {
        return auth.currentUser?.isEmailVerified ?: false
    }

    fun uid(): String {
        return auth.uid.toString()
    }

    fun getName(): String {
        return auth.currentUser?.displayName.toString()
    }

    fun forgotPassword(email: String, actionCallback: ActionCallback) {
        auth.sendPasswordResetEmail(email).addOnCompleteListener {task ->
            if (task.isSuccessful){
                actionCallback.onActionFinish(1, "Email reset password telah dikirim!")
            }else{
                actionCallback.onActionFinish(0, "Gagal mengirim link, hubungi developer")
            }
        }.addOnFailureListener { e ->
            actionCallback.onActionFinish(0, e.message.toString())
        }
    }

}