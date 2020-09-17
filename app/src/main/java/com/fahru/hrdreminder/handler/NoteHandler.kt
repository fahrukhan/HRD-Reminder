package com.fahru.hrdreminder.handler

import com.fahru.hrdreminder.model.NoteModel
import com.google.firebase.Timestamp
import com.google.firebase.firestore.Query

open class NoteHandler : BaseHandler() {
    private val noteRef = db.collection("note")
        .document(auth.uid.toString()).collection("note")

    fun addNote(note: NoteModel, actionCallback: ActionCallback) {
        noteRef.add(note).addOnSuccessListener { doc ->
            noteRef.document(doc.id).update(mapOf("id" to doc.id))
                .addOnSuccessListener { _ ->
                    actionCallback.onActionFinish(success, "Note Saved!")
                }
        }.addOnFailureListener { e ->
            actionCallback.onActionFinish(failed, "Error: ${e.message.toString()}")
        }
    }

    fun updateNote(id: String, note: Map<String, Any>, actionCallback: ActionCallback) {
        noteRef.document(id).update(note).addOnSuccessListener { doc ->
             actionCallback.onActionFinish(success, "Note Diperbarui!")
        }.addOnFailureListener { e ->
            actionCallback.onActionFinish(failed, "Error: ${e.message.toString()}")
        }
    }

    fun getNote(id: String, onNoteCallback: NoteCallback){
        val noteList = arrayListOf<NoteModel>()
        noteRef.document(id).get().addOnSuccessListener { note ->
            if (note.exists()){
                val noteId = note.id
                val authorId = note["author_id"] as String
                val title = note["title"] as String
                val noteMain = note["note"] as String
                val deadline = note["deadline"] as Timestamp
                val history = note["history"] as Map<String, Map<String, String>>
                noteList.add(NoteModel(noteId, authorId, title, noteMain, deadline, history))
            }
            onNoteCallback.onDataNote(success, noteList)
        }
    }

    fun getNotes(onNoteCallback: NoteCallback){
        val noteList = arrayListOf<NoteModel>()
        noteRef.orderBy("deadline", Query.Direction.ASCENDING)
            .whereGreaterThanOrEqualTo("deadline", Timestamp.now())
            .get().addOnSuccessListener { doc ->
            val notes = doc.documents
            for (note in notes){
                val id = note.id
                val authorId = note["author_id"] as String
                val title = note["title"] as String
                val noteMain = note["note"] as String
                val deadline = note["deadline"] as Timestamp
                val history = note["history"] as Map<String, Map<String, String>>

                noteList.add(NoteModel(id, authorId, title, noteMain, deadline, history))
            }
            onNoteCallback.onDataNote(success, noteList)
        }.addOnFailureListener {e ->
            noteList.add(NoteModel(e.message.toString(), "", "", "", Timestamp.now(), mapOf("" to mapOf("" to ""))))
            onNoteCallback.onDataNote(failed, noteList)
        }
    }

    fun getExpiredNotes(onNoteCallback: NoteCallback){
        val noteList = arrayListOf<NoteModel>()
        noteRef.orderBy("deadline", Query.Direction.ASCENDING).whereLessThan("deadline", Timestamp.now())
            .get().addOnSuccessListener { doc ->
            val notes = doc.documents
            for (note in notes){
                val id = note.id
                val authorId = note["author_id"] as String
                val title = note["title"] as String
                val noteMain = note["note"] as String
                val deadline = note["deadline"] as Timestamp
                val history = note["history"] as Map<String, Map<String, String>>

                noteList.add(NoteModel(id, authorId, title, noteMain, deadline, history))
            }
            onNoteCallback.onDataNote(success, noteList)
        }.addOnFailureListener {e ->
            noteList.add(NoteModel("Gagal memuat expired note\nError: ${e.message.toString()}", "", "", "", Timestamp.now(), mapOf("" to mapOf("" to ""))))
            onNoteCallback.onDataNote(failed, noteList)
        }
    }

    fun deleteNote(id: String, actionCallback: ActionCallback) {
        noteRef.document(id).delete().addOnSuccessListener {
            actionCallback.onActionFinish(success, "Note berhasil dihapus")
        }.addOnFailureListener { e ->
            actionCallback.onActionFinish(failed, "Note gagal dihapus!\nError: ${e.message.toString()}")
        }
    }
}