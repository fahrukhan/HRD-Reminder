package com.fahru.hrdreminder.handler

import com.fahru.hrdreminder.model.SettingModel

interface SettingCallback {
    fun onSetting(code: Int, setting: SettingModel)
}