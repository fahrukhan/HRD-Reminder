package com.fahru.hrdreminder.model

import com.google.firebase.Timestamp

data class NoteModel(
    var id: String,
    var author_id: String,
    var title: String,
    var note: String,
    var deadline: Timestamp,
    var history: Map<String, Map<String, String>>
)