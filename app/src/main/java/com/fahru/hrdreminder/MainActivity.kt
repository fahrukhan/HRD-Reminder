package com.fahru.hrdreminder

import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.MenuItem
import android.view.View
import android.widget.PopupMenu
import androidx.recyclerview.widget.LinearLayoutManager
import com.fahru.hrdreminder.adapter.NoteAdapter
import com.fahru.hrdreminder.adapter.NoteExpiredAdapter
import com.fahru.hrdreminder.handler.*
import com.fahru.hrdreminder.model.BaseModel
import com.fahru.hrdreminder.model.NoteModel
import com.fahru.hrdreminder.model.SettingModel
import com.fahru.hrdreminder.submenu.AddNote
import com.fahru.hrdreminder.submenu.Setting
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*


class MainActivity : BaseModel() {
    private val auth = AuthHandler()
    private val db = NoteHandler()
    private val setting = SettingHandler()
    var set = setting.getDefaultSetting()
    private var doubleBackPressed = false
    private val calendar = Calendar.getInstance()
    private val list = arrayListOf<NoteModel>()
    private val expiredList = arrayListOf<NoteModel>()
    private val greetings = when (calendar.get(Calendar.HOUR_OF_DAY)) {
        in 0..11 -> R.string.good_morning
        in 12..15 -> R.string.good_afternoon
        in 16..20 -> R.string.good_evening
        in 21..23 -> R.string.good_night
        else -> R.string.how_are_you
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        setAllText()
        firstPrepare()
    }

    private fun loadSetting(){
        setting.loadSetting(object : SettingCallback{
            override fun onSetting(code: Int, setting: SettingModel) {
                setting.also {
                    setSetting(it)
                }
            }

        })
    }

    fun setSetting(setting: SettingModel){
        set = setting
        setRecyclerView()
    }

    private fun setAllText() {
        main_greetings.typeface = faceHymen(this)
        main_name.typeface = faceHymen(this)
        main_name.text = auth.getName()
        main_greetings.text = getString(greetings)
        when ((calendar.get(Calendar.HOUR_OF_DAY))){
            in 18..23 -> {
                main_line.visibility = View.VISIBLE
                main_msg.visibility = View.VISIBLE
            }
            in 1..3 -> {
                main_line.visibility = View.VISIBLE
                main_msg.visibility = View.VISIBLE
                main_msg.text = "Lembur nih..? Jam segini ngecek kerjaan.."
            }
            else -> {
                main_line.visibility = View.GONE
                main_msg.visibility = View.GONE
            }
        }
    }

    private fun firstPrepare() {
        loadSetting()
        main_setting_icon.setOnClickListener {
            showPopup(main_setting_icon)
        }
        main_fab.setOnClickListener {
            startActivity(Intent(this, AddNote::class.java))
        }
    }

    fun loadAnim(show: Boolean) {
        if (show) load_anim.visibility = View.VISIBLE
        else load_anim.visibility = View.GONE
    }

    private fun setRecyclerView() {
        loadAnim(true)
        db.getNotes(object : NoteCallback{
            override fun onDataNote(code: Int, note: ArrayList<NoteModel>) {
                when(code){
                    1 -> putListToRecycler(note)
                    0 -> {
                        toastError(this@MainActivity, note[0].id)
                        loadAnim(false)
                    }
                }
            }
        })
    }

    private fun putListToRecycler(notes: ArrayList<NoteModel>){
        list.clear()
        for (note in notes){
            val id = note.id
            val authorId = note.author_id
            val title = note.title
            val noteMain = note.note
            val deadline = note.deadline
            val history = note.history

            list.add(NoteModel(id, authorId, title, noteMain, deadline, history))
            main_rv.layoutManager = LinearLayoutManager(this)
            val listAdapter = NoteAdapter(list, set)
            main_rv.adapter = listAdapter
        }

        if (list.size == 0){ main_rv_none.visibility = View.VISIBLE }
        else{ main_rv_none.visibility = View.GONE }

        setRecyclerExpired()
    }

    private fun setRecyclerExpired(){
        db.getExpiredNotes(object : NoteCallback {
            override fun onDataNote(code: Int, note: ArrayList<NoteModel>) {
                when(code){
                    1 -> putExpiredListToRecycler(note)
                    0 -> {
                        toastError(this@MainActivity, note[0].id)
                        loadAnim(false)
                    }
                }
            }
        })
    }

    fun putExpiredListToRecycler(notes: ArrayList<NoteModel>){
        expiredList.clear()
        for (note in notes){
            val id = note.id
            val authorId = note.author_id
            val title = note.title
            val noteMain = note.note
            val deadline = note.deadline
            val history = note.history

            expiredList.add(NoteModel(id, authorId, title, noteMain, deadline, history))
            main_rv_expired.layoutManager = LinearLayoutManager(this)
            val listAdapter = NoteExpiredAdapter(expiredList)
            main_rv_expired.adapter = listAdapter
        }

        if (expiredList.size == 0){ main_rv_expired_none.visibility = View.VISIBLE }
        else{ main_rv_expired_none.visibility = View.GONE }

        loadAnim(false)
    }

    private fun showPopup(view: View) {
        val popup = PopupMenu(this, view)
        popup.inflate(R.menu.main_option_menu)
        popup.setOnMenuItemClickListener{ item: MenuItem? ->

            when (item!!.itemId) {
                R.id.menu_setting -> {
                    startActivity(Intent(this, Setting::class.java))
                }
                R.id.menu_logout -> {
                    logout()
                }
                R.id.menu_about -> {
                    showAbout()
                }
            }

            true
        }
        popup.show()
    }

    private fun logout() {
        val dialog = AlertDialog.Builder(this)
        dialog.setMessage("anda akan LOGOUT?")
        dialog.setPositiveButton("YA"){_, _ ->
            auth.getInstance().signOut().also {
                startActivity(Intent(this, Login::class.java))
            }
        }.setNegativeButton("TIDAK"){d, _ ->
            d.cancel()
        }
        dialog.show()
    }

    private fun showAbout() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.about)
        dialog.show()
    }

    override fun onBackPressed() {
        if (doubleBackPressed){
            super.onBackPressed()
            moveTaskToBack(true)
            return
        }

        this.doubleBackPressed = true
        toastNormal(this, getString(R.string.double_back_pressed_warning))
        Handler().postDelayed({ doubleBackPressed = false }, 2000)
    }

    override fun onResume() {
        super.onResume()
        setRecyclerView()
    }
}

