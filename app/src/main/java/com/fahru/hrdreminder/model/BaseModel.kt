package com.fahru.hrdreminder.model

import android.app.Activity
import android.content.Context
import android.graphics.Typeface
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import es.dmoral.toasty.Toasty

abstract class BaseModel: AppCompatActivity(){

    fun toastSuccess(activity: Activity, msg: String){
        Toasty.success(activity, msg, Toasty.LENGTH_SHORT, true).show()
    }

    fun toastWarning(activity: Activity, msg: String){
        Toasty.warning(activity, msg, Toasty.LENGTH_SHORT, true).show()
    }

    fun toastError(activity: Activity, msg: String){
        Toasty.error(activity, msg, Toasty.LENGTH_LONG, true).show()
    }

    fun toastNormal(activity: Activity, msg: String){
        Toasty.info(activity, msg, Toasty.LENGTH_SHORT, true).show()
    }

    fun hideSoftKeyboard(activity: Activity){
        val view: View? = activity.currentFocus
        val imm =
            (activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?)!!
        imm.hideSoftInputFromWindow(view?.windowToken, 0)
    }

    fun faceHymen(activity: Activity): Typeface {
        return Typeface.createFromAsset(activity.assets, "hymen.otf")
    }
}