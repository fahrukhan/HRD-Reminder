package com.fahru.hrdreminder.model

data class SettingModel(
    var unit: String,
    var urgent: Long,
    var warning: Long
)