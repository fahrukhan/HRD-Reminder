package com.fahru.hrdreminder.handler

interface ActionCallback {
    fun onActionFinish(code: Int, msg: String)
}