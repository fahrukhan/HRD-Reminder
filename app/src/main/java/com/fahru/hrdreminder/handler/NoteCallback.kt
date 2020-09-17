package com.fahru.hrdreminder.handler

import com.fahru.hrdreminder.model.NoteModel

interface NoteCallback {
    fun onDataNote(code: Int, note: ArrayList<NoteModel>)
}