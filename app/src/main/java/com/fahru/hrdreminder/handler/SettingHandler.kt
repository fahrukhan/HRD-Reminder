package com.fahru.hrdreminder.handler

import com.fahru.hrdreminder.model.SettingModel

class SettingHandler: BaseHandler() {
    private val noteSettingRef = db.collection("note").document(auth.uid.toString())
        .collection("setting").document("note")

    private val secondInDay: Long = 86400
    private val secondInWeek: Long= 604800
    private val secondInMonth: Long = 2592000

    private val day = "DAY"
    private val week = "WEEK"
    private val month = "MONTH"

    fun loadSetting(onSettingCallback: SettingCallback){
        noteSettingRef.get().addOnSuccessListener { doc ->
            if (doc.exists()){
                val unit = doc["unit"] as String
                val urgent = doc["urgent"] as Long
                val warning = doc["warning"] as Long

                onSettingCallback.onSetting(1, SettingModel(unit, urgent, warning))
            }else{
                onSettingCallback.onSetting(0, getDefaultSetting())
            }
        }.addOnFailureListener { _ ->
            onSettingCallback.onSetting(0, getDefaultSetting())
        }
    }

    fun getMultiplierWhenUnit(unit: String): Long{
        return when(unit){
            day -> secondInDay
            week -> secondInWeek
            month -> secondInMonth
            else -> secondInDay
        }
    }

    fun saveSetting(settings: SettingModel, actionCallback: ActionCallback){
        noteSettingRef.set(settings).addOnSuccessListener {
            actionCallback.onActionFinish(1, "Perubahan diterapkan")
        }.addOnFailureListener {
            actionCallback.onActionFinish(0, "Perubahan GAGAL disimpan!")
        }
    }

    fun getDefaultSetting(): SettingModel {
        return SettingModel(day, 3, 6)
    }
}