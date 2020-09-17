package com.fahru.hrdreminder.submenu

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import com.fahru.hrdreminder.MainActivity
import com.fahru.hrdreminder.R
import com.fahru.hrdreminder.handler.ActionCallback
import com.fahru.hrdreminder.handler.AuthHandler
import com.fahru.hrdreminder.handler.NoteCallback
import com.fahru.hrdreminder.handler.NoteHandler
import com.fahru.hrdreminder.model.BaseModel
import com.fahru.hrdreminder.model.NoteModel
import com.google.firebase.Timestamp
import kotlinx.android.synthetic.main.activity_add_note.*
import java.text.SimpleDateFormat
import java.util.*

class AddNote : BaseModel() {
    private var mode = 0
    private var editId: String = ""
    private var authorId: String = ""
    private val auth = AuthHandler()
    private val db = NoteHandler()
    private lateinit var date: DatePickerDialog.OnDateSetListener
    private var calendar = Calendar.getInstance()
    private val formatter = SimpleDateFormat("dd-MMMM-yyyy", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_note)

        firstPrepare()
        checkIntentExtra()
    }

    private fun checkIntentExtra() {
        if (intent.getStringExtra("id") != null){
            loadAnim(true)
            editId = intent.getStringExtra("id")!!
            mode = 1
            setEditMode()
        } else{
            Log.i("intent_extra", "none")
        }
    }

    private fun setEditMode() {
        db.getNote(editId, object : NoteCallback{
            override fun onDataNote(code: Int, note: ArrayList<NoteModel>) {
                when(code){
                    1 -> setField(note)
                }
            }
        })
    }

    private fun setField(note: java.util.ArrayList<NoteModel>) {
        add_note_title.setText(note[0].title)
        add_note_note.setText(note[0].note)
        calendar.time = note[0].deadline.toDate()
        authorId = note[0].author_id
        updateDate()
        loadAnim(false)
    }

    private fun firstPrepare() {
        add_note_header.typeface = faceHymen(this)

        date = DatePickerDialog.OnDateSetListener { _, y, m, d ->
            calendar.set(Calendar.YEAR, y)
            calendar.set(Calendar.MONTH, m)
            calendar.set(Calendar.DAY_OF_MONTH, d)
            updateDate()
        }

        add_note_deadline.setOnClickListener {
            hideSoftKeyboard(this)
            val datePicker = DatePickerDialog(this, date,
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH))
            datePicker.show()
        }
        add_note_save_button.setOnClickListener {
            hideSoftKeyboard(this)
            saveNote()
        }
    }

    private fun updateDate() {
        add_note_deadline.setText(formatter.format(calendar.time))
    }

    private fun getTimeNow(): String{
        val timeFormat = SimpleDateFormat("dd-MMMM-yyyy HH:mm:ss", Locale.getDefault())
        return timeFormat.format(Timestamp.now().toDate())
    }

    private fun saveNote() {
        when {
            emptyField() -> {
                toastError(this, getString(R.string.empty_field_error))
            }
            editId.isEmpty() -> {
                loadAnim(true)
                db.addNote(getNote(), object : ActionCallback{
                    override fun onActionFinish(code: Int, msg: String) {
                        when(code){
                            0 -> toastError(this@AddNote, msg)
                            1 -> toastSuccess(this@AddNote, msg).also {
                                clearText()
                            }
                        }
                        loadAnim(false)
                    }
                })
            }
            editId.isNotEmpty() -> {
                updateNote()
            }
        }
    }

    private fun updateNote(){
        loadAnim(true)
        if (authorId == auth.uid()) {
            val note = mapOf<String, Any>(
                "title" to getNote().title,
                "note" to getNote().note,
                "deadline" to getNote().deadline,
                "history" to getNote().history
            )
            db.updateNote(editId, note, object: ActionCallback{
                override fun onActionFinish(code: Int, msg: String) {
                    when(code){
                        1 -> toastSuccess(this@AddNote, msg)
                        0 -> toastError(this@AddNote, msg)
                    }
                    loadAnim(false)
                    startActivity(Intent(this@AddNote, MainActivity::class.java))
                }
            })
        }else{
            toastError(this, "Anda tidak memiliki izin untuk mngedit catatan ini")
            loadAnim(false)
        }
    }

    private fun clearText() {
        add_note_title.setText("")
        add_note_note.setText("")
        add_note_deadline.setText("")
    }

    private fun emptyField(): Boolean {
        return add_note_title.text.toString().isEmpty()
                || add_note_note.text.toString().isEmpty()
                || add_note_deadline.text.toString().isEmpty()
    }

    private fun getNote(): NoteModel {
        val authorId: String = auth.userId()
        val title = add_note_title.text.toString()
        val note = add_note_note.text.toString()
        val deadline: Timestamp = Timestamp(calendar.time)

        val firstCreated = when(mode){
            0 -> {
                mapOf(
                    "time" to getTimeNow(),
                    "note" to "Created: author=$authorId, title=$title, note=$note, deadline=$deadline"
                )
            }
            1 -> {
                mapOf(
                    "time" to getTimeNow(),
                    "note" to "Edited: author=$authorId, title=$title, note=$note, deadline=$deadline"
                )
            }
            else -> mapOf(
                "time" to getTimeNow(),
                "note" to "Edited: author=$authorId, title=$title, note=$note, deadline=$deadline"
            )
        }

        val history = mapOf("1" to firstCreated)
        return NoteModel("", authorId, title, note, deadline, history)
    }

    fun loadAnim(show: Boolean) {
        if (show) load_anim.visibility = View.VISIBLE
        else load_anim.visibility = View.GONE
    }
}