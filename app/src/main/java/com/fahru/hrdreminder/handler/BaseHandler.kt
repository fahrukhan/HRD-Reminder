package com.fahru.hrdreminder.handler

import android.content.SharedPreferences
import android.os.Build
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

open class BaseHandler(){
    val auth = FirebaseAuth.getInstance()
    val db  = FirebaseFirestore.getInstance()
    lateinit var preference: SharedPreferences
    lateinit var editor: SharedPreferences.Editor

    val success: Int = 1
    val failed: Int = 0

    val deviceData = mapOf<String, String>(
        "api-level" to "${Build.VERSION.SDK_INT} - ${Build.VERSION.CODENAME}",
        "device" to "${Build.BRAND} - ${Build.DEVICE}",
        "model" to "${Build.MODEL} - ${Build.PRODUCT}",
    )


}