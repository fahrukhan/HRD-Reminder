package com.fahru.hrdreminder.adapter

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.fahru.hrdreminder.R
import com.fahru.hrdreminder.handler.ActionCallback
import com.fahru.hrdreminder.handler.NoteHandler
import com.fahru.hrdreminder.model.NoteModel
import com.fahru.hrdreminder.submenu.AddNote
import es.dmoral.toasty.Toasty
import java.text.SimpleDateFormat
import java.util.*

class NoteExpiredAdapter(private var noteList: ArrayList<NoteModel>): RecyclerView.Adapter<NoteExpiredAdapter.NoteHolder>(){

    companion object{
        @JvmStatic
        private lateinit var data: ArrayList<NoteModel>
        private lateinit var adapter: NoteExpiredAdapter
    }
    init {
        data = noteList
        adapter = this
    }

    class NoteHolder(view: View): RecyclerView.ViewHolder(view), View.OnCreateContextMenuListener {
        val icon: ImageView = view.findViewById(R.id.item_note_icon)
        val title: TextView = view.findViewById(R.id.item_note_title)
        val deadline: TextView = view.findViewById(R.id.item_note_deadline)
        private val db = NoteHandler()
        val context: Context = view.context
        init {
            view.setOnCreateContextMenuListener(this)
        }

        override fun onCreateContextMenu(p0: ContextMenu?, p1: View?, p2: ContextMenu.ContextMenuInfo?, ) {
            p0?.setHeaderTitle("MENU")
            val edit = p0?.add(Menu.NONE, 1, adapterPosition, "Edit")
            val del = p0?.add(Menu.NONE, 2, adapterPosition, "Delete")

            edit?.setOnMenuItemClickListener(onEditMenu)
            del?.setOnMenuItemClickListener(onEditMenu)
        }

        private val onEditMenu = MenuItem.OnMenuItemClickListener { item ->
            val position = item.order

            when(item.itemId){
                1 -> editNote(position)
                2 -> deleteNote(position)
                else -> Toasty.error(context, "Error: option menu not found", Toasty.LENGTH_SHORT, true).show()
            }
            return@OnMenuItemClickListener true
        }

        private fun editNote(position: Int) {
            val intent = Intent(context, AddNote::class.java)
            intent.putExtra("id", data[position].id)
            context.startActivity(intent)
        }

        private fun deleteNote(position: Int) {
            val msg = "${data[position].title.toUpperCase(Locale.getDefault())} akan hapus\n\nYAKIN?"
            val dialog = AlertDialog.Builder(context)
            dialog.setTitle("PERHATIAN!")
            dialog.setMessage(msg)
            dialog.setPositiveButton("YA"){_, _ ->
                db.deleteNote(data[position].id, object : ActionCallback{
                    override fun onActionFinish(code: Int, msg: String) {
                        when(code){
                            1 -> {
                                data.remove(data[position])
                                adapter.notifyDataSetChanged()
                            }
                            0 -> Toasty.error(context, msg, Toasty.LENGTH_SHORT, true).show()
                        }
                    }
                })
            }.setNegativeButton("CANCEL"){d, _ ->
                d.cancel()
            }
            dialog.show()
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteExpiredAdapter.NoteHolder {
        val view: View = LayoutInflater.from(parent.context).inflate(R.layout.item_note,
            parent,
            false)
        return NoteHolder(view)
    }

    override fun onBindViewHolder(holder: NoteExpiredAdapter.NoteHolder, position: Int) {
        val note = noteList[position]
        holder.title.text = note.title
        holder.icon.setImageResource(R.drawable.ic_expired)
        val formatter = SimpleDateFormat("dd-MMM-yyyy", Locale.getDefault())
        val timestamp = note.deadline.toDate()
        val date = formatter.format(timestamp)
        holder.deadline.text = date
        holder.itemView.setOnClickListener {
            Toast.makeText(holder.itemView.context, "klik tahan untuk melihat menu", Toast.LENGTH_SHORT).show()
        }
    }

    override fun getItemCount(): Int {
        return noteList.size
    }

}
