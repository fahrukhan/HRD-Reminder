package com.fahru.hrdreminder.handler

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import java.text.SimpleDateFormat
import java.util.*

class LogHandler(): BaseHandler() {
    private val formatter = SimpleDateFormat("dd-MMM-yyyy HH:mm", Locale.getDefault())
    private val logInTime =  Timestamp.now().toDate()
    private val time = formatter.format(logInTime).toString()
    private val loginTime = "login - $time"
    private val registerTime = "register - $time"



    fun addUser(code: Int){
        val user = mapOf<String, Any>(
            "id" to auth.uid.toString(),
            "email" to auth.currentUser?.email.toString(),
            "name" to auth.currentUser?.displayName.toString(),
            "time" to FieldValue.serverTimestamp()
        )
        when(code){
            0 -> addUserByEmail(user)
            1 -> addUserByGoogle(user)
        }
    }

    private fun addUserByGoogle(user: Map<String, Any>){
        user["id"].let {
            db.collection("note").document(user["id"] as String).get().addOnSuccessListener { doc ->
                if (doc.contains("id")){
                    addLoginLog()
                } else {
                    db.collection("note").document(user["id"] as String).set(user).addOnSuccessListener {
                        db.collection("note").document(user["id"] as String).update(mapOf("google" to true))
                        addRegisterLog(user["id"] as String)
                    }
                }
            }
        }
    }

    private fun addUserByEmail(user: Map<String, Any>){
        user["id"]?.let {
            db.collection("note").document(it as String).set(user).addOnSuccessListener { _ ->
                addRegisterLog(it)
            }
        }
    }

    private fun addRegisterLog(id: String){
        id.let {
            db.collection("note").document(it).collection("log").document(registerTime).set(deviceData)
        }
    }

    fun addLoginLog(){
        db.collection("note").document(auth.uid.toString()).collection("log").document(loginTime).set(deviceData)
    }
}